package com.enicarthage.chilla.controller;

import com.enicarthage.chilla.exception.DaoException;
import com.enicarthage.chilla.model.Membre;
import com.enicarthage.chilla.service.MembreService;

import java.util.List;
import java.util.function.Supplier;

public class MembreController {

    private final MembreService membreService;

    private boolean succes;
    private String message;
    private Object donnees;

    public MembreController() {
        this(new MembreService());
    }

    public MembreController(MembreService membreService) {
        this.membreService = membreService;
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

    // ---------- INSCRIPTION / CONNEXION ----------

    /** Donnees : le Membre créé  */
    public boolean inscrire(String nom, String prenom, String email, String motDePasse) {
        return executer(() -> {
            Membre membre = new Membre();
            membre.setNommem(nom == null ? null : nom.trim());
            membre.setPrenommem(prenom == null ? null : prenom.trim());
            membre.setEmailmem(email == null ? null : email.trim());
            membre.setMotdepassemem(motDePasse); // en clair : haché par le service
            return membreService.inscrire(membre);
        }, "Inscription réussie.");
    }

    /** Donnees : le Membre connecté. */
    public boolean connecter(String email, String motDePasse) {
        return executer(() -> masquer(membreService.authentifier(email, motDePasse)), "Connexion réussie.");
    }

    // ---------- LECTURE ----------

    /** Donnees : List<Membre> */
    public boolean listerTousLesMembres() {
        return executer(() -> {
            List<Membre> membres = membreService.getTousLesMembres();
            membres.forEach(this::masquer);
            return membres;
        }, "Liste des membres chargée.");
    }

    /** Donnees : Membre */
    public boolean consulterMembre(int idMembre) {
        return executer(() -> masquer(membreService.getMembre(idMembre)), "Membre chargé.");
    }

    // ---------- MODIFICATION ----------

    /** Donnees : le Membre modifié. */
    public boolean modifierProfil(int idMembre, String nom, String prenom, String email) {
        return executer(() -> {
            Membre membre = new Membre();
            membre.setIDmem(idMembre);
            membre.setNommem(nom == null ? null : nom.trim());
            membre.setPrenommem(prenom == null ? null : prenom.trim());
            membre.setEmailmem(email == null ? null : email.trim());
            return membreService.modifierProfil(membre);
        }, "Profil modifié avec succès.");
    }

    public boolean changerMotDePasse(int idMembre, String ancienMotDePasse, String nouveauMotDePasse) {
        return executerSansRetour(
                () -> membreService.changerMotDePasse(idMembre, ancienMotDePasse, nouveauMotDePasse),
                "Mot de passe modifié avec succès.");
    }

    // ---------- SUPPRESSION ----------

    public boolean supprimerCompte(int idMembre, String motDePasse) {
        return executerSansRetour(() -> membreService.supprimerCompte(idMembre, motDePasse),
                "Compte supprimé.");
    }


    private Membre masquer(Membre membre) {
        membre.setMotdepassemem("");
        return membre;
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