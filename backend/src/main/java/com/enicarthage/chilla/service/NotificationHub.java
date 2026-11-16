package com.enicarthage.chilla.service;

import com.enicarthage.chilla.config.JsonConfig;
import com.enicarthage.chilla.model.Notification;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Diffusion des notifications en temps réel (Server-Sent Events).
 *
 * Chaque navigateur connecté garde une connexion HTTP ouverte sur GET /notifications/flux.
 * Quand une notification est créée, publier() l'écrit immédiatement dans les connexions
 * du membre concerné. Les écritures se font dans un pool de threads dédié : la requête
 * qui déclenche la notification n'attend jamais un client lent.
 *
 * Ce service ne connaît pas HTTP : il reçoit simplement un OutputStream.
 * Format SSE : "event: <nom>\ndata: <json>\n\n" ; une ligne ": ping" sert de battement
 * de cœur pour garder la connexion vivante et détecter les clients partis.
 */
public class NotificationHub {

    private static final int MAX_CONNEXIONS_PAR_MEMBRE = 5; // onglets ouverts simultanément
    private static final long PERIODE_BATTEMENT_SECONDES = 20;

    private static final NotificationHub INSTANCE = new NotificationHub(JsonConfig.GSON, PERIODE_BATTEMENT_SECONDES);

    /** Instance unique partagée par tous les services (il faut un seul registre de connexions). */
    public static NotificationHub getInstance() {
        return INSTANCE;
    }

    private final Gson gson;
    private final Map<Integer, List<Connexion>> connexions = new ConcurrentHashMap<>();
    private final ExecutorService ecriture;
    private final ScheduledExecutorService planificateur;

    // Constructeur avec injection : pratique pour les tests (période du battement de cœur réglable)
    public NotificationHub(Gson gson, long periodeBattementSecondes) {
        this.gson = gson;
        this.ecriture = Executors.newCachedThreadPool(threadDemon("notif-ecriture"));
        this.planificateur = Executors.newSingleThreadScheduledExecutor(threadDemon("notif-battement"));
        this.planificateur.scheduleAtFixedRate(this::battement, periodeBattementSecondes,
                periodeBattementSecondes, TimeUnit.SECONDS);
    }

    // ---------- ABONNEMENT ----------

    /**
     * Enregistre une connexion ouverte pour ce membre et lui envoie l'événement "connecte"
     * (avec son nombre de notifications non lues, pour resynchroniser son compteur).
     * Les en-têtes HTTP doivent déjà avoir été envoyés. Le flux reste ouvert jusqu'à la
     * déconnexion du client (détectée à la prochaine écriture).
     */
    public void abonner(int idMembre, OutputStream flux, int nonLues) {
        Connexion connexion = new Connexion(flux);
        List<Connexion> liste = connexions.computeIfAbsent(idMembre, k -> new CopyOnWriteArrayList<>());
        liste.add(connexion);

        // Trop d'onglets : on ferme les plus anciennes
        while (liste.size() > MAX_CONNEXIONS_PAR_MEMBRE) {
            try {
                liste.remove(0).fermer();
            } catch (IndexOutOfBoundsException e) {
                break;
            }
        }

        boolean ok = connexion.ecrire("retry: 5000\n\n"
                + evenement("connecte", "{\"nonLues\":" + nonLues + "}"));
        if (!ok) {
            liste.remove(connexion);
        }
    }

    public boolean aDesAbonnes(int idMembre) {
        List<Connexion> liste = connexions.get(idMembre);
        return liste != null && !liste.isEmpty();
    }

    // ---------- PUBLICATION ----------

    /** Envoie la notification à toutes les connexions ouvertes du membre (sans bloquer l'appelant). */
    public void publier(int idMembre, Notification notification) {
        List<Connexion> liste = connexions.get(idMembre);
        if (liste == null || liste.isEmpty()) {
            return;
        }
        String message = evenement("notification", gson.toJson(notification));
        for (Connexion connexion : liste) {
            ecriture.execute(() -> {
                if (!connexion.ecrire(message)) {
                    liste.remove(connexion);
                }
            });
        }
    }

    // ---------- ARRET ----------

    /** Ferme toutes les connexions et arrête les threads (arrêt propre ou tests). */
    public void arreter() {
        planificateur.shutdownNow();
        connexions.values().forEach(liste -> liste.forEach(Connexion::fermer));
        connexions.clear();
        ecriture.shutdown();
    }

    // ---------- METHODES PRIVEES ----------

    // Ligne de commentaire SSE envoyée à tous : maintient la connexion et élimine les clients partis
    private void battement() {
        for (List<Connexion> liste : connexions.values()) {
            for (Connexion connexion : liste) {
                ecriture.execute(() -> {
                    if (!connexion.ecrire(": ping\n\n")) {
                        liste.remove(connexion);
                    }
                });
            }
        }
    }

    // Le JSON de Gson tient sur une seule ligne (les retours à la ligne sont échappés) : compatible SSE
    private static String evenement(String nom, String json) {
        return "event: " + nom + "\ndata: " + json + "\n\n";
    }

    private static ThreadFactory threadDemon(String nom) {
        return runnable -> {
            Thread t = new Thread(runnable, nom);
            t.setDaemon(true); // ne bloque pas l'arrêt de la JVM
            return t;
        };
    }

    // Une connexion SSE ouverte : écritures sérialisées pour ne jamais mélanger deux messages
    private static final class Connexion {
        private final OutputStream flux;
        private boolean ouverte = true;

        Connexion(OutputStream flux) {
            this.flux = flux;
        }

        synchronized boolean ecrire(String texte) {
            if (!ouverte) {
                return false;
            }
            try {
                flux.write(texte.getBytes(StandardCharsets.UTF_8));
                flux.flush();
                return true;
            } catch (IOException e) {
                fermer();
                return false;
            }
        }

        synchronized void fermer() {
            if (ouverte) {
                ouverte = false;
                try {
                    flux.close(); // termine aussi l'échange HTTP
                } catch (IOException ignore) {
                    // déjà fermé côté client
                }
            }
        }
    }
}
