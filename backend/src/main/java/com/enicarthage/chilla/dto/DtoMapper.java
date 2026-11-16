package com.enicarthage.chilla.dto;

import com.enicarthage.chilla.model.Invitation;
import com.enicarthage.chilla.model.Membre;
import com.enicarthage.chilla.model.Membre_Projet;
import com.enicarthage.chilla.model.Notification;
import com.enicarthage.chilla.model.Projet;
import com.enicarthage.chilla.model.Rolemem;
import com.enicarthage.chilla.model.Sprint;
import com.enicarthage.chilla.model.Tache;
import com.enicarthage.chilla.service.AuthService;

import java.util.ArrayList;
import java.util.List;

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

    /** Convertit ce que renvoie un controller (objet, liste, entier...) en DTO de réponse. */
    public static Object convertir(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Membre m) {
            return vers(m);
        }
        if (o instanceof Projet p) {
            return vers(p);
        }
        if (o instanceof Sprint s) {
            return vers(s);
        }
        if (o instanceof Tache t) {
            return vers(t);
        }
        if (o instanceof Invitation i) {
            return vers(i);
        }
        if (o instanceof Notification n) {
            return vers(n);
        }
        if (o instanceof Membre_Projet mp) {
            return vers(mp);
        }
        if (o instanceof AuthService.Session s) {
            return new SessionResponse(s.token(), vers(s.membre()));
        }
        if (o instanceof Rolemem r) {
            return new RoleResponse(r);
        }
        if (o instanceof Integer n) {
            return new CompteurResponse(n);
        }
        if (o instanceof List<?> liste) {
            List<Object> resultat = new ArrayList<>();
            for (Object element : liste) {
                resultat.add(convertir(element));
            }
            return resultat;
        }
        return o;
    }
}
