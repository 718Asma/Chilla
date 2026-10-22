package com.enicarthage.chilla.controller;

import com.enicarthage.chilla.exception.DaoException;
import com.enicarthage.chilla.service.NotificationService;

import java.util.function.Supplier;

public class NotificationController {

    private final NotificationService notificationService;

    private boolean succes;
    private String message;
    private Object donnees;

    public NotificationController() {
        this(new NotificationService());
    }

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }


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

    // ---------- LECTURE ----------

    /** Donnees : List<Notification>  */
    public boolean listerNotifications(int idMembre) {
        return executer(() -> notificationService.getNotifications(idMembre), "Notifications chargées.");
    }

    /** Donnees : List<Notification> */
    public boolean listerNotificationsNonLues(int idMembre) {
        return executer(() -> notificationService.getNotificationsNonLues(idMembre),
                "Notifications non lues chargées.");
    }

    /** Donnees : Integer */
    public boolean compterNotificationsNonLues(int idMembre) {
        return executer(() -> notificationService.compterNonLues(idMembre), "Nombre de notifications chargé.");
    }

    // ---------- MODIFICATION ----------

    public boolean marquerCommeLue(int idNotification, int idMembre) {
        return executerSansRetour(() -> notificationService.marquerCommeLue(idNotification, idMembre),
                "Notification marquée comme lue.");
    }

    public boolean marquerToutesCommeLues(int idMembre) {
        return executerSansRetour(() -> notificationService.marquerToutesCommeLues(idMembre),
                "Toutes les notifications sont marquées comme lues.");
    }

    // ---------- SUPPRESSION ----------

    public boolean supprimerNotification(int idNotification, int idMembre) {
        return executerSansRetour(() -> notificationService.supprimer(idNotification, idMembre),
                "Notification supprimée.");
    }


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