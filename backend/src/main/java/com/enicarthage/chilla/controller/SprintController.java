package com.enicarthage.chilla.controller;

import com.enicarthage.chilla.exception.DaoException;
import com.enicarthage.chilla.model.Etatsp;
import com.enicarthage.chilla.model.Sprint;
import com.enicarthage.chilla.service.SprintService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Locale;
import java.util.function.Supplier;

public class SprintController {

    private final SprintService sprintService;

    private boolean succes;
    private String message;
    private Object donnees;

    public SprintController() {
        this(new SprintService());
    }

    public SprintController(SprintService sprintService) {
        this.sprintService = sprintService;
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
    // Donnees : le Sprint créé 

    public boolean creerSprint(int idProjet, String nom, String objectif,
            String dateDebut, String dateFin, int idMembre) {
        return executer(() -> sprintService.creerSprint(
                construire(0, idProjet, nom, objectif, lireDate(dateDebut, "début"), lireDate(dateFin, "fin")),
                idMembre), "Sprint créé avec succès.");
    }

    public boolean creerSprint(int idProjet, String nom, String objectif,
            LocalDate dateDebut, LocalDate dateFin, int idMembre) {
        return executer(() -> sprintService.creerSprint(
                construire(0, idProjet, nom, objectif, dateDebut, dateFin), idMembre),
                "Sprint créé avec succès.");
    }

    // ---------- LECTURE ----------

    /** Donnees : Sprint */
    public boolean consulterSprint(int idSprint) {
        return executer(() -> sprintService.getSprint(idSprint), "Sprint chargé.");
    }

    /** Donnees : List<Sprint> */
    public boolean listerSprintsDuProjet(int idProjet) {
        return executer(() -> sprintService.getSprintsDuProjet(idProjet), "Liste des sprints chargée.");
    }

    /** Donnees : le Sprint en cours, ou null s'il n'y en a pas. */
    public boolean consulterSprintEnCours(int idProjet) {
        boolean ok = executer(() -> sprintService.getSprintEnCours(idProjet).orElse(null), "Sprint en cours chargé.");
        if (ok && donnees == null) {
            message = "Aucun sprint en cours dans ce projet.";
        }
        return ok;
    }

    // ---------- MODIFICATION ----------
    // Donnees : le Sprint modifié 

    public boolean modifierSprint(int idSprint, String nom, String objectif,
            String dateDebut, String dateFin, int idMembre) {
        return executer(() -> sprintService.modifierSprint(
                construire(idSprint, 0, nom, objectif, lireDate(dateDebut, "début"), lireDate(dateFin, "fin")),
                idMembre), "Sprint modifié avec succès.");
    }

    public boolean modifierSprint(int idSprint, String nom, String objectif,
            LocalDate dateDebut, LocalDate dateFin, int idMembre) {
        return executer(() -> sprintService.modifierSprint(
                construire(idSprint, 0, nom, objectif, dateDebut, dateFin), idMembre),
                "Sprint modifié avec succès.");
    }

    public boolean changerEtat(int idSprint, String etat, int idMembre) {
        return executerSansRetour(() -> sprintService.changerEtat(idSprint, lireEtat(etat), idMembre),
                "État du sprint modifié.");
    }

    public boolean changerEtat(int idSprint, Etatsp etat, int idMembre) {
        return executerSansRetour(() -> sprintService.changerEtat(idSprint, etat, idMembre),
                "État du sprint modifié.");
    }

    public boolean demarrerSprint(int idSprint, int idMembre) {
        return executerSansRetour(() -> sprintService.changerEtat(idSprint, Etatsp.EN_COURS, idMembre),
                "Sprint démarré.");
    }

    public boolean terminerSprint(int idSprint, int idMembre) {
        return executerSansRetour(() -> sprintService.changerEtat(idSprint, Etatsp.TERMINE, idMembre),
                "Sprint terminé.");
    }

    public boolean annulerSprint(int idSprint, int idMembre) {
        return executerSansRetour(() -> sprintService.changerEtat(idSprint, Etatsp.ANNULE, idMembre),
                "Sprint annulé.");
    }

    // ---------- SUPPRESSION ----------

    public boolean supprimerSprint(int idSprint, int idMembre) {
        return executerSansRetour(() -> sprintService.supprimerSprint(idSprint, idMembre), "Sprint supprimé.");
    }


    private Sprint construire(int idSprint, int idProjet, String nom, String objectif,
            LocalDate dateDebut, LocalDate dateFin) {
        Sprint sprint = new Sprint();
        sprint.setIDsp(idSprint);
        sprint.setIDpro(idProjet);
        sprint.setNomsp(nom == null ? null : nom.trim());
        sprint.setObjectifsp(objectif == null || objectif.isBlank() ? null : objectif.trim());
        sprint.setDatedsp(dateDebut);
        sprint.setDatefsp(dateFin);
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