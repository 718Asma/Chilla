package com.enicarthage.chilla.controller;

import com.enicarthage.chilla.dto.CompteurResponse;
import com.enicarthage.chilla.dto.DtoMapper;
import com.enicarthage.chilla.dto.MessageResponse;
import com.enicarthage.chilla.dto.NotificationResponse;
import com.enicarthage.chilla.service.NotificationService;

import java.util.List;

public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController() {
        this(new NotificationService());
    }

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // ---------- LECTURE ----------

    public List<NotificationResponse> listerNotifications(int idMembre) {
        return notificationService.getNotifications(idMembre).stream().map(DtoMapper::vers).toList();
    }

    public List<NotificationResponse> listerNotificationsNonLues(int idMembre) {
        return notificationService.getNotificationsNonLues(idMembre).stream().map(DtoMapper::vers).toList();
    }

    public CompteurResponse compterNotificationsNonLues(int idMembre) {
        return new CompteurResponse(notificationService.compterNonLues(idMembre));
    }

    // ---------- MODIFICATION ----------

    public MessageResponse marquerCommeLue(int idNotification, int idMembre) {
        notificationService.marquerCommeLue(idNotification, idMembre);
        return new MessageResponse("Notification marquée comme lue.");
    }

    public MessageResponse marquerToutesCommeLues(int idMembre) {
        notificationService.marquerToutesCommeLues(idMembre);
        return new MessageResponse("Toutes les notifications sont marquées comme lues.");
    }

    // ---------- SUPPRESSION ----------

    public MessageResponse supprimerNotification(int idNotification, int idMembre) {
        notificationService.supprimer(idNotification, idMembre);
        return new MessageResponse("Notification supprimée.");
    }
}
