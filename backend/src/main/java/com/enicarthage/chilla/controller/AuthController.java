package com.enicarthage.chilla.controller;

import com.enicarthage.chilla.dto.ConnexionRequest;
import com.enicarthage.chilla.dto.DtoMapper;
import com.enicarthage.chilla.dto.InscriptionRequest;
import com.enicarthage.chilla.dto.MembreResponse;
import com.enicarthage.chilla.dto.SessionResponse;
import com.enicarthage.chilla.exception.AuthentificationException;
import com.enicarthage.chilla.model.Membre;
import com.enicarthage.chilla.service.AuthService;

/**
 * Controller sans état : il reçoit un DTO de requête, appelle le service et renvoie un DTO de réponse.
 * Les exceptions ne sont pas interceptées ici : le Router les convertit en codes HTTP.
 */
public class AuthController {

    private final AuthService authService;

    public AuthController() {
        this(new AuthService());
    }

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** Crée le compte et connecte directement le nouveau membre (token + membre). */
    public SessionResponse inscrire(InscriptionRequest requete) {
        Membre membre = new Membre();
        membre.setNommem(nettoyer(requete.nom()));
        membre.setPrenommem(nettoyer(requete.prenom()));
        membre.setEmailmem(nettoyer(requete.email()));
        membre.setMotdepassemem(requete.motDePasse()); // en clair : haché par le service
        return DtoMapper.vers(authService.inscrire(membre));
    }

    /** Identifiants incorrects -> AuthentificationException (401). */
    public SessionResponse connecter(ConnexionRequest requete) {
        try {
            return DtoMapper.vers(authService.connecter(requete.email(), requete.motDePasse()));
        } catch (IllegalArgumentException e) {
            throw new AuthentificationException(e.getMessage());
        }
    }

    /** Le membre propriétaire du jeton. */
    public MembreResponse consulterMembreCourant(int idMembre) {
        return DtoMapper.vers(authService.getMembreCourant(idMembre));
    }

    private String nettoyer(String texte) {
        return texte == null ? null : texte.trim();
    }
}
