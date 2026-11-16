package com.enicarthage.chilla.controller;

import com.enicarthage.chilla.exception.DaoException;
import com.enicarthage.chilla.model.Membre;
import com.enicarthage.chilla.service.AuthService;

import java.util.function.Supplier;

public class AuthController {

    private final AuthService authService;

    private boolean succes;
    private String message;
    private Object donnees;

    public AuthController() {
        this(new AuthService());
    }

    public AuthController(AuthService authService) {
        this.authService = authService;
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

    // ---------- INSCRIPTION / CONNEXION ----------

    /** Donnees : AuthService.Session (token + membre créé) */
    public boolean inscrire(String nom, String prenom, String email, String motDePasse) {
        return executer(() -> {
            Membre membre = new Membre();
            membre.setNommem(nom == null ? null : nom.trim());
            membre.setPrenommem(prenom == null ? null : prenom.trim());
            membre.setEmailmem(email == null ? null : email.trim());
            membre.setMotdepassemem(motDePasse); // en clair : haché par le service
            return authService.inscrire(membre);
        }, "Inscription réussie.");
    }

    /** Donnees : AuthService.Session (token + membre connecté) */
    public boolean connecter(String email, String motDePasse) {
        return executer(() -> authService.connecter(email, motDePasse), "Connexion réussie.");
    }

    // ---------- SESSION COURANTE ----------

    /** Donnees : le Membre propriétaire du jeton */
    public boolean consulterMembreCourant(int idMembre) {
        return executer(() -> authService.getMembreCourant(idMembre), "Membre connecté chargé.");
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

    private void echec(String messageErreur) {
        succes = false;
        donnees = null;
        message = messageErreur;
    }
}