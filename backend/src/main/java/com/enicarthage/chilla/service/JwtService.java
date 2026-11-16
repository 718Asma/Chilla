package com.enicarthage.chilla.service;

import com.enicarthage.chilla.exception.AuthentificationException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.github.cdimascio.dotenv.Dotenv;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;

/**
 * Création et vérification de JWT signés en HMAC-SHA256 (HS256), sans bibliothèque externe.
 *
 * Format : base64url(en-tête) . base64url(charge utile) . base64url(signature)
 * Charge utile : iss (émetteur), sub (id du membre), iat (émission), exp (expiration), en secondes.
 *
 * Le secret vient de la variable JWT_SECRET (fichier .env ou variable d'environnement) :
 * il n'est jamais écrit dans le code, et l'application refuse de démarrer sans lui.
 */
public class JwtService {

    private static final String ALGORITHME = "HS256";
    private static final String HMAC = "HmacSHA256";
    private static final String EMETTEUR = "chilla";
    private static final int MIN_SECRET_OCTETS = 32;
    private static final Duration DUREE_PAR_DEFAUT = Duration.ofHours(2);

    private static final Base64.Encoder ENCODEUR = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODEUR = Base64.getUrlDecoder();

    private final byte[] secret;
    private final Duration duree;
    private final Clock horloge;

    public JwtService() {
        this(chargerSecret(), DUREE_PAR_DEFAUT, Clock.systemUTC());
    }

    // Constructeur avec injection : pratique pour les tests (secret, durée et horloge maîtrisés)
    public JwtService(String secret, Duration duree, Clock horloge) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_OCTETS) {
            throw new IllegalStateException("JWT_SECRET doit contenir au moins " + MIN_SECRET_OCTETS
                    + " caractères. Exemple pour en générer un : openssl rand -base64 48");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.duree = duree;
        this.horloge = horloge;
    }

    /** Crée un jeton valable {@code duree} pour ce membre. */
    public String generer(int idMembre) {
        long emission = horloge.instant().getEpochSecond();

        JsonObject entete = new JsonObject();
        entete.addProperty("alg", ALGORITHME);
        entete.addProperty("typ", "JWT");

        JsonObject charge = new JsonObject();
        charge.addProperty("iss", EMETTEUR);
        charge.addProperty("sub", String.valueOf(idMembre));
        charge.addProperty("iat", emission);
        charge.addProperty("exp", emission + duree.getSeconds());

        String aSigner = encoder(entete.toString()) + "." + encoder(charge.toString());
        return aSigner + "." + ENCODEUR.encodeToString(signer(aSigner));
    }

    /** Vérifie signature, émetteur et expiration. Retourne l'id du membre ou lève AuthentificationException. */
    public int verifier(String jeton) {
        if (jeton == null || jeton.isBlank()) {
            throw invalide();
        }
        String[] parties = jeton.split("\\.", -1);
        if (parties.length != 3) {
            throw invalide();
        }

        // 1) La signature d'abord : on ne fait confiance au contenu qu'après.
        byte[] signatureRecue;
        try {
            signatureRecue = DECODEUR.decode(parties[2]);
        } catch (IllegalArgumentException e) {
            throw invalide();
        }
        byte[] signatureAttendue = signer(parties[0] + "." + parties[1]);
        if (!MessageDigest.isEqual(signatureAttendue, signatureRecue)) { // comparaison à temps constant
            throw invalide();
        }

        // 2) Lecture du contenu (signé par nous, mais on reste prudent sur sa forme)
        long expiration;
        int idMembre;
        try {
            JsonObject entete = lire(parties[0]);
            JsonObject charge = lire(parties[1]);
            if (!ALGORITHME.equals(entete.get("alg").getAsString())
                    || !EMETTEUR.equals(charge.get("iss").getAsString())) {
                throw invalide();
            }
            expiration = charge.get("exp").getAsLong();
            idMembre = Integer.parseInt(charge.get("sub").getAsString());
        } catch (RuntimeException e) {
            throw invalide();
        }

        // 3) Expiration
        if (horloge.instant().getEpochSecond() >= expiration) {
            throw new AuthentificationException("Session expirée : veuillez vous reconnecter.");
        }
        return idMembre;
    }

    // ---------- METHODES PRIVEES ----------

    private static String chargerSecret() {
        // Lit .env dans le dossier courant, puis les variables d'environnement
        String secret = Dotenv.configure().ignoreIfMissing().load().get("JWT_SECRET");
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET est manquant : définissez-le dans backend/.env "
                    + "ou dans les variables d'environnement (voir .env.example).");
        }
        return secret;
    }

    private byte[] signer(String donnees) {
        try {
            Mac mac = Mac.getInstance(HMAC); // Mac n'est pas thread-safe : une instance par appel
            mac.init(new SecretKeySpec(secret, HMAC));
            return mac.doFinal(donnees.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Signature du jeton impossible", e);
        }
    }

    private String encoder(String json) {
        return ENCODEUR.encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    private JsonObject lire(String partieBase64) {
        String json = new String(DECODEUR.decode(partieBase64), StandardCharsets.UTF_8);
        return JsonParser.parseString(json).getAsJsonObject();
    }

    private AuthentificationException invalide() {
        return new AuthentificationException("Jeton d'authentification invalide.");
    }
}