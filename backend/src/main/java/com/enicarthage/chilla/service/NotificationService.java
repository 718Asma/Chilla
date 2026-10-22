package com.enicarthage.chilla.service;

import com.enicarthage.chilla.dao.MembreDAO;
import com.enicarthage.chilla.dao.NotificationDAO;
import com.enicarthage.chilla.model.Notification;

import java.util.List;

public class NotificationService {

    private static final int MAX_LONGUEUR = 255;

    private final NotificationDAO notificationDAO;
    private final MembreDAO membreDAO;

    public NotificationService() {
        this(new NotificationDAO(), new MembreDAO());
    }

    public NotificationService(NotificationDAO notificationDAO, MembreDAO membreDAO) {
        this.notificationDAO = notificationDAO;
        this.membreDAO = membreDAO;
    }

    /** Envoie une notification à un membre */
    public Notification notifier(int idMembre, String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Le message de la notification est obligatoire.");
        }
        if (membreDAO.findById(idMembre).isEmpty()) {
            throw new IllegalArgumentException("Membre introuvable : " + idMembre);
        }

        Notification notification = new Notification();
        notification.setMessage(message.length() > MAX_LONGUEUR
                ? message.substring(0, MAX_LONGUEUR - 3) + "..."
                : message);
        notification.setIDmem(idMembre);
        notification.setEstlue(false);
        notificationDAO.create(notification);
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
                .orElseThrow(() -> new IllegalArgumentException("Notification introuvable : " + idNotification));
        if (notification.getIDmem() != idMembre) {
            throw new SecurityException("Cette notification ne vous appartient pas.");
        }
    }
}