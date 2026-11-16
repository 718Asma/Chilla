package com.enicarthage.chilla.controller;

import java.util.List;

import com.enicarthage.chilla.dto.DtoMapper;
import com.enicarthage.chilla.dto.InvitationRequest;
import com.enicarthage.chilla.dto.InvitationResponse;
import com.enicarthage.chilla.dto.MessageResponse;
import com.enicarthage.chilla.model.Invitation;
import com.enicarthage.chilla.service.InvitationService;

public class InvitationController {

    private final InvitationService invitationService;

    public InvitationController() {
        this(new InvitationService());
    }

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    // ---------- ENVOI ----------

    /** Invite par email si l'email est renseigné, sinon par idRecepteur. */
    public InvitationResponse inviter(int idProjet, InvitationRequest requete, int idEmetteur) {
        boolean parEmail = requete.email() != null && !requete.email().isBlank();
        Invitation invitation = parEmail
                ? invitationService.inviterParEmail(idProjet, idEmetteur, requete.email())
                : invitationService.inviter(idProjet, idEmetteur, requete.idRecepteur());
        return DtoMapper.vers(invitation);
    }

    // ---------- REPONSE ----------

    public MessageResponse accepterInvitation(int idInvitation, int idMembre) {
        invitationService.accepter(idInvitation, idMembre);
        return new MessageResponse("Invitation acceptée : vous faites maintenant partie du projet.");
    }

    public MessageResponse refuserInvitation(int idInvitation, int idMembre) {
        invitationService.refuser(idInvitation, idMembre);
        return new MessageResponse("Invitation refusée.");
    }

    // ---------- ANNULATION ----------

    public MessageResponse annulerInvitation(int idInvitation, int idActeur) {
        invitationService.annuler(idInvitation, idActeur);
        return new MessageResponse("Invitation annulée.");
    }

    // ---------- LECTURE ----------

    /** Visible par l'émetteur, le destinataire ou le chef du projet. */
    public InvitationResponse consulterInvitation(int idInvitation, int idActeur) {
        return DtoMapper.vers(invitationService.getInvitationPourActeur(idInvitation, idActeur));
    }

    public List<InvitationResponse> listerInvitationsRecues(int idMembre) {
        return invitationService.getInvitationsRecues(idMembre).stream().map(DtoMapper::vers).toList();
    }

    public List<InvitationResponse> listerInvitationsEnAttente(int idMembre) {
        return invitationService.getInvitationsEnAttente(idMembre).stream().map(DtoMapper::vers).toList();
    }
}