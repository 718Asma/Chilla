package com.enicarthage.chilla.controller;

import com.enicarthage.chilla.exception.DaoException;
import com.enicarthage.chilla.model.Rolemem;
import com.enicarthage.chilla.service.Membre_ProjetService;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Supplier;

public class Membre_ProjetController {

    private final Membre_ProjetService membreProjetService;

    private boolean succes;
    private String message;
    private Object donnees;

    public Membre_ProjetController() {
        this(new Membre_ProjetService());
    }

    public Membre_ProjetController(Membre_ProjetService membreProjetService) {
        this.membreProjetService = membreProjetService;
    }


    public boolean isSucces() {
        return succes;
    }

    public String getMessage() {
        return message;
    }

    /** Type déduit de la variable */
    @SuppressWarnings("unchecked")
    public <T> T getDonnees() {
        return (T) donnees;
    }

    // ---------- LECTURE ----------

    /** Donnees : List<Membre_Projet> */
    public boolean listerMembresDuProjet(int idProjet) {
        return executer(() -> membreProjetService.getMembresDuProjet(idProjet), "Équipe du projet chargée.");
    }

    /** Donnees : le Rolemem du membre dans le projet. */
    public boolean consulterRole(int idProjet, int idMembre) {
        return executer(() -> membreProjetService.getRole(idProjet, idMembre), "Rôle chargé.");
    }

    // ---------- MODIFICATION ----------

    public boolean changerRole(int idProjet, int idMembreCible, String role, int idActeur) {
        return executerSansRetour(
                () -> membreProjetService.changerRole(idProjet, idMembreCible, lireRole(role), idActeur),
                "Rôle modifié.");
    }

    public boolean changerRole(int idProjet, int idMembreCible, Rolemem role, int idActeur) {
        return executerSansRetour(
                () -> membreProjetService.changerRole(idProjet, idMembreCible, role, idActeur),
                "Rôle modifié.");
    }

    // ---------- RETRAIT ----------

    /** Le chef retire un membre du projet. */
    public boolean retirerMembre(int idProjet, int idMembreCible, int idActeur) {
        return executerSansRetour(() -> membreProjetService.retirerMembre(idProjet, idMembreCible, idActeur),
                "Membre retiré du projet.");
    }

    /** Le membre connecté quitte lui-même le projet. */
    public boolean quitterProjet(int idProjet, int idMembre) {
        return executerSansRetour(() -> membreProjetService.retirerMembre(idProjet, idMembre, idMembre),
                "Vous avez quitté le projet.");
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