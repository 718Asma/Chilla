package com.enicarthage.chilla.service;

import com.enicarthage.chilla.dao.MembreDAO;
import com.enicarthage.chilla.dao.Membre_ProjetDAO;
import com.enicarthage.chilla.dao.ProjetDAO;
import com.enicarthage.chilla.dao.TacheDAO;
import com.enicarthage.chilla.model.Etattach;
import com.enicarthage.chilla.model.Membre_Projet;
import com.enicarthage.chilla.model.Rolemem;
import com.enicarthage.chilla.model.Tache;

import java.util.List;

public class Membre_ProjetService {

    private final Membre_ProjetDAO membreProjetDAO;
    private final ProjetDAO projetDAO;
    private final MembreDAO membreDAO;
    private final TacheDAO tacheDAO;

    public Membre_ProjetService() {
        this(new Membre_ProjetDAO(), new ProjetDAO(), new MembreDAO(), new TacheDAO());
    }

    public Membre_ProjetService(Membre_ProjetDAO membreProjetDAO, ProjetDAO projetDAO,
            MembreDAO membreDAO, TacheDAO tacheDAO) {
        this.membreProjetDAO = membreProjetDAO;
        this.projetDAO = projetDAO;
        this.membreDAO = membreDAO;
        this.tacheDAO = tacheDAO;
    }

    // ---------- LECTURE ----------

    public List<Membre_Projet> getMembresDuProjet(int idProjet) {
        verifierProjetExiste(idProjet);
        return membreProjetDAO.findByProjet(idProjet);
    }

    public Rolemem getRole(int idProjet, int idMembre) {
        return membreProjetDAO.findById(idMembre, idProjet)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Le membre " + idMembre + " ne fait pas partie du projet " + idProjet))
                .getRolemem();
    }

    public boolean estMembre(int idProjet, int idMembre) {
        return membreProjetDAO.findById(idMembre, idProjet).isPresent();
    }

    public boolean estChef(int idProjet, int idMembre) {
        return membreProjetDAO.findById(idMembre, idProjet)
                .map(mp -> mp.getRolemem() == Rolemem.CHEF_DE_PROJET)
                .orElse(false);
    }

    // ---------- MODIFICATION ----------

    public void changerRole(int idProjet, int idMembreCible, Rolemem nouveauRole, int idActeur) {
        verifierProjetExiste(idProjet);
        verifierChef(idProjet, idActeur);
        if (nouveauRole == null) {
            throw new IllegalArgumentException("Le rôle est obligatoire.");
        }

        Membre_Projet cible = membreProjetDAO.findById(idMembreCible, idProjet)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Le membre " + idMembreCible + " ne fait pas partie du projet."));
        if (cible.getRolemem() == nouveauRole) {
            return;
        }
        if (cible.getRolemem() == Rolemem.CHEF_DE_PROJET && nombreDeChefs(idProjet) <= 1) {
            throw new IllegalStateException(
                    "Le projet doit garder au moins un chef : nommez d'abord un autre chef.");
        }

        cible.setRolemem(nouveauRole);
        membreProjetDAO.update(cible);
    }

    // ---------- RETRAIT ----------

    public void retirerMembre(int idProjet, int idMembreCible, int idActeur) {
        verifierProjetExiste(idProjet);
        if (idActeur != idMembreCible) {
            verifierChef(idProjet, idActeur);
        }

        Membre_Projet cible = membreProjetDAO.findById(idMembreCible, idProjet)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Le membre " + idMembreCible + " ne fait pas partie du projet."));

        if (cible.getRolemem() == Rolemem.CHEF_DE_PROJET && nombreDeChefs(idProjet) <= 1) {
            throw new IllegalStateException(
                    "Impossible de retirer le seul chef du projet : nommez d'abord un autre chef.");
        }
        for (Tache t : tacheDAO.findByMembre(idMembreCible)) {
            if (t.getIDpro() == idProjet && t.getEtat() != Etattach.TERMINE && t.getEtat() != Etattach.ANNULE) {
                throw new IllegalStateException(
                        "Ce membre a encore des tâches en cours dans le projet : réassignez-les d'abord.");
            }
        }
        membreProjetDAO.delete(idMembreCible, idProjet);
    }


    private long nombreDeChefs(int idProjet) {
        return membreProjetDAO.findByProjet(idProjet).stream()
                .filter(mp -> mp.getRolemem() == Rolemem.CHEF_DE_PROJET)
                .count();
    }

    private void verifierProjetExiste(int idProjet) {
        if (projetDAO.findById(idProjet).isEmpty()) {
            throw new IllegalArgumentException("Projet introuvable : " + idProjet);
        }
    }

    private void verifierChef(int idProjet, int idMembre) {
        if (!estChef(idProjet, idMembre)) {
            throw new SecurityException("Seul le chef de projet peut effectuer cette action.");
        }
    }
}