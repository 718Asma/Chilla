package com.enicarthage.chilla.dto;

import java.time.LocalDate;

/** Projet */
public record ProjetResponse(int id, String libelle, String description, LocalDate dateDebut, LocalDate dateFin, boolean archive) {
}
