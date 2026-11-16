package com.enicarthage.chilla.dto;

/** Réponse de /auth/inscription et /auth/connexion */
public record SessionResponse(String token, MembreResponse membre) {
}
