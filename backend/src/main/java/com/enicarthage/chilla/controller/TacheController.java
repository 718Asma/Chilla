package com.enicarthage.chilla.controller;

import com.enicarthage.chilla.dto.AssignationRequest;
import com.enicarthage.chilla.dto.DtoMapper;
import com.enicarthage.chilla.dto.EtatRequest;
import com.enicarthage.chilla.dto.MessageResponse;
import com.enicarthage.chilla.dto.TacheRequest;
import com.enicarthage.chilla.dto.TacheResponse;
import com.enicarthage.chilla.model.Etattach;
import com.enicarthage.chilla.model.Tache;
import com.enicarthage.chilla.service.TacheService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class TacheController {

    private final TacheService tacheService;

    public TacheController() {
        this(new TacheService());
    }

    public TacheController(TacheService tacheService) {
        this.tacheService = tacheService;
    }

    // ---------- CREATION ----------

    public TacheResponse creerTache(int idProjet, TacheRequest requete, int idActeur) {
        Tache tache = construire(0, idProjet, requete);
        return DtoMapper.vers(tacheService.creerTache(tache, idActeur));
    }

    // ---------- LECTURE ----------

    public TacheResponse consulterTache(int idTache) {
        return DtoMapper.vers(tacheService.getTache(idTache));
    }

    public List<TacheResponse> listerTachesDuProjet(int idProjet) {
        return tacheService.getTachesDuProjet(idProjet).stream().map(DtoMapper::vers).toList();
    }

    public List<TacheResponse> listerTachesDuSprint(int idSprint) {
        return tacheService.getTachesDuSprint(idSprint).stream().map(DtoMapper::vers).toList();
    }

    public List<TacheResponse> listerTachesDuMembre(int idMembre) {
        return tacheService.getTachesDuMembre(idMembre).stream().map(DtoMapper::vers).toList();
    }

    // ---------- MODIFICATION ----------

    public TacheResponse modifierTache(int idTache, TacheRequest requete, int idActeur) {
        Tache tache = construire(idTache, 0, requete);
        return DtoMapper.vers(tacheService.modifierTache(tache, idActeur));
    }

    public MessageResponse changerEtat(int idTache, EtatRequest requete, int idActeur) {
        tacheService.changerEtat(idTache, lireEtat(requete.etat()), idActeur);
        return new MessageResponse("État de la tâche modifié.");
    }

    public MessageResponse assignerTache(int idTache, AssignationRequest requete, int idActeur) {
        tacheService.assigner(idTache, requete.idMembre(), idActeur);
        return new MessageResponse("Tâche assignée.");
    }

    // ---------- SUPPRESSION ----------

    public MessageResponse supprimerTache(int idTache, int idActeur) {
        tacheService.supprimerTache(idTache, idActeur);
        return new MessageResponse("Tâche supprimée.");
    }

    private Tache construire(int idTache, int idProjet, TacheRequest requete) {
        Tache tache = new Tache();
        tache.setIDtach(idTache);
        tache.setIDpro(idProjet);
        tache.setIDsp(requete.idSprint());
        tache.setIDmem(requete.idMembreAssigne());
        tache.setLabtach(requete.libelle() == null ? null : requete.libelle().trim());
        tache.setDescrtach(requete.description() == null || requete.description().isBlank()
                ? null
                : requete.description().trim());
        tache.setDateeche(lireDate(requete.dateEcheance()));
        tache.setEstim(requete.estimation());
        tache.setPos(requete.position());
        return tache;
    }

    private LocalDate lireDate(String texte) {
        if (texte == null || texte.isBlank()) {
            throw new IllegalArgumentException("La date d'échéance est obligatoire.");
        }
        try {
            return LocalDate.parse(texte.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("La date d'échéance est invalide (format attendu : aaaa-mm-jj).");
        }
    }

    private Etattach lireEtat(String texte) {
        if (texte == null || texte.isBlank()) {
            throw new IllegalArgumentException("L'état est obligatoire.");
        }
        try {
            return Etattach.valueOf(texte.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("État invalide. Valeurs possibles : "
                    + Arrays.toString(Etattach.values()));
        }
    }
}
