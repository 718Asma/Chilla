package com.enicarthage.chilla.service;

import com.enicarthage.chilla.dao.Membre_ProjetDAO;
import com.enicarthage.chilla.dao.ProjetDAO;
import com.enicarthage.chilla.dao.SprintDAO;
import com.enicarthage.chilla.model.Etatsp;
import com.enicarthage.chilla.model.Membre_Projet;
import com.enicarthage.chilla.model.Projet;
import com.enicarthage.chilla.model.Rolemem;
import com.enicarthage.chilla.model.Sprint;

import java.util.List;
import java.util.Optional;

public class SprintService {

    private static final int MAX_LONGUEUR = 255;

    private final SprintDAO sprintDAO;
    private final ProjetDAO projetDAO;
    private final Membre_ProjetDAO membreProjetDAO;

    public SprintService() {
        this(new SprintDAO(), new ProjetDAO(), new Membre_ProjetDAO());
    }

    public SprintService(SprintDAO sprintDAO, ProjetDAO projetDAO, Membre_ProjetDAO membreProjetDAO) {
        this.sprintDAO = sprintDAO;
        this.projetDAO = projetDAO;
        this.membreProjetDAO = membreProjetDAO;
    }

    // ---------- CREATION ----------

    /** Crée un sprint dans un projet. Remplit l'id généré. */
    public Sprint creerSprint(Sprint sprint, int idMembre) {
        if (sprint == null) {
            throw new IllegalArgumentException("Le sprint est obligatoire.");
        }
        Projet projet = getProjetActif(sprint.getIDpro());
        verifierChef(projet.getIDpro(), idMembre);
        valider(sprint, projet);

        sprint.setEtat(Etatsp.NON_COMMENCE);
        sprintDAO.create(sprint);
        return sprint;
    }

    // ---------- LECTURE ----------

    public Sprint getSprint(int idSprint) {
        return sprintDAO.findById(idSprint)
                .orElseThrow(() -> new IllegalArgumentException("Sprint introuvable : " + idSprint));
    }

    public List<Sprint> getSprintsDuProjet(int idProjet) {
        return sprintDAO.findByProjet(idProjet);
    }

    /** Le sprint EN_COURS du projet, s'il y en a un. */
    public Optional<Sprint> getSprintEnCours(int idProjet) {
        return sprintDAO.findByProjet(idProjet).stream()
                .filter(s -> s.getEtat() == Etatsp.EN_COURS)
                .findFirst();
    }

    // ---------- MODIFICATION ----------

    /** Modifie nom, objectif et dates. L'état et le projet ne changent pas par cette méthode. */
    public Sprint modifierSprint(Sprint sprint, int idMembre) {
        Sprint existant = getSprint(sprint.getIDsp());
        Projet projet = getProjetActif(existant.getIDpro());
        verifierChef(projet.getIDpro(), idMembre);
        if (existant.getEtat() == Etatsp.TERMINE || existant.getEtat() == Etatsp.ANNULE) {
            throw new IllegalStateException("Un sprint terminé ou annulé ne peut plus être modifié.");
        }

        sprint.setIDpro(existant.getIDpro());
        sprint.setEtat(existant.getEtat());
        valider(sprint, projet);

        sprintDAO.update(sprint);
        return sprint;
    }

    /** Change l'état d'un sprint en respectant le cycle de vie. */
    public void changerEtat(int idSprint, Etatsp nouvelEtat, int idMembre) {
        Sprint sprint = getSprint(idSprint);
        getProjetActif(sprint.getIDpro());
        verifierChef(sprint.getIDpro(), idMembre);

        if (nouvelEtat == null || !transitionAutorisee(sprint.getEtat(), nouvelEtat)) {
            throw new IllegalStateException(
                    "Transition impossible : " + sprint.getEtat() + " -> " + nouvelEtat);
        }
        if (nouvelEtat == Etatsp.EN_COURS) {
            Optional<Sprint> enCours = getSprintEnCours(sprint.getIDpro());
            if (enCours.isPresent()) {
                throw new IllegalStateException(
                        "Le sprint « " + enCours.get().getNomsp() + " » est déjà en cours dans ce projet.");
            }
        }

        sprint.setEtat(nouvelEtat);
        sprintDAO.update(sprint);
    }

    // ---------- SUPPRESSION ----------

    /** Supprime un sprint et, en cascade, ses tâches. */
    public void supprimerSprint(int idSprint, int idMembre) {
        Sprint sprint = getSprint(idSprint);
        verifierChef(sprint.getIDpro(), idMembre);
        sprintDAO.delete(idSprint);
    }


    private boolean transitionAutorisee(Etatsp de, Etatsp vers) {
        switch (de) {
            case NON_COMMENCE:
                return vers == Etatsp.EN_COURS || vers == Etatsp.ANNULE;
            case EN_COURS:
                return vers == Etatsp.TERMINE || vers == Etatsp.ANNULE;
            default:
                return false; // TERMINE et ANNULE sont définitifs
        }
    }

    private Projet getProjetActif(int idProjet) {
        Projet projet = projetDAO.findById(idProjet)
                .orElseThrow(() -> new IllegalArgumentException("Projet introuvable : " + idProjet));
        if (projet.getEstarchive()) {
            throw new IllegalStateException("Le projet est archivé : aucune modification n'est possible.");
        }
        return projet;
    }

    private void verifierChef(int idProjet, int idMembre) {
        Optional<Membre_Projet> mp = membreProjetDAO.findById(idMembre, idProjet);
        if (mp.isEmpty() || mp.get().getRolemem() != Rolemem.CHEF_DE_PROJET) {
            throw new SecurityException("Seul le chef de projet peut effectuer cette action.");
        }
    }

    // Règles alignées sur le schéma SQL ; les dates doivent rester dans celles du projet
    private void valider(Sprint sprint, Projet projet) {
        if (sprint.getNomsp() == null || sprint.getNomsp().isBlank()) {
            throw new IllegalArgumentException("Le nom du sprint est obligatoire.");
        }
        if (sprint.getNomsp().length() > MAX_LONGUEUR) {
            throw new IllegalArgumentException("Le nom ne doit pas dépasser " + MAX_LONGUEUR + " caractères.");
        }
        if (sprint.getObjectifsp() != null && sprint.getObjectifsp().length() > MAX_LONGUEUR) {
            throw new IllegalArgumentException("L'objectif ne doit pas dépasser " + MAX_LONGUEUR + " caractères.");
        }
        if (sprint.getDatedsp() == null || sprint.getDatefsp() == null) {
            throw new IllegalArgumentException("Les dates de début et de fin sont obligatoires.");
        }
        if (sprint.getDatefsp().isBefore(sprint.getDatedsp())) {
            throw new IllegalArgumentException("La date de fin ne peut pas précéder la date de début.");
        }
        if (sprint.getDatedsp().isBefore(projet.getDatedpro()) || sprint.getDatefsp().isAfter(projet.getDatefpro())) {
            throw new IllegalArgumentException("Le sprint doit rester dans les dates du projet ("
                    + projet.getDatedpro() + " -> " + projet.getDatefpro() + ").");
        }
    }
}