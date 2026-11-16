package com.enicarthage.chilla.router;

import com.enicarthage.chilla.controller.AuthController;
import com.enicarthage.chilla.controller.InvitationController;
import com.enicarthage.chilla.controller.MembreController;
import com.enicarthage.chilla.controller.Membre_ProjetController;
import com.enicarthage.chilla.controller.NotificationController;
import com.enicarthage.chilla.controller.ProjetController;
import com.enicarthage.chilla.controller.SprintController;
import com.enicarthage.chilla.controller.TacheController;
import com.enicarthage.chilla.dto.AssignationRequest;
import com.enicarthage.chilla.dto.ConnexionRequest;
import com.enicarthage.chilla.dto.DtoMapper;
import com.enicarthage.chilla.dto.ErreurResponse;
import com.enicarthage.chilla.dto.EtatRequest;
import com.enicarthage.chilla.dto.InscriptionRequest;
import com.enicarthage.chilla.dto.InvitationRequest;
import com.enicarthage.chilla.dto.MessageResponse;
import com.enicarthage.chilla.dto.MotDePasseRequest;
import com.enicarthage.chilla.dto.ProfilRequest;
import com.enicarthage.chilla.dto.ProjetRequest;
import com.enicarthage.chilla.dto.RoleRequest;
import com.enicarthage.chilla.dto.SprintRequest;
import com.enicarthage.chilla.dto.SuppressionCompteRequest;
import com.enicarthage.chilla.dto.TacheRequest;
import com.enicarthage.chilla.exception.AuthentificationException;
import com.enicarthage.chilla.service.AuthService;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonParseException;
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

    // Vérification des JWT. Créé une seule fois : l'application refuse de démarrer
    // si JWT_SECRET est absent (exception levée par JwtService).
    private final AuthService authService = new AuthService();

    // ---------------------------------------------------------
    // Point d'entrée unique pour toutes les requêtes HTTP
    // ---------------------------------------------------------
    public void dispatcher(HttpExchange exchange) throws IOException {
        ajouterCors(exchange);
        String methode = exchange.getRequestMethod();

        // Pré-requête CORS du navigateur (frontend séparé) : sans jeton
        if (methode.equals("OPTIONS")) {
            envoyer(exchange, 204, "");
            return;
        }

        String[] segments = exchange.getRequestURI().getPath().split("/");

        try {
            // Sécurisé par défaut : tout est protégé sauf l'inscription et la connexion
            if (!estRoutePublique(methode, segments)) {
                authentifier(exchange);
            }
            Reponse reponse = router(methode, segments, exchange);
            envoyer(exchange, reponse.statut(), reponse.corps());
        } catch (HttpErreur e) {
            envoyer(exchange, e.statut, jsonErreur(e.getMessage()));
        } catch (IllegalArgumentException | JsonParseException e) {
            envoyer(exchange, 400, jsonErreur("Requête invalide : " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            envoyer(exchange, 500, jsonErreur("Erreur interne du serveur."));
        }
    }

    // Dispatch par première ressource
    private Reponse router(String methode, String[] segments, HttpExchange ex) throws IOException {
        if (segments.length < 2 || segments[1].isBlank()) {
            throw new HttpErreur(404, "Route vide ou inconnue.");
        }
        switch (segments[1]) {
            case "auth":
                return routesAuth(methode, segments, ex);
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
    // AUTH (les deux premières routes sont publiques, le reste exige un jeton)
    // POST /auth/inscription (InscriptionRequest) -> SessionResponse
    // POST /auth/connexion (ConnexionRequest) -> SessionResponse
    // GET /auth/moi -> MembreResponse
    //
    // Toutes les autres routes exigent : Authorization: Bearer <token>
    // ---------------------------------------------------------
    private Reponse routesAuth(String m, String[] s, HttpExchange ex) throws IOException {
        if (s.length != 3) {
            throw inconnue(s);
        }
        AuthController c = new AuthController();

        switch (s[2]) {
            case "inscription": {
                exiger(m, "POST", s);
                InscriptionRequest b = corps(ex, InscriptionRequest.class);
                return rep(c, c.inscrire(b.nom(), b.prenom(), b.email(), b.motDePasse()), 201);
            }
            case "connexion": {
                exiger(m, "POST", s);
                ConnexionRequest b = corps(ex, ConnexionRequest.class);
                boolean ok = c.connecter(b.email(), b.motDePasse());
                if (!ok) {
                    return new Reponse(401, jsonErreur(c.getMessage()));
                }
                return rep(c, true, 200);
            }
            case "moi":
                exiger(m, "GET", s);
                return rep(c, c.consulterMembreCourant(acteur(ex)), 200);
            default:
                throw inconnue(s);
        }
    }

    // ---------------------------------------------------------
    // MEMBRES
    // GET /membres
    // GET /membres/{id}
    // PUT /membres/{id} (ProfilRequest) : uniquement son propre compte
    // DELETE /membres/{id} (SuppressionCompteRequest) : uniquement son propre compte
    // PUT /membres/{id}/mot-de-passe (MotDePasseRequest) : uniquement le sien
    // GET /membres/{id}/projets
    // GET /membres/{id}/projets/actifs
    // GET /membres/{id}/taches
    // (l'inscription et la connexion sont sous /auth)
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
            int id = entier(s[2]);
            MembreController c = new MembreController();
            switch (m) {
                case "GET":
                    return rep(c, c.consulterMembre(id), 200);
                case "PUT": {
                    exigerSoiMeme(ex, id);
                    ProfilRequest b = corps(ex, ProfilRequest.class);
                    return rep(c, c.modifierProfil(id, b.nom(), b.prenom(), b.email()), 200);
                }
                case "DELETE": {
                    exigerSoiMeme(ex, id);
                    SuppressionCompteRequest b = corps(ex, SuppressionCompteRequest.class);
                    return rep(c, c.supprimerCompte(id, b.motDePasse()), 200);
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
                    exigerSoiMeme(ex, id);
                    MotDePasseRequest b = corps(ex, MotDePasseRequest.class);
                    MembreController c = new MembreController();
                    return rep(c, c.changerMotDePasse(id, b.ancienMotDePasse(), b.nouveauMotDePasse()), 200);
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
    // POST /projets (ProjetRequest)
    // GET /projets/{id}
    // PUT /projets/{id} (ProjetRequest)
    // DELETE /projets/{id}
    // POST /projets/{id}/archiver
    // POST /projets/{id}/desarchiver
    // POST /projets/{id}/quitter
    // GET /projets/{id}/membres
    // GET /projets/{id}/membres/{idMembre}/role
    // PUT /projets/{id}/membres/{idMembre}/role (RoleRequest)
    // DELETE /projets/{id}/membres/{idMembre}
    // GET /projets/{id}/sprints
    // POST /projets/{id}/sprints (SprintRequest)
    // GET /projets/{id}/sprints/en-cours
    // GET /projets/{id}/taches
    // POST /projets/{id}/taches (TacheRequest)
    // POST /projets/{id}/invitations (InvitationRequest : idRecepteur OU email)
    // ---------------------------------------------------------
    private Reponse routesProjet(String m, String[] s, HttpExchange ex) throws IOException {
        int n = s.length;

        if (n == 2) {
            ProjetController c = new ProjetController();
            switch (m) {
                case "GET":
                    return rep(c, c.listerTousLesProjets(), 200);
                case "POST": {
                    ProjetRequest b = corps(ex, ProjetRequest.class);
                    return rep(c, c.creerProjet(b.libelle(), b.description(),
                            b.dateDebut(), b.dateFin(), acteur(ex)), 201);
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
                    ProjetRequest b = corps(ex, ProjetRequest.class);
                    return rep(c, c.modifierProjet(id, b.libelle(), b.description(),
                            b.dateDebut(), b.dateFin(), acteur(ex)), 200);
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
                        SprintRequest b = corps(ex, SprintRequest.class);
                        return rep(c, c.creerSprint(id, b.nom(), b.objectif(),
                                b.dateDebut(), b.dateFin(), acteur(ex)), 201);
                    }
                    throw nonSupportee(m, s);
                }
                case "taches": {
                    TacheController c = new TacheController();
                    if (m.equals("GET")) {
                        return rep(c, c.listerTachesDuProjet(id), 200);
                    }
                    if (m.equals("POST")) {
                        TacheRequest b = corps(ex, TacheRequest.class);
                        return rep(c, c.creerTache(id, b.idSprint(), b.idMembreAssigne(), b.libelle(),
                                b.description(), b.dateEcheance(), b.estimation(), b.position(),
                                acteur(ex)), 201);
                    }
                    throw nonSupportee(m, s);
                }
                case "invitations": {
                    exiger(m, "POST", s);
                    InvitationRequest b = corps(ex, InvitationRequest.class);
                    InvitationController c = new InvitationController();
                    boolean parEmail = b.email() != null && !b.email().isBlank();
                    boolean ok = parEmail
                            ? c.inviterParEmail(id, acteur(ex), b.email())
                            : c.inviterMembre(id, acteur(ex), b.idRecepteur());
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
                RoleRequest b = corps(ex, RoleRequest.class);
                return rep(c, c.changerRole(id, idCible, b.role(), acteur(ex)), 200);
            }
            throw nonSupportee(m, s);
        }

        throw inconnue(s);
    }

    // ---------------------------------------------------------
    // SPRINTS
    // GET /sprints/{id}
    // PUT /sprints/{id} (SprintRequest)
    // DELETE /sprints/{id}
    // PUT /sprints/{id}/etat (EtatRequest)
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
                    SprintRequest b = corps(ex, SprintRequest.class);
                    return rep(c, c.modifierSprint(id, b.nom(), b.objectif(),
                            b.dateDebut(), b.dateFin(), acteur(ex)), 200);
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
                    EtatRequest b = corps(ex, EtatRequest.class);
                    return rep(c, c.changerEtat(id, b.etat(), acteur(ex)), 200);
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
    // PUT /taches/{id} (TacheRequest)
    // DELETE /taches/{id}
    // PUT /taches/{id}/etat (EtatRequest)
    // PUT /taches/{id}/assignation (AssignationRequest)
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
                    TacheRequest b = corps(ex, TacheRequest.class);
                    return rep(c, c.modifierTache(id, b.idSprint(), b.idMembreAssigne(), b.libelle(),
                            b.description(), b.dateEcheance(), b.estimation(), b.position(),
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
            EtatRequest b = corps(ex, EtatRequest.class);
            return rep(c, c.changerEtat(id, b.etat(), acteur(ex)), 200);
        }

        if (n == 4 && s[3].equals("assignation")) {
            exiger(m, "PUT", s);
            AssignationRequest b = corps(ex, AssignationRequest.class);
            return rep(c, c.assignerTache(id, b.idMembre(), acteur(ex)), 200);
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
    private Reponse rep(AuthController c, boolean ok, int statut) {
        return reponse(ok, statut, c.getMessage(), c.getDonnees());
    }

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
            return new Reponse(400, jsonErreur(message));
        }
        if (donnees == null) {
            return new Reponse(statutSucces, jsonMessage(message));
        }
        // Le modèle est converti en DTO de réponse : le mot de passe n'est jamais exposé
        return new Reponse(statutSucces, gson.toJson(DtoMapper.convertir(donnees)));
    }

    // ---------------------------------------------------------
    // Authentification
    // ---------------------------------------------------------

    // Seules l'inscription et la connexion sont accessibles sans jeton
    private boolean estRoutePublique(String methode, String[] segments) {
        return methode.equals("POST")
                && segments.length == 3
                && segments[1].equals("auth")
                && (segments[2].equals("inscription") || segments[2].equals("connexion"));
    }

    // Identifiant du membre authentifié, lu dans le JWT (en-tête Authorization: Bearer ...).
    private int acteur(HttpExchange ex) {
        return authentifier(ex);
    }

    private int authentifier(HttpExchange ex) {
        try {
            return authService.identifier(ex.getRequestHeaders().getFirst("Authorization"));
        } catch (AuthentificationException e) {
            throw new HttpErreur(401, e.getMessage());
        }
    }

    // Un membre ne peut agir que sur son propre compte
    private void exigerSoiMeme(HttpExchange ex, int idMembre) {
        if (acteur(ex) != idMembre) {
            throw new HttpErreur(403, "Vous ne pouvez agir que sur votre propre compte.");
        }
    }

    // ---------------------------------------------------------
    // Utilitaires de lecture de la requête
    // ---------------------------------------------------------

    private int entier(String texte) {
        try {
            return Integer.parseInt(texte);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Identifiant invalide : " + texte);
        }
    }

    // Corps JSON -> DTO de requête (Gson). Corps vide -> DTO avec valeurs par défaut
    // (null / 0) : les controllers et services valident ensuite les champs.
    private <T> T corps(HttpExchange ex, Class<T> classe) throws IOException {
        String contenu = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        if (contenu.isBlank()) {
            contenu = "{}";
        }
        T requete = gson.fromJson(contenu, classe);
        return requete != null ? requete : gson.fromJson("{}", classe);
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
    private String jsonErreur(String message) {
        return gson.toJson(new ErreurResponse(message == null ? "" : message));
    }

    private String jsonMessage(String message) {
        return gson.toJson(new MessageResponse(message == null ? "" : message));
    }

    // CORS : le frontend tourne sur une autre origine (ex. localhost:4200 ou 5173).
    // Authorization doit être autorisé, sinon le navigateur bloque le preflight.
    private void ajouterCors(HttpExchange exchange) {
        var h = exchange.getResponseHeaders();
        h.set("Access-Control-Allow-Origin", "*");
        h.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        h.set("Access-Control-Allow-Headers", "Content-Type, Authorization");
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
