package com.enicarthage.chilla.service;

import com.enicarthage.chilla.dao.MembreDAO;
import com.enicarthage.chilla.dao.Membre_ProjetDAO;
import com.enicarthage.chilla.dao.TacheDAO;
import com.enicarthage.chilla.model.Membre;
import com.enicarthage.chilla.model.Membre_Projet;
import com.enicarthage.chilla.model.Rolemem;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class MembreService {

    private static final int MAX_LONGUEUR = 255;
    private static final int MIN_MOT_DE_PASSE = 8;
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private static final int ITERATIONS = 120_000;
    private static final int TAILLE_CLE_BITS = 256;
    private static final int TAILLE_SEL_OCTETS = 16;

    private final MembreDAO membreDAO;
    private final Membre_ProjetDAO membreProjetDAO;
    private final TacheDAO tacheDAO;

    public MembreService() {
        this(new MembreDAO(), new Membre_ProjetDAO(), new TacheDAO());
    }

    public MembreService(MembreDAO membreDAO, Membre_ProjetDAO membreProjetDAO, TacheDAO tacheDAO) {
        this.membreDAO = membreDAO;
        this.membreProjetDAO = membreProjetDAO;
        this.tacheDAO = tacheDAO;
    }

    // ---------- INSCRIPTION / CONNEXION ----------

    /**
     * Inscrit un membre. Avant l'appel, membre.getMotdepassemem() contient le mot de passe
     * en clair ; le service le remplace par son hachage avant l'enregistrement,
     * puis vide le champ dans l'objet retourné.
     */
    public Membre inscrire(Membre membre) {
        validerIdentite(membre);
        membre.setEmailmem(normaliserEmail(membre.getEmailmem()));
        validerMotDePasse(membre.getMotdepassemem());

        if (membreDAO.findByEmail(membre.getEmailmem()).isPresent()) {
            throw new IllegalArgumentException("Cet email est déjà utilisé.");
        }

        membre.setMotdepassemem(hacher(membre.getMotdepassemem()));
        membre.setDatecreamem(null); 
        membreDAO.create(membre);
        membre.setMotdepassemem("");
        return membre;
    }

    /** Vérifie les identifiants et retourne le membre connecté. */
    public Membre authentifier(String email, String motDePasse) {
        if (email == null || motDePasse == null) {
            throw new IllegalArgumentException("Email ou mot de passe incorrect.");
        }
        Optional<Membre> membre = membreDAO.findByEmail(normaliserEmail(email));
        if (membre.isEmpty() || !verifier(motDePasse, membre.get().getMotdepassemem())) {
            throw new IllegalArgumentException("Email ou mot de passe incorrect.");
        }
        return membre.get();
    }

    // ---------- LECTURE ----------

    public List<Membre> getTousLesMembres() {
        return membreDAO.findAll();
    }

    public Membre getMembre(int idMembre) {
        return membreDAO.findById(idMembre)
                .orElseThrow(() -> new IllegalArgumentException("Membre introuvable : " + idMembre));
    }

    // ---------- MODIFICATION ----------

    /** Modifie nom, prénom et email. Le mot de passe et la date de création ne changent pas. */
    public Membre modifierProfil(Membre membre) {
        Membre existant = getMembre(membre.getIDmem());
        validerIdentite(membre);
        membre.setEmailmem(normaliserEmail(membre.getEmailmem()));

        Optional<Membre> memeEmail = membreDAO.findByEmail(membre.getEmailmem());
        if (memeEmail.isPresent() && memeEmail.get().getIDmem() != membre.getIDmem()) {
            throw new IllegalArgumentException("Cet email est déjà utilisé.");
        }

        membre.setMotdepassemem(existant.getMotdepassemem());
        membre.setDatecreamem(existant.getDatecreamem());
        membreDAO.update(membre);
        membre.setMotdepassemem("");
        return membre;
    }

    public void changerMotDePasse(int idMembre, String ancienMotDePasse, String nouveauMotDePasse) {
        Membre membre = getMembre(idMembre);
        if (ancienMotDePasse == null || !verifier(ancienMotDePasse, membre.getMotdepassemem())) {
            throw new IllegalArgumentException("L'ancien mot de passe est incorrect.");
        }
        validerMotDePasse(nouveauMotDePasse);

        membre.setMotdepassemem(hacher(nouveauMotDePasse));
        membreDAO.update(membre);
    }

    // ---------- SUPPRESSION ----------

    /**
     * Supprime le compte d'un membre après confirmation du mot de passe.
     * Refusé s'il a encore des tâches assignées (clé étrangère RESTRICT en base)
     * ou s'il est le seul chef d'un projet.
     */
    public void supprimerCompte(int idMembre, String motDePasse) {
        Membre membre = getMembre(idMembre);
        if (motDePasse == null || !verifier(motDePasse, membre.getMotdepassemem())) {
            throw new IllegalArgumentException("Mot de passe incorrect.");
        }
        if (!tacheDAO.findByMembre(idMembre).isEmpty()) {
            throw new IllegalStateException(
                    "Ce membre a encore des tâches assignées : réassignez-les avant de supprimer le compte.");
        }
        for (Membre_Projet mp : membreProjetDAO.findByMembre(idMembre)) {
            if (mp.getRolemem() == Rolemem.CHEF_DE_PROJET && estSeulChef(mp.getIDpro())) {
                throw new IllegalStateException(
                        "Vous êtes le seul chef du projet " + mp.getIDpro()
                                + " : nommez un autre chef ou supprimez le projet.");
            }
        }
        membreDAO.delete(idMembre);
    }


    private boolean estSeulChef(int idProjet) {
        long chefs = membreProjetDAO.findByProjet(idProjet).stream()
                .filter(m -> m.getRolemem() == Rolemem.CHEF_DE_PROJET)
                .count();
        return chefs <= 1;
    }

    private String normaliserEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private void validerIdentite(Membre membre) {
        if (membre == null) {
            throw new IllegalArgumentException("Le membre est obligatoire.");
        }
        if (membre.getNommem() == null || membre.getNommem().isBlank()) {
            throw new IllegalArgumentException("Le nom est obligatoire.");
        }
        if (membre.getPrenommem() == null || membre.getPrenommem().isBlank()) {
            throw new IllegalArgumentException("Le prénom est obligatoire.");
        }
        if (membre.getNommem().length() > MAX_LONGUEUR || membre.getPrenommem().length() > MAX_LONGUEUR) {
            throw new IllegalArgumentException("Le nom et le prénom ne doivent pas dépasser "
                    + MAX_LONGUEUR + " caractères.");
        }
        String email = normaliserEmail(membre.getEmailmem());
        if (email == null || !EMAIL.matcher(email).matches() || email.length() > MAX_LONGUEUR) {
            throw new IllegalArgumentException("L'adresse email est invalide.");
        }
    }

    private void validerMotDePasse(String motDePasse) {
        if (motDePasse == null || motDePasse.length() < MIN_MOT_DE_PASSE) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins "
                    + MIN_MOT_DE_PASSE + " caractères.");
        }
    }

    // ---------- HACHAGE (PBKDF2) ----------

    private String hacher(String motDePasse) {
        byte[] sel = new byte[TAILLE_SEL_OCTETS];
        new SecureRandom().nextBytes(sel);
        byte[] hash = pbkdf2(motDePasse, sel, ITERATIONS);
        return ITERATIONS + ":" + Base64.getEncoder().encodeToString(sel)
                + ":" + Base64.getEncoder().encodeToString(hash);
    }

    private boolean verifier(String motDePasse, String stocke) {
        if (stocke == null) {
            return false;
        }
        String[] parties = stocke.split(":");
        if (parties.length != 3) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parties[0]);
            byte[] sel = Base64.getDecoder().decode(parties[1]);
            byte[] attendu = Base64.getDecoder().decode(parties[2]);
            byte[] calcule = pbkdf2(motDePasse, sel, iterations);
            return MessageDigest.isEqual(attendu, calcule); // comparaison à temps constant
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private byte[] pbkdf2(String motDePasse, byte[] sel, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(motDePasse.toCharArray(), sel, iterations, TAILLE_CLE_BITS);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Hachage du mot de passe impossible", e);
        }
    }
}