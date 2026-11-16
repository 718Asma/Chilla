package com.enicarthage.chilla.dto;

/** POST /projets/{id}/invitations : idRecepteur OU email */
public record InvitationRequest(int idRecepteur, String email) {
}
