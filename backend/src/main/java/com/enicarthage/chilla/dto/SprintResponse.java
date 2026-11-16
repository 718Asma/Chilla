package com.enicarthage.chilla.dto;

import java.time.LocalDate;

import com.enicarthage.chilla.model.Etatsp;

/** Sprint */
public record SprintResponse(int id, String nom, String objectif, LocalDate dateDebut, LocalDate dateFin, Etatsp etat, int idProjet) {
}
