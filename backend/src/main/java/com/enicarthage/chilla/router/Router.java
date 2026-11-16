package com.enicarthage.chilla.router;

import com.enicarthage.chilla.config.JsonConfig;
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
import com.enicarthage.chilla.dto.ErreurResponse;
import com.enicarthage.chilla.dto.EtatRequest;
import com.enicarthage.chilla.dto.InscriptionRequest;
import com.enicarthage.chilla.dto.InvitationRequest;
import com.enicarthage.chilla.dto.MotDePasseRequest;
import com.enicarthage.chilla.dto.ProfilRequest;
import com.enicarthage.chilla.dto.ProjetRequest;
import com.enicarthage.chilla.dto.RoleRequest;
import com.enicarthage.chilla.dto.SprintRequest;
import com.enicarthage.chilla.dto.SuppressionCompteRequest;
import com.enicarthage.chilla.dto.TacheRequest;
import com.enicarthage.chilla.exception.AuthentificationException;
import com.enicarthage.chilla.exception.DaoException;
import com.enicarthage.chilla.exception.ResourceNotFoundException;
import com.enicarthage.chilla.service.AuthService;
import com.enicarthage.chilla.service.NotificationHub;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class Router {

    // Représente une réponse HTTP (statut + corps JSON)
    private record Reponse(int statut, String corps) {
    }

    // Marqueur : la réponse est un flux temps réel déjà ouvert, le dispatcher ne
    // doit rien envoyer
    private static final Reponse FLUX_OUVERT = new Reponse(-1, null);

    // Erreur HTTP avec un statut précis (404, 405, 401...)
    private static class HttpErreur extends RuntimeException {
        final int statut;

        HttpErreur(int statut, String message) {
            super(message);
            this.statut = statut;
        }
    }

    // Gson partagé avec la diffusion temps réel (dates ISO, pas d'échappement HTML)
    private final Gson gson = JsonConfig.GSON;

    // Vérification des JWT. Créé une seule fois : l'application refuse de démarrer
    // si JWT_SECRET est absent (exception levée par JwtService).
    private final AuthService authService = new AuthService();

    // Controllers sans état : une seule instance de chaque suffit
    private final AuthController authController = new AuthController();
    private final MembreController membreController = new MembreController();
    private final ProjetController projetController = new ProjetController();
    private final Membre_ProjetController membreProjetController = new Membre_ProjetController();
    private final SprintController sprintController = new SprintController();
    private final TacheController tacheController = new TacheController();
    private final InvitationController invitationController = new InvitationController();
    private final NotificationController notificationController = new NotificationController();

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

        // Gestion centralisée des erreurs : exceptions -> codes HTTP
        try {
            // Sécurisé par défaut : tout est protégé sauf l'inscription et la connexion
            if (!estRoutePublique(methode, segments)) {
                authentifier(exchange);
            }
            Reponse reponse = router(methode, segments, exchange);
            if (reponse != FLUX_OUVERT) {
                envoyer(exchange, reponse.statut(), reponse.corps());
            }
        } catch (HttpErreur e) {
            envoyer(exchange, e.statut, jsonErreur(e.getMessage()));
        } catch (AuthentificationException e) {
            envoyer(exchange, 401, jsonErreur(e.getMessage()));
        } catch (SecurityException e) {
            envoyer(exchange, 403, jsonErreur(e.getMessage()));
        } catch (ResourceNotFoundException e) {
            envoyer(exchange, 404, jsonErreur(e.getMessage()));
        } catch (IllegalStateException e) {
            envoyer(exchange, 409, jsonErreur(e.getMessage()));
        } catch (IllegalArgumentException e) {
            envoyer(exchange, 400, jsonErreur(e.getMessage()));
        } catch (JsonParseException e) {
            envoyer(exchange, 400, jsonErreur(
                    "Requête invalide : le corps JSON est mal formé ou un champ n'a pas le bon type."));
        } catch (DaoException e) {
            System.err.println("Erreur technique : " + e.getMessage());
            envoyer(exchange, 500,
                    jsonErreur("Une erreur technique est survenue (base de données). Veuillez réessayer."));
        } catch (Exception e) {
            e.printStackTrace();
            envoyer(exchange, 500, jsonErreur("Erreur interne du serveur."));
        }
    }

    // Dispatch par première ressource
    private Reponse router(String methode, String[] segments, HttpExchange ex) throws IOException {
        if (segments.length < 2 || segments[1].isBlank()) {
            throw new ResourceNotFoundException("Route vide ou inconnue.");
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
                throw new ResourceNotFoundException("Route inconnue : /" + segments[1]);
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
        switch (s[2]) {
            case "inscription": {
                exiger(m, "POST", s);
                InscriptionRequest b = corps(ex, InscriptionRequest.class);
                return reponse(201, authController.inscrire(b));
            }
            case "connexion": {
                exiger(m, "POST", s);
                ConnexionRequest b = corps(ex, ConnexionRequest.class);
                return reponse(200, authController.connecter(b));
            }
            case "moi":
                exiger(m, "GET", s);
                return reponse(200, authController.consulterMembreCourant(acteur(ex)));
            default:
                throw inconnue(s);
        }
    }

    // ---------------------------------------------------------
    // MEMBRES
    // GET /membres/{id} : soi-même ou un coéquipier (membre d'un projet commun)
    // PUT /membres/{id} (ProfilRequest) : uniquement son propre compte
    // DELETE /membres/{id} (SuppressionCompteRequest) : uniquement son propre
    // compte
    // PUT /membres/{id}/mot-de-passe (MotDePasseRequest) : uniquement le sien
    // GET /membres/{id}/projets : uniquement les siens
    // GET /membres/{id}/projets/actifs : uniquement les siens
    // GET /membres/{id}/taches : uniquement les siennes
    // (l'inscription et la connexion sont sous /auth ; la liste de tous les
    // membres n'existe plus : elle exposait tous les emails)
    // ---------------------------------------------------------
    private Reponse routesMembre(String m, String[] s, HttpExchange ex) throws IOException {
        int n = s.length;

        if (n == 2) {
            throw inconnue(s);
        }

        if (n == 3) {
            int id = entier(s[2]);
            switch (m) {
                case "GET": {
                    int acteur = acteur(ex);
                    if (acteur != id) {
                        membreProjetController.verifierCoequipier(acteur, id);
                    }
                    return reponse(200, membreController.consulterMembre(id));
                }
                case "PUT": {
                    exigerSoiMeme(ex, id);
                    ProfilRequest b = corps(ex, ProfilRequest.class);
                    return reponse(200, membreController.modifierProfil(id, b));
                }
                case "DELETE": {
                    exigerSoiMeme(ex, id);
                    SuppressionCompteRequest b = corps(ex, SuppressionCompteRequest.class);
                    return reponse(200, membreController.supprimerCompte(id, b));
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
                    return reponse(200, membreController.changerMotDePasse(id, b));
                }
                case "projets":
                    exiger(m, "GET", s);
                    exigerSoiMeme(ex, id);
                    return reponse(200, projetController.listerProjetsDuMembre(id));
                case "taches":
                    exiger(m, "GET", s);
                    exigerSoiMeme(ex, id);
                    return reponse(200, tacheController.listerTachesDuMembre(id));
                default:
                    break;
            }
        }

        if (n == 5 && s[3].equals("projets") && s[4].equals("actifs")) {
            exiger(m, "GET", s);
            int id = entier(s[2]);
            exigerSoiMeme(ex, id);
            return reponse(200, projetController.listerProjetsActifsDuMembre(id));
        }

        throw inconnue(s);
    }

    // ---------------------------------------------------------
    // PROJETS (+ équipe, sprints, tâches et invitations du projet)
    // GET /projets : uniquement les projets de l'acteur
    // POST /projets (ProjetRequest)
    // GET /projets/{id} : réservé aux membres du projet
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
    //
    // Tous les GET sous /projets/{id} exigent d'être membre du projet.
    // ---------------------------------------------------------
    private Reponse routesProjet(String m, String[] s, HttpExchange ex) throws IOException {
        int n = s.length;

        if (n == 2) {
            switch (m) {
                case "GET":
                    return reponse(200, projetController.listerProjetsDuMembre(acteur(ex)));
                case "POST": {
                    ProjetRequest b = corps(ex, ProjetRequest.class);
                    return reponse(201, projetController.creerProjet(b, acteur(ex)));
                }
                default:
                    throw nonSupportee(m, s);
            }
        }

        int id = entier(s[2]);

        // Lecture : réservée aux membres du projet (couvre projet, membres, sprints,
        // tâches, sprint en cours et rôle)
        if (m.equals("GET")) {
            membreProjetController.verifierMembre(id, acteur(ex));
        }

        if (n == 3) {
            switch (m) {
                case "GET":
                    return reponse(200, projetController.consulterProjet(id));
                case "PUT": {
                    ProjetRequest b = corps(ex, ProjetRequest.class);
                    return reponse(200, projetController.modifierProjet(id, b, acteur(ex)));
                }
                case "DELETE":
                    return reponse(200, projetController.supprimerProjet(id, acteur(ex)));
                default:
                    throw nonSupportee(m, s);
            }
        }

        if (n == 4) {
            switch (s[3]) {
                case "archiver":
                    exiger(m, "POST", s);
                    return reponse(200, projetController.archiverProjet(id, acteur(ex)));
                case "desarchiver":
                    exiger(m, "POST", s);
                    return reponse(200, projetController.desarchiverProjet(id, acteur(ex)));
                case "quitter":
                    exiger(m, "POST", s);
                    return reponse(200, membreProjetController.quitterProjet(id, acteur(ex)));
                case "membres":
                    exiger(m, "GET", s);
                    return reponse(200, membreProjetController.listerMembresDuProjet(id));
                case "sprints":
                    if (m.equals("GET")) {
                        return reponse(200, sprintController.listerSprintsDuProjet(id));
                    }
                    if (m.equals("POST")) {
                        SprintRequest b = corps(ex, SprintRequest.class);
                        return reponse(201, sprintController.creerSprint(id, b, acteur(ex)));
                    }
                    throw nonSupportee(m, s);
                case "taches":
                    if (m.equals("GET")) {
                        return reponse(200, tacheController.listerTachesDuProjet(id));
                    }
                    if (m.equals("POST")) {
                        TacheRequest b = corps(ex, TacheRequest.class);
                        return reponse(201, tacheController.creerTache(id, b, acteur(ex)));
                    }
                    throw nonSupportee(m, s);
                case "invitations": {
                    exiger(m, "POST", s);
                    InvitationRequest b = corps(ex, InvitationRequest.class);
                    return reponse(201, invitationController.inviter(id, b, acteur(ex)));
                }
                default:
                    break;
            }
        }

        if (n == 5 && s[3].equals("sprints") && s[4].equals("en-cours")) {
            exiger(m, "GET", s);
            return reponse(200, sprintController.consulterSprintEnCours(id));
        }

        if (n == 5 && s[3].equals("membres")) {
            exiger(m, "DELETE", s);
            return reponse(200, membreProjetController.retirerMembre(id, entier(s[4]), acteur(ex)));
        }

        if (n == 6 && s[3].equals("membres") && s[5].equals("role")) {
            int idCible = entier(s[4]);
            if (m.equals("GET")) {
                return reponse(200, membreProjetController.consulterRole(id, idCible));
            }
            if (m.equals("PUT")) {
                RoleRequest b = corps(ex, RoleRequest.class);
                return reponse(200, membreProjetController.changerRole(id, idCible, b, acteur(ex)));
            }
            throw nonSupportee(m, s);
        }

        throw inconnue(s);
    }

    // ---------------------------------------------------------
    // SPRINTS
    // GET /sprints/{id} : réservé aux membres du projet du sprint
    // PUT /sprints/{id} (SprintRequest)
    // DELETE /sprints/{id}
    // PUT /sprints/{id}/etat (EtatRequest)
    // POST /sprints/{id}/demarrer
    // POST /sprints/{id}/terminer
    // POST /sprints/{id}/annuler
    // GET /sprints/{id}/taches : réservé aux membres du projet du sprint
    // ---------------------------------------------------------
    private Reponse routesSprint(String m, String[] s, HttpExchange ex) throws IOException {
        int n = s.length;
        if (n < 3) {
            throw inconnue(s);
        }
        int id = entier(s[2]);

        if (n == 3) {
            switch (m) {
                case "GET":
                    return reponse(200, sprintController.consulterSprint(id, acteur(ex)));
                case "PUT": {
                    SprintRequest b = corps(ex, SprintRequest.class);
                    return reponse(200, sprintController.modifierSprint(id, b, acteur(ex)));
                }
                case "DELETE":
                    return reponse(200, sprintController.supprimerSprint(id, acteur(ex)));
                default:
                    throw nonSupportee(m, s);
            }
        }

        if (n == 4) {
            switch (s[3]) {
                case "etat": {
                    exiger(m, "PUT", s);
                    EtatRequest b = corps(ex, EtatRequest.class);
                    return reponse(200, sprintController.changerEtat(id, b, acteur(ex)));
                }
                case "demarrer":
                    exiger(m, "POST", s);
                    return reponse(200, sprintController.demarrerSprint(id, acteur(ex)));
                case "terminer":
                    exiger(m, "POST", s);
                    return reponse(200, sprintController.terminerSprint(id, acteur(ex)));
                case "annuler":
                    exiger(m, "POST", s);
                    return reponse(200, sprintController.annulerSprint(id, acteur(ex)));
                case "taches":
                    exiger(m, "GET", s);
                    return reponse(200, tacheController.listerTachesDuSprint(id, acteur(ex)));
                default:
                    break;
            }
        }

        throw inconnue(s);
    }

    // ---------------------------------------------------------
    // TACHES
    // GET /taches/{id} : réservé aux membres du projet de la tâche
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

        if (n == 3) {
            switch (m) {
                case "GET":
                    return reponse(200, tacheController.consulterTache(id, acteur(ex)));
                case "PUT": {
                    TacheRequest b = corps(ex, TacheRequest.class);
                    return reponse(200, tacheController.modifierTache(id, b, acteur(ex)));
                }
                case "DELETE":
                    return reponse(200, tacheController.supprimerTache(id, acteur(ex)));
                default:
                    throw nonSupportee(m, s);
            }
        }

        if (n == 4 && s[3].equals("etat")) {
            exiger(m, "PUT", s);
            EtatRequest b = corps(ex, EtatRequest.class);
            return reponse(200, tacheController.changerEtat(id, b, acteur(ex)));
        }

        if (n == 4 && s[3].equals("assignation")) {
            exiger(m, "PUT", s);
            AssignationRequest b = corps(ex, AssignationRequest.class);
            return reponse(200, tacheController.assignerTache(id, b, acteur(ex)));
        }

        throw inconnue(s);
    }

    // ---------------------------------------------------------
    // INVITATIONS
    // GET /invitations (reçues)
    // GET /invitations/en-attente
    // GET /invitations/{id} : émetteur, destinataire ou chef du projet
    // DELETE /invitations/{id} (annuler)
    // POST /invitations/{id}/accepter
    // POST /invitations/{id}/refuser
    // ---------------------------------------------------------
    private Reponse routesInvitation(String m, String[] s, HttpExchange ex) throws IOException {
        int n = s.length;

        if (n == 2) {
            exiger(m, "GET", s);
            return reponse(200, invitationController.listerInvitationsRecues(acteur(ex)));
        }

        if (n == 3 && s[2].equals("en-attente")) {
            exiger(m, "GET", s);
            return reponse(200, invitationController.listerInvitationsEnAttente(acteur(ex)));
        }

        if (n == 3) {
            int id = entier(s[2]);
            switch (m) {
                case "GET":
                    return reponse(200, invitationController.consulterInvitation(id, acteur(ex)));
                case "DELETE":
                    return reponse(200, invitationController.annulerInvitation(id, acteur(ex)));
                default:
                    throw nonSupportee(m, s);
            }
        }

        if (n == 4) {
            int id = entier(s[2]);
            if (s[3].equals("accepter")) {
                exiger(m, "POST", s);
                return reponse(200, invitationController.accepterInvitation(id, acteur(ex)));
            }
            if (s[3].equals("refuser")) {
                exiger(m, "POST", s);
                return reponse(200, invitationController.refuserInvitation(id, acteur(ex)));
            }
        }

        throw inconnue(s);
    }

    // ---------------------------------------------------------
    // NOTIFICATIONS
    // GET /notifications
    // GET /notifications/non-lues
    // GET /notifications/non-lues/nombre
    // GET /notifications/flux (temps réel : flux Server-Sent Events, reste ouvert)
    // POST /notifications/lues (tout marquer comme lu)
    // PUT /notifications/{id}/lue
    // DELETE /notifications/{id}
    // ---------------------------------------------------------
    private Reponse routesNotification(String m, String[] s, HttpExchange ex) throws IOException {
        int n = s.length;

        if (n == 2) {
            exiger(m, "GET", s);
            return reponse(200, notificationController.listerNotifications(acteur(ex)));
        }

        if (n == 3 && s[2].equals("non-lues")) {
            exiger(m, "GET", s);
            return reponse(200, notificationController.listerNotificationsNonLues(acteur(ex)));
        }

        if (n == 3 && s[2].equals("flux")) {
            exiger(m, "GET", s);
            return ouvrirFlux(ex);
        }

        if (n == 3 && s[2].equals("lues")) {
            exiger(m, "POST", s);
            return reponse(200, notificationController.marquerToutesCommeLues(acteur(ex)));
        }

        if (n == 4 && s[2].equals("non-lues") && s[3].equals("nombre")) {
            exiger(m, "GET", s);
            return reponse(200, notificationController.compterNotificationsNonLues(acteur(ex)));
        }

        if (n == 3) {
            exiger(m, "DELETE", s);
            return reponse(200, notificationController.supprimerNotification(entier(s[2]), acteur(ex)));
        }

        if (n == 4 && s[3].equals("lue")) {
            exiger(m, "PUT", s);
            return reponse(200, notificationController.marquerCommeLue(entier(s[2]), acteur(ex)));
        }

        throw inconnue(s);
    }

    // ---------------------------------------------------------
    // Temps réel : GET /notifications/flux
    // Ouvre un flux Server-Sent Events et le confie au NotificationHub.
    // La méthode retourne tout de suite (elle ne bloque jamais le serveur) ; la
    // connexion
    // reste ouverte et c'est le hub qui y écrit à chaque nouvelle notification.
    // ---------------------------------------------------------
    private Reponse ouvrirFlux(HttpExchange ex) throws IOException {
        int idMembre = acteur(ex);
        // Compteur de départ : le client se resynchronise à chaque (re)connexion
        int nonLues = notificationController.compterNotificationsNonLues(idMembre).nombre();

        var h = ex.getResponseHeaders();
        h.set("Content-Type", "text/event-stream; charset=utf-8");
        h.set("Cache-Control", "no-cache");
        h.set("X-Accel-Buffering", "no"); // évite la mise en mémoire tampon par un proxy
        ex.sendResponseHeaders(200, 0); // 0 = réponse en morceaux : le flux reste ouvert

        NotificationHub.getInstance().abonner(idMembre, ex.getResponseBody(), nonLues);
        return FLUX_OUVERT;
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

    // Identifiant du membre authentifié, lu dans le JWT (en-tête Authorization:
    // Bearer ...).
    private int acteur(HttpExchange ex) {
        return authentifier(ex);
    }

    private int authentifier(HttpExchange ex) {
        return authService.identifier(ex.getRequestHeaders().getFirst("Authorization"));
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

    // Corps JSON -> DTO de requête (Gson). Corps vide -> DTO avec valeurs par
    // défaut
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

    private ResourceNotFoundException inconnue(String[] segments) {
        return new ResourceNotFoundException("Route inconnue : " + String.join("/", segments));
    }

    // ---------------------------------------------------------
    // Utilitaires de réponse
    // ---------------------------------------------------------

    // Le controller renvoie déjà un DTO (ou une liste de DTO) : on le sérialise tel
    // quel
    private Reponse reponse(int statut, Object resultat) {
        return new Reponse(statut, gson.toJson(resultat));
    }

    private String jsonErreur(String message) {
        return gson.toJson(new ErreurResponse(message == null ? "" : message));
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