package com.enicarthage.chilla.dto;

import java.time.LocalDateTime;

/** Notification */
public record NotificationResponse(int id, String message, LocalDateTime dateCreation, boolean lue, int idMembre) {
}
