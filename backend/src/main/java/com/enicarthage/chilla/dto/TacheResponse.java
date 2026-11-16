package com.enicarthage.chilla.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.enicarthage.chilla.model.Etattach;

/** Tâche */
public record TacheResponse(int id, String libelle, String description, LocalDateTime dateCreation, LocalDate dateEcheance, int estimation, Etattach etat, int position, int idProjet, int idMembre, int idSprint) {
}
