package com.enicarthage.chilla.dto;

import com.enicarthage.chilla.model.Rolemem;

/** Appartenance d'un membre à un projet */
public record MembreProjetResponse(int idMembre, int idProjet, Rolemem role) {
}
