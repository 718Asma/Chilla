package com.enicarthage.chilla.router;

import com.enicarthage.chilla.controller.InvitationController;
import com.enicarthage.chilla.controller.MembreController;
import com.enicarthage.chilla.controller.Membre_ProjetController;
import com.enicarthage.chilla.controller.NotificationController;
import com.enicarthage.chilla.controller.ProjetController;
import com.enicarthage.chilla.controller.SprintController;
import com.enicarthage.chilla.controller.TacheController;
import com.enicarthage.chilla.model.Membre;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Router {

    // Représente une réponse HTTP (statut + corps JSON)
    private record Reponse(int statut, String corps) {
    }

    // Erreur HTTP avec un statut précis (404, 405, 401...)
    private static class HttpErreur extends RuntimeException {
        final int statut;

        HttpErreur(int statut, String message) {
            super(message);
            this.statut = statut;
        }
    }

    // Gson configuré pour gérer LocalDate / LocalDateTime
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class,
                    (JsonSerializer<LocalDate>) (src, type, ctx) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalDate.class,
                    (JsonDeserializer<LocalDate>) (json, type, ctx) -> json.isJsonNull() ? null
                            : LocalDate.parse(json.getAsString()))
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonSerializer<LocalDateTime>) (src, type, ctx) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonDeserializer<LocalDateTime>) (json, type, ctx) -> json.isJsonNull() ? null
                            : LocalDateTime.parse(json.getAsString()))
            .create();

    // ---------------------------------------------------------
    // Point d'entrée unique pour toutes les requêtes HTTP
    // ---------------------------------------------------------
    public void dispatcher(HttpExchange exchange) throws IOException {
        ajouterCors(exchange);
        String methode = exchange.getRequestMethod();

        // Pré-requête CORS du navigateur (frontend séparé)
        if (methode.equals("OPTIONS")) {
            envoyer(exchange, 204, "");
            return;
        }

        String[] segments = exchange.getRequestURI().getPath().split("/");

        try {
            Reponse reponse = router(methode, segments, exchange);
            envoyer(exchange, reponse.statut(), reponse.corps());
        } catch (HttpErreur e) {
            envoyer(exchange, e.statut, json("erreur", e.getMessage()));
        } catch (IllegalArgumentException | JsonParseException e) {
            envoyer(exchange, 400, json("erreur", "Requête invalide : " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            envoyer(exchange, 500, json("erreur", "Erreur interne du serveur."));
        }
    }

    // Dispatch par première ressource
    private Reponse router(String methode, String[] segments, HttpExchange ex) throws IOException {
        if (segments.length < 2 || segments[1].isBlank()) {
            throw new HttpErreur(404, "Route vide ou inconnue.");
        }
        switch (segments[1]) {
            case "membres":
                return routesMembre(methode, segments, ex);
            case "projets":
                return routesProjet(methode, segments, ex);
            case "sprints":
                return routesSprint(methode, segments, ex);
            case "taches":
                return routesTache(methode, segments, ex);
            case "invitations":
                return routesInvitation(methode, segments, ex);
            case "notifications":
                return routesNotification(methode, segments, ex);
            default:
                throw new HttpErreur(404, "Route inconnue : /" + segments[1]);
        }
    }

    // ---------------------------------------------------------
    // MEMBRES
    // POST /membres/inscription
    // POST /membres/connexion
    // GET /membres
    // GET /membres/{id}
    // PUT /membres/{id} (nom, prenom, email)
    // DELETE /membres/{id} (corps : motDePasse)
    // PUT /membres/{id}/mot-de-passe (ancienMotDePasse, nouveauMotDePasse)
    // GET /membres/{id}/projets
    // GET /membres/{id}/projets/actifs
    // GET /membres/{id}/taches
    // ---------------------------------------------------------
    private Reponse routesMembre(String m, String[] s, HttpExchange ex) throws IOException {
        int n = s.length;

        if (n == 2) {
            if (m.equals("GET")) {
                MembreController c = new MembreController();
                return rep(c, c.listerTousLesMembres(), 200);
            }
            throw nonSupportee(m, s);
        }

        if (n == 3) {
            if (s[2].equals("inscription")) {
                exiger(m, "POST", s);
                JsonObject b = corps(ex);
                MembreController c = new MembreController();
                boolean ok = c.inscrire(texte(b, "nom"), texte(b, "prenom"), texte(b, "email"),
                        texte(b, "motDePasse"));
                masquer(c, ok);
                return rep(c, ok, 201);
            }
            if (s[2].equals("connexion")) {
                exiger(m, "POST", s);
                JsonObject b = corps(ex);
                MembreController c = new MembreController();
                return rep(c, c.connecter(texte(b, "email"), texte(b, "motDePasse")), 200);
            }

            int id = entier(s[2]);
            MembreController c = new MembreController();
            switch (m) {
                case "GET":
                    return rep(c, c.consulterMembre(id), 200);
                case "PUT": {
                    JsonObject b = corps(ex);
                    boolean ok = c.modifierProfil(id, texte(b, "nom"), texte(b, "prenom"), texte(b, "email"));
                    masquer(c, ok);
                    return rep(c, ok, 200);
                }
                case "DELETE": {
                    JsonObject b = corps(ex);
                    return rep(c, c.supprimerCompte(id, texte(b, "motDePasse")), 200);
                }
                default:
                    throw nonSupportee(m, s);
            }
        }

        if (n == 4) {
            int id = entier(s[2]);
            switch (s[3]) {
                case "mot-de-passe": {
                    exiger(m, "PUT", s);
                    JsonObject b = corps(ex);
                    MembreController c = new MembreController();
                    return rep(c, c.changerMotDePasse(id, texte(b, "ancienMotDePasse"),
                            texte(b, "nouveauMotDePasse")), 200);
                }
                case "projets": {
                    exiger(m, "GET", s);
                    ProjetController c = new ProjetController();
                    return rep(c, c.listerProjetsDuMembre(id), 200);
                }
                case "taches": {
                    exiger(m, "GET", s);
                    TacheController c = new TacheController();
                    return rep(c, c.listerTachesDuMembre(id), 200);
                }
                default:
                    break;
            }
        }

        if (n == 5 && s[3].equals("projets") && s[4].equals("actifs")) {
            exiger(m, "GET", s);
            int id = entier(s[2]);
            ProjetController c = new ProjetController();
            return rep(c, c.listerProjetsActifsDuMembre(id), 200);
        }

        throw inconnue(s);
    }

    // ---------------------------------------------------------
    // PROJETS (+ équipe, sprints, tâches et invitations du projet)
    // GET /projets
    // POST /projets (libelle, description, dateDebut, dateFin)
    // GET /projets/{id}
    // PUT /projets/{id}
    // DELETE /projets/{id}
    // POST /projets/{id}/archiver
    // POST /projets/{id}/desarchiver
    // POST /projets/{id}/quitter
    // GET /projets/{id}/membres
    // GET /projets/{id}/membres/{idMembre}/role
    // PUT /projets/{id}/membres/{idMembre}/role (role)
    // DELETE /projets/{id}/membres/{idMembre}
    // GET /projets/{id}/sprints
    // POST /projets/{id}/sprints (nom, objectif, dateDebut, dateFin)
    // GET /projets/{id}/sprints/en-cours
    // GET /projets/{id}/taches
    // POST /projets/{id}/taches (idSprint, idMembreAssigne, libelle,
    // description, dateEcheance, estimation, position)
    // POST /projets/{id}/invitations (idRecepteur OU email)
    // ---------------------------------------------------------
    private Reponse routesProjet(String m, String[] s, HttpExchange ex) throws IOException {
        int n = s.length;

        if (n == 2) {
            ProjetController c = new ProjetController();
            switch (m) {
                case "GET":
                    return rep(c, c.listerTousLesProjets(), 200);
                case "POST": {
                    JsonObject b = corps(ex);
                    return rep(c, c.creerProjet(texte(b, "libelle"), texte(b, "description"),
                            texte(b, "dateDebut"), texte(b, "dateFin"), acteur(ex)), 201);
                }
                default:
                    throw nonSupportee(m, s);
            }
        }

        int id = entier(s[2]);

        if (n == 3) {
            ProjetController c = new ProjetController();
            switch (m) {
                case "GET":
                    return rep(c, c.consulterProjet(id), 200);
                case "PUT": {
                    JsonObject b = corps(ex);
                    return rep(c, c.modifierProjet(id, texte(b, "libelle"), texte(b, "description"),
                            texte(b, "dateDebut"), texte(b, "dateFin"), acteur(ex)), 200);
                }
                case "DELETE":
                    return rep(c, c.supprimerProjet(id, acteur(ex)), 200);
                default:
                    throw nonSupportee(m, s);
            }
        }

        if (n == 4) {
            switch (s[3]) {
                case "archiver": {
                    exiger(m, "POST", s);
                    ProjetController c = new ProjetController();
                    return rep(c, c.archiverProjet(id, acteur(ex)), 200);
                }
                case "desarchiver": {
                    exiger(m, "POST", s);
                    ProjetController c = new ProjetController();
                    return rep(c, c.desarchiverProjet(id, acteur(ex)), 200);
                }
                case "quitter": {
                    exiger(m, "POST", s);
                    Membre_ProjetController c = new Membre_ProjetController();
                    return rep(c, c.quitterProjet(id, acteur(ex)), 200);
                }
                case "membres": {
                    exiger(m, "GET", s);
                    Membre_ProjetController c = new Membre_ProjetController();
                    return rep(c, c.listerMembresDuProjet(id), 200);
                }
                case "sprints": {
                    SprintController c = new SprintController();
                    if (m.equals("GET")) {
                        return rep(c, c.listerSprintsDuProjet(id), 200);
                    }
                    if (m.equals("POST")) {
                        JsonObject b = corps(ex);
                        return rep(c, c.creerSprint(id, texte(b, "nom"), texte(b, "objectif"),
                                texte(b, "dateDebut"), texte(b, "dateFin"), acteur(ex)), 201);
                    }
                    throw nonSupportee(m, s);
                }
                case "taches": {
                    TacheController c = new TacheController();
                    if (m.equals("GET")) {
                        return rep(c, c.listerTachesDuProjet(id), 200);
                    }
                    if (m.equals("POST")) {
                        JsonObject b = corps(ex);
                        return rep(c, c.creerTache(id, entierOuZero(b, "idSprint"),
                                entierOuZero(b, "idMembreAssigne"), texte(b, "libelle"),
                                texte(b, "description"), texte(b, "dateEcheance"),
                                entierOuZero(b, "estimation"), entierOuZero(b, "position"),
                                acteur(ex)), 201);
                    }
                    throw nonSupportee(m, s);
                }
                case "invitations": {
                    exiger(m, "POST", s);
                    JsonObject b = corps(ex);
                    InvitationController c = new InvitationController();
                    boolean ok = texte(b, "email") != null
                            ? c.inviterParEmail(id, acteur(ex), texte(b, "email"))
                            : c.inviterMembre(id, acteur(ex), entierOuZero(b, "idRecepteur"));
                    return rep(c, ok, 201);
                }
                default:
                    break;
            }
        }

        if (n == 5 && s[3].equals("sprints") && s[4].equals("en-cours")) {
            exiger(m, "GET", s);
            SprintController c = new SprintController();
            return rep(c, c.consulterSprintEnCours(id), 200);
        }

        if (n == 5 && s[3].equals("membres")) {
            exiger(m, "DELETE", s);
            int idCible = entier(s[4]);
            Membre_ProjetController c = new Membre_ProjetController();
            return rep(c, c.retirerMembre(id, idCible, acteur(ex)), 200);
        }

        if (n == 6 && s[3].equals("membres") && s[5].equals("role")) {
            int idCible = entier(s[4]);
            Membre_ProjetController c = new Membre_ProjetController();
            if (m.equals("GET")) {
                return rep(c, c.consulterRole(id, idCible), 200);
            }
            if (m.equals("PUT")) {
                JsonObject b = corps(ex);
                String role = texte(b, "role");
                return rep(c, c.changerRole(id, idCible, role, acteur(ex)), 200);
            }
            throw nonSupportee(m, s);
        }

        throw inconnue(s);
    }

    // ---------------------------------------------------------
    // SPRINTS
    // GET /sprints/{id}
    // PUT /sprints/{id} (nom, objectif, dateDebut, dateFin)
    // DELETE /sprints/{id}
    // PUT /sprints/{id}/etat (etat)
    // POST /sprints/{id}/demarrer
    // POST /sprints/{id}/terminer
    // POST /sprints/{id}/annuler
    // GET /sprints/{id}/taches
    // ---------------------------------------------------------
    private Reponse routesSprint(String m, String[] s, HttpExchange ex) throws IOException {
        int n = s.length;
        if (n < 3) {
            throw inconnue(s);
        }
        int id = entier(s[2]);

        if (n == 3) {
            SprintController c = new SprintController();
            switch (m) {
                case "GET":
                    return rep(c, c.consulterSprint(id), 200);
                case "PUT": {
                    JsonObject b = corps(ex);
                    return rep(c, c.modifierSprint(id, texte(b, "nom"), texte(b, "objectif"),
                            texte(b, "dateDebut"), texte(b, "dateFin"), acteur(ex)), 200);
                }
                case "DELETE":
                    return rep(c, c.supprimerSprint(id, acteur(ex)), 200);
                default:
                    throw nonSupportee(m, s);
            }
        }

        if (n == 4) {
            SprintController c = new SprintController();
            switch (s[3]) {
                case "etat": {
                    exiger(m, "PUT", s);
                    JsonObject b = corps(ex);
                    String etat = texte(b, "etat");
                    return rep(c, c.changerEtat(id, etat, acteur(ex)), 200);
                }
                case "demarrer":
                    exiger(m, "POST", s);
                    return rep(c, c.demarrerSprint(id, acteur(ex)), 200);
                case "terminer":
                    exiger(m, "POST", s);
                    return rep(c, c.terminerSprint(id, acteur(ex)), 200);
                case "annuler":
                    exiger(m, "POST", s);
                    return rep(c, c.annulerSprint(id, acteur(ex)), 200);
                case "taches": {
                    exiger(m, "GET", s);
                    TacheController tc = new TacheController();
                    return rep(tc, tc.listerTachesDuSprint(id), 200);
                }
                default:
                    break;
            }
        }

        throw inconnue(s);
    }

    // ---------------------------------------------------------
    // TACHES
    // GET /taches/{id}
    // PUT /taches/{id} (idSprint, idMembreAssigne, libelle, description,
    // dateEcheance, estimation, position)
    // DELETE /taches/{id}
    // PUT /taches/{id}/etat (etat)
    // PUT /taches/{id}/assignation (idMembre)
    // ---------------------------------------------------------
    private Reponse routesTache(String m, String[] s, HttpExchange ex) throws IOException {
        int n = s.length;
        if (n < 3) {
            throw inconnue(s);
        }
        int id = entier(s[2]);
        TacheController c = new TacheController();

        if (n == 3) {
            switch (m) {
                case "GET":
                    return rep(c, c.consulterTache(id), 200);
                case "PUT": {
                    JsonObject b = corps(ex);
                    return rep(c, c.modifierTache(id, entierOuZero(b, "idSprint"),
                            entierOuZero(b, "idMembreAssigne"), texte(b, "libelle"),
                            texte(b, "description"), texte(b, "dateEcheance"),
                            entierOuZero(b, "estimation"), entierOuZero(b, "position"),
                            acteur(ex)), 200);
                }
                case "DELETE":
                    return rep(c, c.supprimerTache(id, acteur(ex)), 200);
                default:
                    throw nonSupportee(m, s);
            }
        }

        if (n == 4 && s[3].equals("etat")) {
            exiger(m, "PUT", s);
            JsonObject b = corps(ex);
            String etat = texte(b, "etat");
            return rep(c, c.changerEtat(id, etat, acteur(ex)), 200);
        }

        if (n == 4 && s[3].equals("assignation")) {
            exiger(m, "PUT", s);
            JsonObject b = corps(ex);
            return rep(c, c.assignerTache(id, entierOuZero(b, "idMembre"), acteur(ex)), 200);
        }

        throw inconnue(s);
    }

    // ---------------------------------------------------------
    // INVITATIONS
    // GET /invitations (reçues)
    // GET /invitations/en-attente
    // GET /invitations/{id}
    // DELETE /invitations/{id} (annuler)
    // POST /invitations/{id}/accepter
    // POST /invitations/{id}/refuser
    // ---------------------------------------------------------
    private Reponse routesInvitation(String m, String[] s, HttpExchange ex) throws IOException {
        int n = s.length;
        InvitationController c = new InvitationController();

        if (n == 2) {
            exiger(m, "GET", s);
            return rep(c, c.listerInvitationsRecues(acteur(ex)), 200);
        }

        if (n == 3 && s[2].equals("en-attente")) {
            exiger(m, "GET", s);
            return rep(c, c.listerInvitationsEnAttente(acteur(ex)), 200);
        }

        if (n == 3) {
            int id = entier(s[2]);
            switch (m) {
                case "GET":
                    return rep(c, c.consulterInvitation(id), 200);
                case "DELETE":
                    return rep(c, c.annulerInvitation(id, acteur(ex)), 200);
                default:
                    throw nonSupportee(m, s);
            }
        }

        if (n == 4) {
            int id = entier(s[2]);
            if (s[3].equals("accepter")) {
                exiger(m, "POST", s);
                return rep(c, c.accepterInvitation(id, acteur(ex)), 200);
            }
            if (s[3].equals("refuser")) {
                exiger(m, "POST", s);
                return rep(c, c.refuserInvitation(id, acteur(ex)), 200);
            }
        }

        throw inconnue(s);
    }

    // ---------------------------------------------------------
    // NOTIFICATIONS
    // GET /notifications
    // GET /notifications/non-lues
    // GET /notifications/non-lues/nombre
    // POST /notifications/lues (tout marquer comme lu)
    // PUT /notifications/{id}/lue
    // DELETE /notifications/{id}
    // ---------------------------------------------------------
    private Reponse routesNotification(String m, String[] s, HttpExchange ex) throws IOException {
        int n = s.length;
        NotificationController c = new NotificationController();

        if (n == 2) {
            exiger(m, "GET", s);
            return rep(c, c.listerNotifications(acteur(ex)), 200);
        }

        if (n == 3 && s[2].equals("non-lues")) {
            exiger(m, "GET", s);
            return rep(c, c.listerNotificationsNonLues(acteur(ex)), 200);
        }

        if (n == 3 && s[2].equals("lues")) {
            exiger(m, "POST", s);
            return rep(c, c.marquerToutesCommeLues(acteur(ex)), 200);
        }

        if (n == 4 && s[2].equals("non-lues") && s[3].equals("nombre")) {
            exiger(m, "GET", s);
            return rep(c, c.compterNotificationsNonLues(acteur(ex)), 200);
        }

        if (n == 3) {
            exiger(m, "DELETE", s);
            return rep(c, c.supprimerNotification(entier(s[2]), acteur(ex)), 200);
        }

        if (n == 4 && s[3].equals("lue")) {
            exiger(m, "PUT", s);
            return rep(c, c.marquerCommeLue(entier(s[2]), acteur(ex)), 200);
        }

        throw inconnue(s);
    }

    // ---------------------------------------------------------
    // Conversion résultat du controller -> réponse HTTP
    // (une surcharge par controller, car ils n'ont pas d'interface commune)
    // ---------------------------------------------------------
    private Reponse rep(MembreController c, boolean ok, int statut) {
        return reponse(ok, statut, c.getMessage(), c.getDonnees());
    }

    private Reponse rep(ProjetController c, boolean ok, int statut) {
        return reponse(ok, statut, c.getMessage(), c.getDonnees());
    }

    private Reponse rep(Membre_ProjetController c, boolean ok, int statut) {
        return reponse(ok, statut, c.getMessage(), c.getDonnees());
    }

    private Reponse rep(SprintController c, boolean ok, int statut) {
        return reponse(ok, statut, c.getMessage(), c.getDonnees());
    }

    private Reponse rep(TacheController c, boolean ok, int statut) {
        return reponse(ok, statut, c.getMessage(), c.getDonnees());
    }

    private Reponse rep(InvitationController c, boolean ok, int statut) {
        return reponse(ok, statut, c.getMessage(), c.getDonnees());
    }

    private Reponse rep(NotificationController c, boolean ok, int statut) {
        return reponse(ok, statut, c.getMessage(), c.getDonnees());
    }

    private Reponse reponse(boolean ok, int statutSucces, String message, Object donnees) {
        if (!ok) {
            // Les controllers transforment toutes les exceptions en message :
            // on ne sait plus distinguer 403 / 404 / 409, d'où un 400 générique.
            return new Reponse(400, json("erreur", message));
        }
        if (donnees == null) {
            return new Reponse(statutSucces, json("message", message));
        }
        return new Reponse(statutSucces, gson.toJson(donnees));
    }

    // Ne jamais renvoyer le mot de passe (haché ou non) au client
    private void masquer(MembreController c, boolean ok) {
        if (ok) {
            Membre membre = c.getDonnees();
            if (membre != null) {
                membre.setMotdepassemem("");
            }
        }
    }

    // ---------------------------------------------------------
    // Utilitaires de lecture de la requête
    // ---------------------------------------------------------

    // Identifiant du membre qui agit (en-tête X-Membre-Id)
    private int acteur(HttpExchange ex) {
        String valeur = ex.getRequestHeaders().getFirst("X-Membre-Id");
        if (valeur == null || valeur.isBlank()) {
            throw new HttpErreur(401, "En-tête X-Membre-Id manquant (connexion requise).");
        }
        try {
            return Integer.parseInt(valeur.trim());
        } catch (NumberFormatException e) {
            throw new HttpErreur(401, "En-tête X-Membre-Id invalide.");
        }
    }

    private int entier(String texte) {
        try {
            return Integer.parseInt(texte);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Identifiant invalide : " + texte);
        }
    }

    // Corps JSON -> objet ; corps vide -> objet vide
    private JsonObject corps(HttpExchange ex) throws IOException {
        String contenu = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        if (contenu.isBlank()) {
            return new JsonObject();
        }
        JsonElement element = JsonParser.parseString(contenu);
        if (!element.isJsonObject()) {
            throw new IllegalArgumentException("le corps doit être un objet JSON.");
        }
        return element.getAsJsonObject();
    }

    // Champ texte (null si absent) : les controllers valident et nettoient
    // eux-mêmes
    private String texte(JsonObject o, String cle) {
        JsonElement e = o.get(cle);
        return (e == null || e.isJsonNull()) ? null : e.getAsString();
    }

    // Champ entier (0 si absent) : le service rejettera un id 0 inexistant
    private int entierOuZero(JsonObject o, String cle) {
        String valeur = texte(o, cle);
        if (valeur == null || valeur.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(valeur.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("le champ '" + cle + "' doit être un entier.");
        }
    }

    private void exiger(String methode, String attendue, String[] segments) {
        if (!methode.equals(attendue)) {
            throw nonSupportee(methode, segments);
        }
    }

    private HttpErreur nonSupportee(String methode, String[] segments) {
        return new HttpErreur(405, "Méthode non supportée : " + methode + " " + String.join("/", segments));
    }

    private HttpErreur inconnue(String[] segments) {
        return new HttpErreur(404, "Route inconnue : " + String.join("/", segments));
    }

    // ---------------------------------------------------------
    // Utilitaires de réponse
    // ---------------------------------------------------------
    private String json(String cle, String valeur) {
        JsonObject o = new JsonObject();
        o.addProperty(cle, valeur == null ? "" : valeur);
        return gson.toJson(o);
    }

    // CORS : le frontend tourne sur une autre origine (ex. localhost:4200 ou 5173)
    private void ajouterCors(HttpExchange exchange) {
        var h = exchange.getResponseHeaders();
        h.set("Access-Control-Allow-Origin", "*");
        h.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        h.set("Access-Control-Allow-Headers", "Content-Type, X-Membre-Id");
    }

    // Envoie la réponse HTTP avec statut et corps JSON
    private void envoyer(HttpExchange exchange, int statut, String corps) throws IOException {
        byte[] bytes = corps == null ? new byte[0] : corps.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > 0) {
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(statut, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } else {
            // Réponse sans corps (ex. 204 No Content)
            exchange.sendResponseHeaders(statut, -1);
            exchange.close();
        }
    }
}