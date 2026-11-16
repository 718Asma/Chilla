package com.enicarthage.chilla.dto;

/** PUT /membres/{id} */
public record ProfilRequest(String nom, String prenom, String email) {
}
