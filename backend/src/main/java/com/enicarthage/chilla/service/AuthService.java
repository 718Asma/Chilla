package com.enicarthage.chilla.service;

import com.enicarthage.chilla.exception.AuthentificationException;
import com.enicarthage.chilla.model.Membre;

public class AuthService {

    private static final String PREFIXE_BEARER = "Bearer ";

    /** Ce que reçoit le client après inscription ou connexion. */
    public record Session(String token, Membre membre) {
    }

    private final MembreService membreService;
    private final JwtService jwtService;

    public AuthService() {
        this(new MembreService(), new JwtService());
    }

    // Constructeur avec injection : pratique pour les tests
    public AuthService(MembreService membreService, JwtService jwtService) {
        this.membreService = membreService;
        this.jwtService = jwtService;
    }

    // ---------- INSCRIPTION / CONNEXION ----------

    /** Crée le compte puis connecte directement le nouveau membre. */
    public Session inscrire(Membre membre) {
        Membre cree = membreService.inscrire(membre); // mot de passe déjà vidé par le service
        return new Session(jwtService.generer(cree.getIDmem()), cree);
    }

    /** Vérifie les identifiants et retourne un jeton + le membre (sans mot de passe). */
    public Session connecter(String email, String motDePasse) {
        Membre membre = membreService.authentifier(email, motDePasse);
        membre.setMotdepassemem("");
        return new Session(jwtService.generer(membre.getIDmem()), membre);
    }

    /** Le membre correspondant au jeton (pour restaurer la session côté frontend). */
    public Membre getMembreCourant(int idMembre) {
        Membre membre = membreService.getMembre(idMembre);
        membre.setMotdepassemem("");
        return membre;
    }

    // ---------- VERIFICATION DU JETON ----------

    /**
     * Extrait l'id du membre depuis l'en-tête "Authorization: Bearer <jeton>".
     * Lève AuthentificationException si l'en-tête est absent, mal formé, invalide ou expiré.
     */
    public int identifier(String enteteAuthorization) {
        if (enteteAuthorization == null
                || !enteteAuthorization.regionMatches(true, 0, PREFIXE_BEARER, 0, PREFIXE_BEARER.length())) {
            throw new AuthentificationException(
                    "Authentification requise : en-tête « Authorization: Bearer <token> » manquant.");
        }
        return jwtService.verifier(enteteAuthorization.substring(PREFIXE_BEARER.length()).trim());
    }
}