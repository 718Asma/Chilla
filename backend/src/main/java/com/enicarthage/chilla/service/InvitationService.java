package com.enicarthage.chilla.service;

import com.enicarthage.chilla.dao.InvitationDAO;
import com.enicarthage.chilla.dao.MembreDAO;
import com.enicarthage.chilla.dao.Membre_ProjetDAO;
import com.enicarthage.chilla.dao.ProjetDAO;
import com.enicarthage.chilla.model.Invitation;
import com.enicarthage.chilla.model.Membre;
import com.enicarthage.chilla.model.Membre_Projet;
import com.enicarthage.chilla.model.Projet;
import com.enicarthage.chilla.model.Rolemem;
import com.enicarthage.chilla.model.Statut;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InvitationService {

    private final InvitationDAO invitationDAO;
    private final ProjetDAO projetDAO;
    private final MembreDAO membreDAO;
    private final Membre_ProjetDAO membreProjetDAO;
    private final NotificationService notificationService;

    public InvitationService() {
        this(new InvitationDAO(), new ProjetDAO(), new MembreDAO(), new Membre_ProjetDAO(),
                new NotificationService());
    }

    public InvitationService(InvitationDAO invitationDAO, ProjetDAO projetDAO, MembreDAO membreDAO,
            Membre_ProjetDAO membreProjetDAO, NotificationService notificationService) {
        this.invitationDAO = invitationDAO;
        this.projetDAO = projetDAO;
        this.membreDAO = membreDAO;
        this.membreProjetDAO = membreProjetDAO;
        this.notificationService = notificationService;
    }

    // ---------- ENVOI ----------

    /** Invite un membre à rejoindre un projet. Remplit l'id généré. */
    public Invitation inviter(int idProjet, int idEmetteur, int idRecepteur) {
        Projet projet = getProjetActif(idProjet);
        verifierChef(idProjet, idEmetteur);

        if (idEmetteur == idRecepteur) {
            throw new IllegalArgumentException("Vous ne pouvez pas vous inviter vous-même.");
        }
        Membre recepteur = membreDAO.findById(idRecepteur)
                .orElseThrow(() -> new IllegalArgumentException("Membre introuvable : " + idRecepteur));
        if (membreProjetDAO.findById(idRecepteur, idProjet).isPresent()) {
            throw new IllegalStateException("Ce membre fait déjà partie du projet.");
        }
        if (aInvitationEnAttente(idProjet, idRecepteur)) {
            throw new IllegalStateException("Une invitation est déjà en attente pour ce membre.");
        }

        Invitation invitation = new Invitation();
        invitation.setStatut(Statut.EN_ATTENTE);
        invitation.setDateevoi(LocalDate.now());
        invitation.setIDpro(idProjet);
        invitation.setIDemetteur(idEmetteur);
        invitation.setIDrecepteur(recepteur.getIDmem());
        invitationDAO.create(invitation);

        Membre emetteur = membreDAO.findById(idEmetteur).orElse(null);
        String nomEmetteur = emetteur == null ? "Un membre"
                : emetteur.getPrenommem() + " " + emetteur.getNommem();
        notificationService.notifierSansErreur(idRecepteur,
                nomEmetteur + " vous invite à rejoindre le projet « " + projet.getLabpro() + " ».");
        return invitation;
    }

    /** Invite un membre à partir de son adresse email. */
    public Invitation inviterParEmail(int idProjet, int idEmetteur, String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("L'email est obligatoire.");
        }
        Membre recepteur = membreDAO.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Aucun membre avec l'email : " + email));
        return inviter(idProjet, idEmetteur, recepteur.getIDmem());
    }

    // ---------- REPONSE ----------

    public void accepter(int idInvitation, int idMembre) {
        Invitation invitation = getInvitationPourReponse(idInvitation, idMembre);
        Projet projet = getProjetActif(invitation.getIDpro());

        if (membreProjetDAO.findById(idMembre, projet.getIDpro()).isEmpty()) {
            Membre_Projet mp = new Membre_Projet();
            mp.setIDmem(idMembre);
            mp.setIDpro(projet.getIDpro());
            mp.setRolemem(Rolemem.MEMBRE);
            membreProjetDAO.create(mp);
        }
        invitation.setStatut(Statut.ACCEPTEE);
        invitationDAO.update(invitation);

        notifierEmetteur(invitation, " a accepté votre invitation au projet « " + projet.getLabpro() + " ».");
    }

    public void refuser(int idInvitation, int idMembre) {
        Invitation invitation = getInvitationPourReponse(idInvitation, idMembre);
        Projet projet = projetDAO.findById(invitation.getIDpro()).orElse(null);

        invitation.setStatut(Statut.REFUSEE);
        invitationDAO.update(invitation);

        String nomProjet = projet == null ? "" : " au projet « " + projet.getLabpro() + " »";
        notifierEmetteur(invitation, " a refusé votre invitation" + nomProjet + ".");
    }

    // ---------- ANNULATION ----------

    /** Annule une invitation en attente. Réservé à l'émetteur ou au chef du projet. */
    public void annuler(int idInvitation, int idActeur) {
        Invitation invitation = getInvitation(idInvitation);
        if (invitation.getIDemetteur() != idActeur) {
            verifierChef(invitation.getIDpro(), idActeur);
        }
        if (invitation.getStatut() != Statut.EN_ATTENTE) {
            throw new IllegalStateException("Seule une invitation en attente peut être annulée.");
        }
        invitationDAO.delete(idInvitation);
    }

    // ---------- LECTURE ----------

    public Invitation getInvitation(int idInvitation) {
        return invitationDAO.findById(idInvitation)
                .orElseThrow(() -> new IllegalArgumentException("Invitation introuvable : " + idInvitation));
    }

    /** Toutes les invitations reçues par un membre . */
    public List<Invitation> getInvitationsRecues(int idMembre) {
        return invitationDAO.findByRecepteur(idMembre);
    }

    /** Uniquement les invitations EN_ATTENTE d'un membre. */
    public List<Invitation> getInvitationsEnAttente(int idMembre) {
        List<Invitation> enAttente = new ArrayList<>();
        for (Invitation i : invitationDAO.findByRecepteur(idMembre)) {
            if (i.getStatut() == Statut.EN_ATTENTE) {
                enAttente.add(i);
            }
        }
        return enAttente;
    }

    // ---------- METHODES PRIVEES ----------

    private Invitation getInvitationPourReponse(int idInvitation, int idMembre) {
        Invitation invitation = getInvitation(idInvitation);
        if (invitation.getIDrecepteur() != idMembre) {
            throw new SecurityException("Cette invitation ne vous est pas destinée.");
        }
        if (invitation.getStatut() != Statut.EN_ATTENTE) {
            throw new IllegalStateException("Cette invitation a déjà reçu une réponse.");
        }
        return invitation;
    }

    private boolean aInvitationEnAttente(int idProjet, int idRecepteur) {
        for (Invitation i : invitationDAO.findByRecepteur(idRecepteur)) {
            if (i.getIDpro() == idProjet && i.getStatut() == Statut.EN_ATTENTE) {
                return true;
            }
        }
        return false;
    }

    private void notifierEmetteur(Invitation invitation, String suffixe) {
        Membre recepteur = membreDAO.findById(invitation.getIDrecepteur()).orElse(null);
        String nom = recepteur == null ? "Un membre"
                : recepteur.getPrenommem() + " " + recepteur.getNommem();
        notificationService.notifierSansErreur(invitation.getIDemetteur(), nom + suffixe);
    }

    private Projet getProjetActif(int idProjet) {
        Projet projet = projetDAO.findById(idProjet)
                .orElseThrow(() -> new IllegalArgumentException("Projet introuvable : " + idProjet));
        if (projet.getEstarchive()) {
            throw new IllegalStateException("Le projet est archivé : aucune invitation possible.");
        }
        return projet;
    }

    private void verifierChef(int idProjet, int idMembre) {
        Membre_Projet mp = membreProjetDAO.findById(idMembre, idProjet).orElse(null);
        if (mp == null || mp.getRolemem() != Rolemem.CHEF_DE_PROJET) {
            throw new SecurityException("Seul le chef de projet peut effectuer cette action.");
        }
    }
}