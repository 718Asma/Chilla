package com.enicarthage.chilla.dto;

/** POST /projets/{id}/sprints, PUT /sprints/{id} (dates au format aaaa-mm-jj) */
public record SprintRequest(String nom, String objectif, String dateDebut, String dateFin) {
}
