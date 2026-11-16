package com.enicarthage.chilla.controller;

import com.enicarthage.chilla.dto.DtoMapper;
import com.enicarthage.chilla.dto.MembreResponse;
import com.enicarthage.chilla.dto.MessageResponse;
import com.enicarthage.chilla.dto.MotDePasseRequest;
import com.enicarthage.chilla.dto.ProfilRequest;
import com.enicarthage.chilla.dto.SuppressionCompteRequest;
import com.enicarthage.chilla.model.Membre;
import com.enicarthage.chilla.service.MembreService;

import java.util.List;

// L'inscription et la connexion sont dans AuthController.
public class MembreController {

    private final MembreService membreService;

    public MembreController() {
        this(new MembreService());
    }

    public MembreController(MembreService membreService) {
        this.membreService = membreService;
    }

    // ---------- LECTURE ----------

    public List<MembreResponse> listerTousLesMembres() {
        return membreService.getTousLesMembres().stream().map(DtoMapper::vers).toList();
    }

    public MembreResponse consulterMembre(int idMembre) {
        return DtoMapper.vers(membreService.getMembre(idMembre));
    }

    // ---------- MODIFICATION ----------

    public MembreResponse modifierProfil(int idMembre, ProfilRequest requete) {
        Membre membre = new Membre();
        membre.setIDmem(idMembre);
        membre.setNommem(nettoyer(requete.nom()));
        membre.setPrenommem(nettoyer(requete.prenom()));
        membre.setEmailmem(nettoyer(requete.email()));
        return DtoMapper.vers(membreService.modifierProfil(membre));
    }

    public MessageResponse changerMotDePasse(int idMembre, MotDePasseRequest requete) {
        membreService.changerMotDePasse(idMembre, requete.ancienMotDePasse(), requete.nouveauMotDePasse());
        return new MessageResponse("Mot de passe modifié avec succès.");
    }

    // ---------- SUPPRESSION ----------

    public MessageResponse supprimerCompte(int idMembre, SuppressionCompteRequest requete) {
        membreService.supprimerCompte(idMembre, requete.motDePasse());
        return new MessageResponse("Compte supprimé.");
    }

    private String nettoyer(String texte) {
        return texte == null ? null : texte.trim();
    }
}
