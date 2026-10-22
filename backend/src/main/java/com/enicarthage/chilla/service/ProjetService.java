package com.enicarthage.chilla.service;

import com.enicarthage.chilla.dao.Membre_ProjetDAO;
import com.enicarthage.chilla.dao.ProjetDAO;
import com.enicarthage.chilla.model.Membre_Projet;
import com.enicarthage.chilla.model.Projet;
import com.enicarthage.chilla.model.Rolemem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProjetService {

    private static final int MAX_LONGUEUR = 255; // taille des VARCHAR(255) en base

    private final ProjetDAO projetDAO;
    private final Membre_ProjetDAO membreProjetDAO;

    public ProjetService() {
        this(new ProjetDAO(), new Membre_ProjetDAO());
    }

    // Constructeur avec injection : pratique pour les tests
    public ProjetService(ProjetDAO projetDAO, Membre_ProjetDAO membreProjetDAO) {
        this.projetDAO = projetDAO;
        this.membreProjetDAO = membreProjetDAO;
    }

    // ---------- CREATION ----------

    /**
     * Crée un projet et ajoute son créateur comme CHEF_DE_PROJET.
     * Remplit l'id généré dans l'objet projet.
     */
    public Projet creerProjet(Projet projet, int idCreateur) {
        valider(projet);
        projet.setEstarchive(false);

        projetDAO.create(projet);

        Membre_Projet chef = new Membre_Projet();
        chef.setIDmem(idCreateur);
        chef.setIDpro(projet.getIDpro());
        chef.setRolemem(Rolemem.CHEF_DE_PROJET);

        try {
            membreProjetDAO.create(chef);
        } catch (RuntimeException e) {
            projetDAO.delete(projet.getIDpro());
            throw e;
        }
        return projet;
    }

    // ---------- LECTURE ----------

    public List<Projet> getTousLesProjets() {
        return projetDAO.findAll();
    }

    public Projet getProjet(int idProjet) {
        return projetDAO.findById(idProjet)
                .orElseThrow(() -> new IllegalArgumentException("Projet introuvable : " + idProjet));
    }

    /** Tous les projets auxquels participe un membre (archivés ou non). */
    public List<Projet> getProjetsDuMembre(int idMembre) {
        List<Projet> projets = new ArrayList<>();
        for (Membre_Projet mp : membreProjetDAO.findByMembre(idMembre)) {
            projetDAO.findById(mp.getIDpro()).ifPresent(projets::add);
        }
        return projets;
    }

    /** Uniquement les projets non archivés d'un membre. */
    public List<Projet> getProjetsActifsDuMembre(int idMembre) {
        List<Projet> actifs = new ArrayList<>();
        for (Projet p : getProjetsDuMembre(idMembre)) {
            if (!p.getEstarchive()) {
                actifs.add(p);
            }
        }
        return actifs;
    }

    // ---------- MODIFICATION ----------

    /**
     * Modifie un projet. Réservé au chef de projet, interdit si le projet est
     * archivé.
     */
    public Projet modifierProjet(Projet projet, int idMembre) {
        Projet existant = getProjet(projet.getIDpro());
        verifierChefDeProjet(existant.getIDpro(), idMembre);
        verifierNonArchive(existant);
        valider(projet);

        projet.setEstarchive(existant.getEstarchive());
        projetDAO.update(projet);
        return projet;
    }

    public void archiverProjet(int idProjet, int idMembre) {
        changerArchivage(idProjet, idMembre, true);
    }

    public void desarchiverProjet(int idProjet, int idMembre) {
        changerArchivage(idProjet, idMembre, false);
    }

    // ---------- SUPPRESSION ----------

    /**
     * Supprime définitivement un projet (sprints, tâches, invitations et
     * appartenances sont supprimés en cascade par la base). Réservé au chef de
     * projet.
     */
    public void supprimerProjet(int idProjet, int idMembre) {
        getProjet(idProjet);
        verifierChefDeProjet(idProjet, idMembre);
        projetDAO.delete(idProjet);
    }

    private void changerArchivage(int idProjet, int idMembre, boolean archive) {
        Projet projet = getProjet(idProjet);
        verifierChefDeProjet(idProjet, idMembre);
        if (projet.getEstarchive() == archive) {
            return;
        }
        projet.setEstarchive(archive);
        projetDAO.update(projet);
    }

    private void verifierChefDeProjet(int idProjet, int idMembre) {
        Optional<Membre_Projet> mp = membreProjetDAO.findById(idMembre, idProjet);
        if (mp.isEmpty() || mp.get().getRolemem() != Rolemem.CHEF_DE_PROJET) {
            throw new SecurityException("Seul le chef de projet peut effectuer cette action.");
        }
    }

    private void verifierNonArchive(Projet projet) {
        if (projet.getEstarchive()) {
            throw new IllegalStateException("Le projet est archivé : désarchivez-le avant de le modifier.");
        }
    }

    // Règles alignées sur le schéma SQL (NOT NULL, VARCHAR(255))
    private void valider(Projet projet) {
        if (projet == null) {
            throw new IllegalArgumentException("Le projet est obligatoire.");
        }
        if (projet.getLabpro() == null || projet.getLabpro().isBlank()) {
            throw new IllegalArgumentException("Le libellé du projet est obligatoire.");
        }
        if (projet.getLabpro().length() > MAX_LONGUEUR) {
            throw new IllegalArgumentException("Le libellé ne doit pas dépasser " + MAX_LONGUEUR + " caractères.");
        }
        if (projet.getDescrpro() != null && projet.getDescrpro().length() > MAX_LONGUEUR) {
            throw new IllegalArgumentException("La description ne doit pas dépasser " + MAX_LONGUEUR + " caractères.");
        }
        if (projet.getDatedpro() == null || projet.getDatefpro() == null) {
            throw new IllegalArgumentException("Les dates de début et de fin sont obligatoires.");
        }
        if (projet.getDatefpro().isBefore(projet.getDatedpro())) {
            throw new IllegalArgumentException("La date de fin ne peut pas précéder la date de début.");
        }
    }
}