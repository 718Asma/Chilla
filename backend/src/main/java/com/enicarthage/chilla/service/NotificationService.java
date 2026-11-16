package com.enicarthage.chilla.service;

import com.enicarthage.chilla.exception.ResourceNotFoundException;
import com.enicarthage.chilla.dao.MembreDAO;
import com.enicarthage.chilla.dao.NotificationDAO;
import com.enicarthage.chilla.model.Notification;

import java.util.List;

public class NotificationService {

    private static final int MAX_LONGUEUR = 255;

    private final NotificationDAO notificationDAO;
    private final MembreDAO membreDAO;
    private final NotificationHub hub;

    public NotificationService() {
        this(new NotificationDAO(), new MembreDAO(), NotificationHub.getInstance());
    }

    public NotificationService(NotificationDAO notificationDAO, MembreDAO membreDAO) {
        this(notificationDAO, membreDAO, NotificationHub.getInstance());
    }

    // Constructeur avec injection : pratique pour les tests
    public NotificationService(NotificationDAO notificationDAO, MembreDAO membreDAO, NotificationHub hub) {
        this.notificationDAO = notificationDAO;
        this.membreDAO = membreDAO;
        this.hub = hub;
    }

    /**
     * Envoie une notification à un membre (enregistrée en base, puis poussée en
     * temps réel s'il est connecté).
     */
    public Notification notifier(int idMembre, String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Le message de la notification est obligatoire.");
        }
        if (membreDAO.findById(idMembre).isEmpty()) {
            throw new ResourceNotFoundException("Membre introuvable : " + idMembre);
        }

        Notification notification = new Notification();
        notification.setMessage(message.length() > MAX_LONGUEUR
                ? message.substring(0, MAX_LONGUEUR - 3) + "..."
                : message);
        notification.setIDmem(idMembre);
        notification.setEstlue(false);
        notificationDAO.create(notification);

        diffuser(notification);
        return notification;
    }

    public void notifierSansErreur(int idMembre, String message) {
        try {
            notifier(idMembre, message);
        } catch (RuntimeException e) {
            System.err.println("Notification non envoyée au membre " + idMembre + " : " + e.getMessage());
        }
    }

    public List<Notification> getNotifications(int idMembre) {
        return notificationDAO.findByMembre(idMembre);
    }

    public List<Notification> getNotificationsNonLues(int idMembre) {
        return notificationDAO.findUnreadByMembre(idMembre);
    }

    public int compterNonLues(int idMembre) {
        return notificationDAO.findUnreadByMembre(idMembre).size();
    }

    public void marquerCommeLue(int idNotification, int idMembre) {
        verifierProprietaire(idNotification, idMembre);
        notificationDAO.markAsRead(idNotification);
    }

    public void marquerToutesCommeLues(int idMembre) {
        for (Notification n : notificationDAO.findUnreadByMembre(idMembre)) {
            notificationDAO.markAsRead(n.getIDnotif());
        }
    }

    public void supprimer(int idNotification, int idMembre) {
        verifierProprietaire(idNotification, idMembre);
        notificationDAO.delete(idNotification);
    }

    private void verifierProprietaire(int idNotification, int idMembre) {
        Notification notification = notificationDAO.findById(idNotification)
                .orElseThrow(() -> new ResourceNotFoundException("Notification introuvable : " + idNotification));
        if (notification.getIDmem() != idMembre) {
            throw new SecurityException("Cette notification ne vous appartient pas.");
        }
    }

    // La notification est déjà en base : un échec de la diffusion ne doit jamais
    // faire échouer l'action
    // qui l'a déclenchée (assignation d'une tâche, invitation...).
    private void diffuser(Notification notification) {
        try {
            if (!hub.aDesAbonnes(notification.getIDmem())) {
                return; // membre hors ligne : il la verra à sa prochaine connexion
            }
            // On relit la ligne pour envoyer la même chose que GET /notifications (date
            // générée par la base)
            Notification complete = notificationDAO.findById(notification.getIDnotif()).orElse(notification);
            hub.publier(notification.getIDmem(), complete);
        } catch (RuntimeException e) {
            System.err.println("Diffusion temps réel impossible pour le membre "
                    + notification.getIDmem() + " : " + e.getMessage());
        }
    }
}