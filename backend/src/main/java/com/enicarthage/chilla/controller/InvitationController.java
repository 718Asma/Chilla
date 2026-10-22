package com.enicarthage.chilla.controller;

import com.enicarthage.chilla.exception.DaoException;
import com.enicarthage.chilla.service.InvitationService;

import java.util.function.Supplier;

public class InvitationController {

    private final InvitationService invitationService;

    // Résultat de la dernière action
    private boolean succes;
    private String message;
    private Object donnees;

    public InvitationController() {
        this(new InvitationService());
    }

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    // ---------- RESULTAT DE LA DERNIERE ACTION ----------

    public boolean isSucces() {
        return succes;
    }

    public String getMessage() {
        return message;
    }

    /** Type déduit de la variable */
    @SuppressWarnings("unchecked")
    public <T> T getDonnees() {
        return (T) donnees;
    }

    // ---------- ENVOI ----------
    // Donnees : l'Invitation créée 

    public boolean inviterMembre(int idProjet, int idEmetteur, int idRecepteur) {
        return executer(() -> invitationService.inviter(idProjet, idEmetteur, idRecepteur),
                "Invitation envoyée.");
    }

    public boolean inviterParEmail(int idProjet, int idEmetteur, String email) {
        return executer(() -> invitationService.inviterParEmail(idProjet, idEmetteur, email),
                "Invitation envoyée.");
    }

    // ---------- REPONSE ----------

    public boolean accepterInvitation(int idInvitation, int idMembre) {
        return executerSansRetour(() -> invitationService.accepter(idInvitation, idMembre),
                "Invitation acceptée : vous faites maintenant partie du projet.");
    }

    public boolean refuserInvitation(int idInvitation, int idMembre) {
        return executerSansRetour(() -> invitationService.refuser(idInvitation, idMembre),
                "Invitation refusée.");
    }

    // ---------- ANNULATION ----------

    public boolean annulerInvitation(int idInvitation, int idActeur) {
        return executerSansRetour(() -> invitationService.annuler(idInvitation, idActeur),
                "Invitation annulée.");
    }

    // ---------- LECTURE ----------

    /** Donnees : Invitation */
    public boolean consulterInvitation(int idInvitation) {
        return executer(() -> invitationService.getInvitation(idInvitation), "Invitation chargée.");
    }

    /** Donnees : List<Invitation>  */
    public boolean listerInvitationsRecues(int idMembre) {
        return executer(() -> invitationService.getInvitationsRecues(idMembre), "Invitations chargées.");
    }

    /** Donnees : List<Invitation>  */
    public boolean listerInvitationsEnAttente(int idMembre) {
        return executer(() -> invitationService.getInvitationsEnAttente(idMembre),
                "Invitations en attente chargées.");
    }

    // ---------- METHODES PRIVEES ----------

    // Exécute une action du service, mémorise le résultat et retourne true si elle a réussi
    private boolean executer(Supplier<?> action, String messageSucces) {
        try {
            donnees = action.get();
            succes = true;
            message = messageSucces;
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            echec(e.getMessage());
        } catch (DaoException e) {
            System.err.println("Erreur technique : " + e.getMessage());
            echec("Une erreur technique est survenue (base de données). Veuillez réessayer.");
        } catch (RuntimeException e) {
            System.err.println("Erreur inattendue : " + e);
            echec("Une erreur inattendue est survenue.");
        }
        return succes;
    }

    private boolean executerSansRetour(Runnable action, String messageSucces) {
        return executer(() -> {
            action.run();
            return null;
        }, messageSucces);
    }

    private void echec(String messageErreur) {
        succes = false;
        donnees = null;
        message = messageErreur;
    }
}