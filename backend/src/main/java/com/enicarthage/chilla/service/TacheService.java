package com.enicarthage.chilla.service;

import com.enicarthage.chilla.dao.Membre_ProjetDAO;
import com.enicarthage.chilla.dao.ProjetDAO;
import com.enicarthage.chilla.dao.SprintDAO;
import com.enicarthage.chilla.dao.TacheDAO;
import com.enicarthage.chilla.model.Etatsp;
import com.enicarthage.chilla.model.Etattach;
import com.enicarthage.chilla.model.Membre_Projet;
import com.enicarthage.chilla.model.Projet;
import com.enicarthage.chilla.model.Rolemem;
import com.enicarthage.chilla.model.Sprint;
import com.enicarthage.chilla.model.Tache;

import java.util.List;

public class TacheService {

    private static final int MAX_LONGUEUR = 255;

    private final TacheDAO tacheDAO;
    private final SprintDAO sprintDAO;
    private final ProjetDAO projetDAO;
    private final Membre_ProjetDAO membreProjetDAO;
    private final NotificationService notificationService;

    public TacheService() {
        this(new TacheDAO(), new SprintDAO(), new ProjetDAO(), new Membre_ProjetDAO(), new NotificationService());
    }

    public TacheService(TacheDAO tacheDAO, SprintDAO sprintDAO, ProjetDAO projetDAO,
            Membre_ProjetDAO membreProjetDAO, NotificationService notificationService) {
        this.tacheDAO = tacheDAO;
        this.sprintDAO = sprintDAO;
        this.projetDAO = projetDAO;
        this.membreProjetDAO = membreProjetDAO;
        this.notificationService = notificationService;
    }

    // ---------- CREATION ----------

    /**
     * Crée une tâche (état NON_COMMENCE). Si pos <= 0, elle est placée en fin de sprint.
     * Remplit l'id généré.
     */
    public Tache creerTache(Tache tache, int idActeur) {
        if (tache == null) {
            throw new IllegalArgumentException("La tâche est obligatoire.");
        }
        getProjetActif(tache.getIDpro());
        verifierMembreDuProjet(tache.getIDpro(), idActeur);
        valider(tache);
        verifierSprintOuvert(tache.getIDsp(), tache.getIDpro());
        verifierAssigneDuProjet(tache.getIDmem(), tache.getIDpro());

        tache.setEtat(Etattach.NON_COMMENCE);
        tache.setDatecreatach(null);
        if (tache.getPos() <= 0) {
            tache.setPos(prochainePosition(tache.getIDsp()));
        }
        tacheDAO.create(tache);

        if (tache.getIDmem() != idActeur) {
            notificationService.notifierSansErreur(tache.getIDmem(),
                    "Une tâche vous a été assignée : " + tache.getLabtach());
        }
        return tache;
    }

    // ---------- LECTURE ----------

    public Tache getTache(int idTache) {
        return tacheDAO.findById(idTache)
                .orElseThrow(() -> new IllegalArgumentException("Tâche introuvable : " + idTache));
    }

    public List<Tache> getTachesDuProjet(int idProjet) {
        return tacheDAO.findByProjet(idProjet);
    }

    public List<Tache> getTachesDuSprint(int idSprint) {
        return tacheDAO.findBySprint(idSprint);
    }

    public List<Tache> getTachesDuMembre(int idMembre) {
        return tacheDAO.findByMembre(idMembre);
    }

    // ---------- MODIFICATION ----------

    /** Modifie libellé, description, échéance, estimation, position, sprint et assigné. L'état change via changerEtat. */
    public Tache modifierTache(Tache tache, int idActeur) {
        Tache existante = getTache(tache.getIDtach());
        getProjetActif(existante.getIDpro());
        verifierMembreDuProjet(existante.getIDpro(), idActeur);

        tache.setIDpro(existante.getIDpro());
        tache.setEtat(existante.getEtat());
        tache.setDatecreatach(existante.getDatecreatach());
        valider(tache);

        if (tache.getIDsp() != existante.getIDsp()) {
            verifierSprintOuvert(tache.getIDsp(), tache.getIDpro());
        }
        boolean assigneChange = tache.getIDmem() != existante.getIDmem();
        if (assigneChange) {
            verifierAssigneDuProjet(tache.getIDmem(), tache.getIDpro());
        }

        tacheDAO.update(tache);

        if (assigneChange && tache.getIDmem() != idActeur) {
            notificationService.notifierSansErreur(tache.getIDmem(),
                    "Une tâche vous a été assignée : " + tache.getLabtach());
        }
        return tache;
    }

    /** Change l'état d'une tâche. Autorisé pour le chef de projet et pour le membre assigné. */
    public void changerEtat(int idTache, Etattach nouvelEtat, int idActeur) {
        Tache tache = getTache(idTache);
        getProjetActif(tache.getIDpro());
        Membre_Projet acteur = verifierMembreDuProjet(tache.getIDpro(), idActeur);

        if (acteur.getRolemem() != Rolemem.CHEF_DE_PROJET && tache.getIDmem() != idActeur) {
            throw new SecurityException("Seul le chef de projet ou le membre assigné peut changer l'état.");
        }
        if (nouvelEtat == null) {
            throw new IllegalArgumentException("L'état est obligatoire.");
        }
        if (tache.getEtat() == nouvelEtat) {
            return;
        }
        tache.setEtat(nouvelEtat);
        tacheDAO.update(tache);
    }

    /** Réassigne une tâche à un autre membre du projet. Réservé au chef de projet. */
    public void assigner(int idTache, int idNouveauMembre, int idActeur) {
        Tache tache = getTache(idTache);
        getProjetActif(tache.getIDpro());
        verifierChef(tache.getIDpro(), idActeur);
        verifierAssigneDuProjet(idNouveauMembre, tache.getIDpro());

        if (tache.getIDmem() == idNouveauMembre) {
            return;
        }
        tache.setIDmem(idNouveauMembre);
        tacheDAO.update(tache);

        if (idNouveauMembre != idActeur) {
            notificationService.notifierSansErreur(idNouveauMembre,
                    "Une tâche vous a été assignée : " + tache.getLabtach());
        }
    }

    // ---------- SUPPRESSION ----------

    /** Supprime une tâche. Réservé au chef de projet. */
    public void supprimerTache(int idTache, int idActeur) {
        Tache tache = getTache(idTache);
        getProjetActif(tache.getIDpro());
        verifierChef(tache.getIDpro(), idActeur);
        tacheDAO.delete(idTache);
    }


    private Projet getProjetActif(int idProjet) {
        Projet projet = projetDAO.findById(idProjet)
                .orElseThrow(() -> new IllegalArgumentException("Projet introuvable : " + idProjet));
        if (projet.getEstarchive()) {
            throw new IllegalStateException("Le projet est archivé : aucune modification n'est possible.");
        }
        return projet;
    }

    private Membre_Projet verifierMembreDuProjet(int idProjet, int idMembre) {
        return membreProjetDAO.findById(idMembre, idProjet)
                .orElseThrow(() -> new SecurityException("Vous ne faites pas partie de ce projet."));
    }

    private void verifierChef(int idProjet, int idMembre) {
        if (verifierMembreDuProjet(idProjet, idMembre).getRolemem() != Rolemem.CHEF_DE_PROJET) {
            throw new SecurityException("Seul le chef de projet peut effectuer cette action.");
        }
    }

    private void verifierAssigneDuProjet(int idMembre, int idProjet) {
        if (membreProjetDAO.findById(idMembre, idProjet).isEmpty()) {
            throw new IllegalArgumentException("Le membre assigné ne fait pas partie du projet.");
        }
    }

    private void verifierSprintOuvert(int idSprint, int idProjet) {
        Sprint sprint = sprintDAO.findById(idSprint)
                .orElseThrow(() -> new IllegalArgumentException("Sprint introuvable : " + idSprint));
        if (sprint.getIDpro() != idProjet) {
            throw new IllegalArgumentException("Ce sprint n'appartient pas au projet de la tâche.");
        }
        if (sprint.getEtat() == Etatsp.TERMINE || sprint.getEtat() == Etatsp.ANNULE) {
            throw new IllegalStateException("Le sprint est terminé ou annulé : on ne peut pas y placer de tâche.");
        }
    }

    private int prochainePosition(int idSprint) {
        int max = 0;
        for (Tache t : tacheDAO.findBySprint(idSprint)) {
            max = Math.max(max, t.getPos());
        }
        return max + 1;
    }

    // Règles alignées sur le schéma SQL 
    private void valider(Tache tache) {
        if (tache.getLabtach() == null || tache.getLabtach().isBlank()) {
            throw new IllegalArgumentException("Le libellé de la tâche est obligatoire.");
        }
        if (tache.getLabtach().length() > MAX_LONGUEUR) {
            throw new IllegalArgumentException("Le libellé ne doit pas dépasser " + MAX_LONGUEUR + " caractères.");
        }
        if (tache.getDescrtach() != null && tache.getDescrtach().length() > MAX_LONGUEUR) {
            throw new IllegalArgumentException("La description ne doit pas dépasser " + MAX_LONGUEUR + " caractères.");
        }
        if (tache.getDateeche() == null) {
            throw new IllegalArgumentException("La date d'échéance est obligatoire.");
        }
        if (tache.getEstim() < 0) {
            throw new IllegalArgumentException("L'estimation ne peut pas être négative.");
        }
        if (tache.getPos() < 0) {
            throw new IllegalArgumentException("La position ne peut pas être négative.");
        }
    }
}