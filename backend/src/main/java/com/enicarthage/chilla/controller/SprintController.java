package com.enicarthage.chilla.controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.enicarthage.chilla.dto.DtoMapper;
import com.enicarthage.chilla.dto.EtatRequest;
import com.enicarthage.chilla.dto.MessageResponse;
import com.enicarthage.chilla.dto.SprintRequest;
import com.enicarthage.chilla.dto.SprintResponse;
import com.enicarthage.chilla.model.Etatsp;
import com.enicarthage.chilla.model.Sprint;
import com.enicarthage.chilla.service.SprintService;

public class SprintController {

    private final SprintService sprintService;

    public SprintController() {
        this(new SprintService());
    }

    public SprintController(SprintService sprintService) {
        this.sprintService = sprintService;
    }

    // ---------- CREATION ----------

    public SprintResponse creerSprint(int idProjet, SprintRequest requete, int idMembre) {
        Sprint sprint = construire(0, idProjet, requete);
        return DtoMapper.vers(sprintService.creerSprint(sprint, idMembre));
    }

    // ---------- LECTURE ----------

    /** Réservé aux membres du projet du sprint. */
    public SprintResponse consulterSprint(int idSprint, int idActeur) {
        return DtoMapper.vers(sprintService.getSprintPourMembre(idSprint, idActeur));
    }

    public List<SprintResponse> listerSprintsDuProjet(int idProjet) {
        return sprintService.getSprintsDuProjet(idProjet).stream().map(DtoMapper::vers).toList();
    }

    /**
     * Le sprint en cours (SprintResponse) ou un message s'il n'y en a pas
     * (MessageResponse).
     */
    public Object consulterSprintEnCours(int idProjet) {
        Optional<Sprint> enCours = sprintService.getSprintEnCours(idProjet);
        if (enCours.isPresent()) {
            return DtoMapper.vers(enCours.get());
        }
        return new MessageResponse("Aucun sprint en cours dans ce projet.");
    }

    // ---------- MODIFICATION ----------

    public SprintResponse modifierSprint(int idSprint, SprintRequest requete, int idMembre) {
        Sprint sprint = construire(idSprint, 0, requete);
        return DtoMapper.vers(sprintService.modifierSprint(sprint, idMembre));
    }

    public MessageResponse changerEtat(int idSprint, EtatRequest requete, int idMembre) {
        sprintService.changerEtat(idSprint, lireEtat(requete.etat()), idMembre);
        return new MessageResponse("État du sprint modifié.");
    }

    public MessageResponse demarrerSprint(int idSprint, int idMembre) {
        sprintService.changerEtat(idSprint, Etatsp.EN_COURS, idMembre);
        return new MessageResponse("Sprint démarré.");
    }

    public MessageResponse terminerSprint(int idSprint, int idMembre) {
        sprintService.changerEtat(idSprint, Etatsp.TERMINE, idMembre);
        return new MessageResponse("Sprint terminé.");
    }

    public MessageResponse annulerSprint(int idSprint, int idMembre) {
        sprintService.changerEtat(idSprint, Etatsp.ANNULE, idMembre);
        return new MessageResponse("Sprint annulé.");
    }

    // ---------- SUPPRESSION ----------

    public MessageResponse supprimerSprint(int idSprint, int idMembre) {
        sprintService.supprimerSprint(idSprint, idMembre);
        return new MessageResponse("Sprint supprimé.");
    }

    private Sprint construire(int idSprint, int idProjet, SprintRequest requete) {
        Sprint sprint = new Sprint();
        sprint.setIDsp(idSprint);
        sprint.setIDpro(idProjet);
        sprint.setNomsp(requete.nom() == null ? null : requete.nom().trim());
        sprint.setObjectifsp(requete.objectif() == null || requete.objectif().isBlank()
                ? null
                : requete.objectif().trim());
        sprint.setDatedsp(lireDate(requete.dateDebut(), "début"));
        sprint.setDatefsp(lireDate(requete.dateFin(), "fin"));
        return sprint;
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

    private Etatsp lireEtat(String texte) {
        if (texte == null || texte.isBlank()) {
            throw new IllegalArgumentException("L'état est obligatoire.");
        }
        try {
            return Etatsp.valueOf(texte.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("État invalide. Valeurs possibles : "
                    + Arrays.toString(Etatsp.values()));
        }
    }
}