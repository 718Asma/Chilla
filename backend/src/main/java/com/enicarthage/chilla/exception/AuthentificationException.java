package com.enicarthage.chilla.exception;

/** Jeton absent, invalide ou expiré : la requête doit être rejetée avec un 401. */
public class AuthentificationException extends RuntimeException {

    public AuthentificationException(String message) {
        super(message);
    }
}