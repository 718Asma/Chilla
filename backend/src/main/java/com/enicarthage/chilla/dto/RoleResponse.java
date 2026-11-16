package com.enicarthage.chilla.dto;

import com.enicarthage.chilla.model.Rolemem;

/** GET /projets/{id}/membres/{idMembre}/role */
public record RoleResponse(Rolemem role) {
}
