package com.enicarthage.chilla.controller;

import com.enicarthage.chilla.exception.DaoException;
import com.enicarthage.chilla.model.Projet;
import com.enicarthage.chilla.service.ProjetService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.function.Supplier;

public class ProjetController {

    private final ProjetService projetService;

    private boolean succes;
    private String message;
    private Object donnees;

    public ProjetController() {
        this(new ProjetService());
    }

    public ProjetController(ProjetService projetService) {
        this.projetService = projetService;
    }


    public boolean isSucces() {
        return succes;
    }

    public String getMessage() {
        return message;
    }


    @SuppressWarnings("unchecked")
    public <T> T getDonnees() {
        return (T) donnees;
    }

    // ---------- CREATION ----------
    // Donnees : le Projet créé

    public boolean creerProjet(String libelle, String description,
            String dateDebut, String dateFin, int idCreateur) {
        return executer(() -> {
            Projet projet = construire(0, libelle, description, lireDate(dateDebut, "début"), lireDate(dateFin, "fin"));
            return projetService.creerProjet(projet, idCreateur);
        }, "Projet créé avec succès.");
    }

    public boolean creerProjet(String libelle, String description,
            LocalDate dateDebut, LocalDate dateFin, int idCreateur) {
        return executer(() -> projetService.creerProjet(
                construire(0, libelle, description, dateDebut, dateFin), idCreateur),
                "Projet créé avec succès.");
    }

    // ---------- LECTURE ----------

    /** Donnees : List<Projet> */
    public boolean listerTousLesProjets() {
        return executer(projetService::getTousLesProjets, "Liste des projets chargée.");
    }

    /** Donnees : List<Projet> */
    public boolean listerProjetsDuMembre(int idMembre) {
        return executer(() -> projetService.getProjetsDuMembre(idMembre), "Liste de vos projets chargée.");
    }

    /** Donnees : List<Projet> */
    public boolean listerProjetsActifsDuMembre(int idMembre) {
        return executer(() -> projetService.getProjetsActifsDuMembre(idMembre), "Liste de vos projets actifs chargée.");
    }

    /** Donnees : Projet */
    public boolean consulterProjet(int idProjet) {
        return executer(() -> projetService.getProjet(idProjet), "Projet chargé.");
    }

    // ---------- MODIFICATION ----------
    // Donnees : le Projet modifié

    public boolean modifierProjet(int idProjet, String libelle, String description,
            String dateDebut, String dateFin, int idMembre) {
        return executer(() -> {
            Projet projet = construire(idProjet, libelle, description,
                    lireDate(dateDebut, "début"), lireDate(dateFin, "fin"));
            return projetService.modifierProjet(projet, idMembre);
        }, "Projet modifié avec succès.");
    }

    public boolean modifierProjet(int idProjet, String libelle, String description,
            LocalDate dateDebut, LocalDate dateFin, int idMembre) {
        return executer(() -> projetService.modifierProjet(
                construire(idProjet, libelle, description, dateDebut, dateFin), idMembre),
                "Projet modifié avec succès.");
    }

    public boolean archiverProjet(int idProjet, int idMembre) {
        return executerSansRetour(() -> projetService.archiverProjet(idProjet, idMembre), "Projet archivé.");
    }

    public boolean desarchiverProjet(int idProjet, int idMembre) {
        return executerSansRetour(() -> projetService.desarchiverProjet(idProjet, idMembre), "Projet désarchivé.");
    }

    // ---------- SUPPRESSION ----------

    public boolean supprimerProjet(int idProjet, int idMembre) {
        return executerSansRetour(() -> projetService.supprimerProjet(idProjet, idMembre), "Projet supprimé.");
    }


    // Nettoie les saisies : espaces retirés, description vide -> null
    private Projet construire(int idProjet, String libelle, String description,
            LocalDate dateDebut, LocalDate dateFin) {
        Projet projet = new Projet();
        projet.setIDpro(idProjet);
        projet.setLabpro(libelle == null ? null : libelle.trim());
        projet.setDescrpro(description == null || description.isBlank() ? null : description.trim());
        projet.setDatedpro(dateDebut);
        projet.setDatefpro(dateFin);
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