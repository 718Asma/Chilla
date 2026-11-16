package com.enicarthage.chilla.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.enicarthage.chilla.dto.DtoMapper;
import com.enicarthage.chilla.dto.MembreProjetResponse;
import com.enicarthage.chilla.dto.MessageResponse;
import com.enicarthage.chilla.dto.RoleRequest;
import com.enicarthage.chilla.dto.RoleResponse;
import com.enicarthage.chilla.model.Rolemem;
import com.enicarthage.chilla.service.Membre_ProjetService;

public class Membre_ProjetController {

    private final Membre_ProjetService membreProjetService;

    public Membre_ProjetController() {
        this(new Membre_ProjetService());
    }

    public Membre_ProjetController(Membre_ProjetService membreProjetService) {
        this.membreProjetService = membreProjetService;
    }

    // ---------- CONTROLE D'ACCES (lecture) ----------

    /** Lève SecurityException (403) si le membre ne fait pas partie du projet. */
    public void verifierMembre(int idProjet, int idMembre) {
        if (!membreProjetService.estMembre(idProjet, idMembre)) {
            throw new SecurityException("Vous ne faites pas partie de ce projet.");
        }
    }

    /**
     * Autorise la consultation d'un autre membre seulement s'il partage un projet
     * avec l'acteur.
     */
    public void verifierCoequipier(int idActeur, int idAutre) {
        if (!membreProjetService.partagentUnProjet(idActeur, idAutre)) {
            throw new SecurityException("Vous ne pouvez consulter que les membres de vos projets.");
        }
    }

    // ---------- LECTURE ----------

    public List<MembreProjetResponse> listerMembresDuProjet(int idProjet) {
        return membreProjetService.getMembresDuProjet(idProjet).stream().map(DtoMapper::vers).toList();
    }

    public RoleResponse consulterRole(int idProjet, int idMembre) {
        return new RoleResponse(membreProjetService.getRole(idProjet, idMembre));
    }

    // ---------- MODIFICATION ----------

    public MessageResponse changerRole(int idProjet, int idMembreCible, RoleRequest requete, int idActeur) {
        membreProjetService.changerRole(idProjet, idMembreCible, lireRole(requete.role()), idActeur);
        return new MessageResponse("Rôle modifié.");
    }

    // ---------- RETRAIT ----------

    /** Le chef retire un membre du projet. */
    public MessageResponse retirerMembre(int idProjet, int idMembreCible, int idActeur) {
        membreProjetService.retirerMembre(idProjet, idMembreCible, idActeur);
        return new MessageResponse("Membre retiré du projet.");
    }

    /** Le membre connecté quitte lui-même le projet. */
    public MessageResponse quitterProjet(int idProjet, int idMembre) {
        membreProjetService.retirerMembre(idProjet, idMembre, idMembre);
        return new MessageResponse("Vous avez quitté le projet.");
    }

    private Rolemem lireRole(String texte) {
        if (texte == null || texte.isBlank()) {
            throw new IllegalArgumentException("Le rôle est obligatoire.");
        }
        try {
            return Rolemem.valueOf(texte.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Rôle invalide. Valeurs possibles : "
                    + Arrays.toString(Rolemem.values()));
        }
    }
}