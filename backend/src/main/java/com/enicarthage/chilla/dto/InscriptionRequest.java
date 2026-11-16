package com.enicarthage.chilla.dto;

/** POST /auth/inscription */
public record InscriptionRequest(String nom, String prenom, String email, String motDePasse) {
}
