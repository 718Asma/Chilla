package com.enicarthage.chilla.controller;

import com.enicarthage.chilla.dto.DtoMapper;
import com.enicarthage.chilla.dto.MessageResponse;
import com.enicarthage.chilla.dto.ProjetRequest;
import com.enicarthage.chilla.dto.ProjetResponse;
import com.enicarthage.chilla.model.Projet;
import com.enicarthage.chilla.service.ProjetService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ProjetController {

    private final ProjetService projetService;

    public ProjetController() {
        this(new ProjetService());
    }

    public ProjetController(ProjetService projetService) {
        this.projetService = projetService;
    }

    // ---------- CREATION ----------

    public ProjetResponse creerProjet(ProjetRequest requete, int idCreateur) {
        Projet projet = construire(0, requete);
        return DtoMapper.vers(projetService.creerProjet(projet, idCreateur));
    }

    // ---------- LECTURE ----------

    public List<ProjetResponse> listerTousLesProjets() {
        return projetService.getTousLesProjets().stream().map(DtoMapper::vers).toList();
    }

    public List<ProjetResponse> listerProjetsDuMembre(int idMembre) {
        return projetService.getProjetsDuMembre(idMembre).stream().map(DtoMapper::vers).toList();
    }

    public List<ProjetResponse> listerProjetsActifsDuMembre(int idMembre) {
        return projetService.getProjetsActifsDuMembre(idMembre).stream().map(DtoMapper::vers).toList();
    }

    public ProjetResponse consulterProjet(int idProjet) {
        return DtoMapper.vers(projetService.getProjet(idProjet));
    }

    // ---------- MODIFICATION ----------

    public ProjetResponse modifierProjet(int idProjet, ProjetRequest requete, int idMembre) {
        Projet projet = construire(idProjet, requete);
        return DtoMapper.vers(projetService.modifierProjet(projet, idMembre));
    }

    public MessageResponse archiverProjet(int idProjet, int idMembre) {
        projetService.archiverProjet(idProjet, idMembre);
        return new MessageResponse("Projet archivé.");
    }

    public MessageResponse desarchiverProjet(int idProjet, int idMembre) {
        projetService.desarchiverProjet(idProjet, idMembre);
        return new MessageResponse("Projet désarchivé.");
    }

    // ---------- SUPPRESSION ----------

    public MessageResponse supprimerProjet(int idProjet, int idMembre) {
        projetService.supprimerProjet(idProjet, idMembre);
        return new MessageResponse("Projet supprimé.");
    }

    // Nettoie les saisies : espaces retirés, description vide -> null
    private Projet construire(int idProjet, ProjetRequest requete) {
        Projet projet = new Projet();
        projet.setIDpro(idProjet);
        projet.setLabpro(requete.libelle() == null ? null : requete.libelle().trim());
        projet.setDescrpro(requete.description() == null || requete.description().isBlank()
                ? null
                : requete.description().trim());
        projet.setDatedpro(lireDate(requete.dateDebut(), "début"));
        projet.setDatefpro(lireDate(requete.dateFin(), "fin"));
        return projet;
    }

    private LocalDate lireDate(String texte, String nomChamp) {
        if (texte == null || texte.isBlank()) {
            throw new IllegalArgumentException("La date de " + nomChamp + " est obligatoire.");
        }
        try {
            return LocalDate.parse(texte.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "La date de " + nomChamp + " est invalide (format attendu : aaaa-mm-jj).");
        }
    }
}
