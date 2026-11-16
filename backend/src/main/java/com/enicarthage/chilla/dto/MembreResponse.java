package com.enicarthage.chilla.dto;

import java.time.LocalDateTime;

/** Membre sans mot de passe */
public record MembreResponse(int id, String nom, String prenom, String email, LocalDateTime dateCreation) {
}
