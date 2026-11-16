package com.enicarthage.chilla.dto;

/** PUT /membres/{id}/mot-de-passe */
public record MotDePasseRequest(String ancienMotDePasse, String nouveauMotDePasse) {
}
