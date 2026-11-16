package com.enicarthage.chilla.dto;

import java.time.LocalDateTime;

import com.enicarthage.chilla.model.Statut;

/** Invitation */
public record InvitationResponse(int id, Statut statut, LocalDateTime dateEnvoi, int idProjet, int idEmetteur, int idRecepteur) {
}
