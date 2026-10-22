package com.enicarthage.chilla.controller;

import com.enicarthage.chilla.exception.DaoException;
import com.enicarthage.chilla.model.Etattach;
import com.enicarthage.chilla.model.Tache;
import com.enicarthage.chilla.service.TacheService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Locale;
import java.util.function.Supplier;

public class TacheController {

    private final TacheService tacheService;

    private boolean succes;
    private String message;
    private Object donnees;

    public TacheController() {
        this(new TacheService());
    }

    public TacheController(TacheService tacheService) {
        this.tacheService = tacheService;
    }


    public boolean isSucces() {
        return succes;
    }

    public String getMessage() {
        return message;
    }

    /** Type déduit de la variable  */
    @SuppressWarnings("unchecked")
    public <T> T getDonnees() {
        return (T) donnees;
    }

    // ---------- CREATION ----------
    // Donnees : la Tache créée 

    public boolean creerTache(int idProjet, int idSprint, int idMembreAssigne, String libelle,
            String description, String dateEcheance, int estimation, int position, int idActeur) {
        return executer(() -> tacheService.creerTache(
                construire(0, idProjet, idSprint, idMembreAssigne, libelle, description,
                        lireDate(dateEcheance), estimation, position),
                idActeur), "Tâche créée avec succès.");
    }

    public boolean creerTache(int idProjet, int idSprint, int idMembreAssigne, String libelle,
            String description, LocalDate dateEcheance, int estimation, int position, int idActeur) {
        return executer(() -> tacheService.creerTache(
                construire(0, idProjet, idSprint, idMembreAssigne, libelle, description,
                        dateEcheance, estimation, position),
                idActeur), "Tâche créée avec succès.");
    }

    // ---------- LECTURE ----------

    /** Donnees : Tache */
    public boolean consulterTache(int idTache) {
        return executer(() -> tacheService.getTache(idTache), "Tâche chargée.");
    }

    /** Donnees : List<Tache> */
    public boolean listerTachesDuProjet(int idProjet) {
        return executer(() -> tacheService.getTachesDuProjet(idProjet), "Tâches du projet chargées.");
    }

    /** Donnees : List<Tache> */
    public boolean listerTachesDuSprint(int idSprint) {
        return executer(() -> tacheService.getTachesDuSprint(idSprint), "Tâches du sprint chargées.");
    }

    /** Donnees : List<Tache> */
    public boolean listerTachesDuMembre(int idMembre) {
        return executer(() -> tacheService.getTachesDuMembre(idMembre), "Tâches du membre chargées.");
    }

    // ---------- MODIFICATION ----------
    // Donnees : la Tache modifiée 

    public boolean modifierTache(int idTache, int idSprint, int idMembreAssigne, String libelle,
            String description, String dateEcheance, int estimation, int position, int idActeur) {
        return executer(() -> tacheService.modifierTache(
                construire(idTache, 0, idSprint, idMembreAssigne, libelle, description,
                        lireDate(dateEcheance), estimation, position),
                idActeur), "Tâche modifiée avec succès.");
    }

    public boolean modifierTache(int idTache, int idSprint, int idMembreAssigne, String libelle,
            String description, LocalDate dateEcheance, int estimation, int position, int idActeur) {
        return executer(() -> tacheService.modifierTache(
                construire(idTache, 0, idSprint, idMembreAssigne, libelle, description,
                        dateEcheance, estimation, position),
                idActeur), "Tâche modifiée avec succès.");
    }

    public boolean changerEtat(int idTache, String etat, int idActeur) {
        return executerSansRetour(() -> tacheService.changerEtat(idTache, lireEtat(etat), idActeur),
                "État de la tâche modifié.");
    }

    public boolean changerEtat(int idTache, Etattach etat, int idActeur) {
        return executerSansRetour(() -> tacheService.changerEtat(idTache, etat, idActeur),
                "État de la tâche modifié.");
    }

    public boolean assignerTache(int idTache, int idNouveauMembre, int idActeur) {
        return executerSansRetour(() -> tacheService.assigner(idTache, idNouveauMembre, idActeur),
                "Tâche assignée.");
    }

    // ---------- SUPPRESSION ----------

    public boolean supprimerTache(int idTache, int idActeur) {
        return executerSansRetour(() -> tacheService.supprimerTache(idTache, idActeur), "Tâche supprimée.");
    }


    private Tache construire(int idTache, int idProjet, int idSprint, int idMembre, String libelle,
            String description, LocalDate dateEcheance, int estimation, int position) {
        Tache tache = new Tache();
        tache.setIDtach(idTache);
        tache.setIDpro(idProjet);
        tache.setIDsp(idSprint);
        tache.setIDmem(idMembre);
        tache.setLabtach(libelle == null ? null : libelle.trim());
        tache.setDescrtach(description == null || description.isBlank() ? null : description.trim());
        tache.setDateeche(dateEcheance);
        tache.setEstim(estimation);
        tache.setPos(position);
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

    // Exécute une action du service, mémorise le résultat et retourne true si elle a réussi
    private boolean executer(Supplier<?> action, String messageSucces) {
        try {
            donnees = action.get();
            succes = true;
            message = messageSucces;
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            echec(e.getMessage());
        } catch (DaoException e) {
            System.err.println("Erreur technique : " + e.getMessage());
            echec("Une erreur technique est survenue (base de données). Veuillez réessayer.");
        } catch (RuntimeException e) {
            System.err.println("Erreur inattendue : " + e);
            echec("Une erreur inattendue est survenue.");
        }
        return succes;
    }

    private boolean executerSansRetour(Runnable action, String messageSucces) {
        return executer(() -> {
            action.run();
            return null;
        }, messageSucces);
    }

    private void echec(String messageErreur) {
        succes = false;
        donnees = null;
        message = messageErreur;
    }
}