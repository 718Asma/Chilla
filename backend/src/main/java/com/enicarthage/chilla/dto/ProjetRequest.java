package com.enicarthage.chilla.dto;

/** POST /projets, PUT /projets/{id} (dates au format aaaa-mm-jj) */
public record ProjetRequest(String libelle, String description, String dateDebut, String dateFin) {
}
