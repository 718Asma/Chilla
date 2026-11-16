package com.enicarthage.chilla.dto;

import com.enicarthage.chilla.model.Invitation;
import com.enicarthage.chilla.model.Membre;
import com.enicarthage.chilla.model.Membre_Projet;
import com.enicarthage.chilla.model.Notification;
import com.enicarthage.chilla.model.Projet;
import com.enicarthage.chilla.model.Sprint;
import com.enicarthage.chilla.model.Tache;
import com.enicarthage.chilla.service.AuthService;

/** Convertit les objets du modèle en DTO de réponse (aucune donnée sensible n'est exposée). */
public final class DtoMapper {

    private DtoMapper() {
    }

    public static MembreResponse vers(Membre m) {
        return new MembreResponse(m.getIDmem(), m.getNommem(), m.getPrenommem(), m.getEmailmem(),
                m.getDatecreamem());
    }

    public static ProjetResponse vers(Projet p) {
        return new ProjetResponse(p.getIDpro(), p.getLabpro(), p.getDescrpro(), p.getDatedpro(),
                p.getDatefpro(), Boolean.TRUE.equals(p.getEstarchive()));
    }

    public static SprintResponse vers(Sprint s) {
        return new SprintResponse(s.getIDsp(), s.getNomsp(), s.getObjectifsp(), s.getDatedsp(),
                s.getDatefsp(), s.getEtat(), s.getIDpro());
    }

    public static TacheResponse vers(Tache t) {
        return new TacheResponse(t.getIDtach(), t.getLabtach(), t.getDescrtach(), t.getDatecreatach(),
                t.getDateeche(), t.getEstim(), t.getEtat(), t.getPos(), t.getIDpro(), t.getIDmem(),
                t.getIDsp());
    }

    public static InvitationResponse vers(Invitation i) {
        return new InvitationResponse(i.getIDinvit(), i.getStatut(), i.getDateevoi(), i.getIDpro(),
                i.getIDemetteur(), i.getIDrecepteur());
    }

    public static NotificationResponse vers(Notification n) {
        return new NotificationResponse(n.getIDnotif(), n.getMessage(), n.getDateCreaNotif(),
                n.getEstlue(), n.getIDmem());
    }

    public static MembreProjetResponse vers(Membre_Projet mp) {
        return new MembreProjetResponse(mp.getIDmem(), mp.getIDpro(), mp.getRolemem());
    }

    public static SessionResponse vers(AuthService.Session session) {
        return new SessionResponse(session.token(), vers(session.membre()));
    }
}
