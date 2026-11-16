package com.enicarthage.chilla.dto;

/** POST /auth/connexion */
public record ConnexionRequest(String email, String motDePasse) {
}
