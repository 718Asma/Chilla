package com.enicarthage.chilla.dto;

/** POST /projets/{id}/taches, PUT /taches/{id} */
public record TacheRequest(int idSprint, int idMembreAssigne, String libelle, String description, String dateEcheance, int estimation, int position) {
}
