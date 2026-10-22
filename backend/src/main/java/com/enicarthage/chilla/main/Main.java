package com.enicarthage.chilla.main;

import java.io.IOException;
import java.sql.Connection;

import com.enicarthage.chilla.config.DatabaseConnection;

public class Main {

    public static void main(String[] args) {
        if (!testerConnexion()) {
            System.out.println("Verifiez que MySQL est demarre et que database.sql est execute.");
            return;
        }

        try {
            ApiServer serveur = new ApiServer(8080);
            serveur.demarrer();
            System.out.println("Serveur HTTP demarre sur http://localhost:8080");
            System.out.println("Testez avec Postman : GET http://localhost:8080/");        //produits");
        } catch (IOException e) {
            System.out.println("Impossible de demarrer le serveur : " + e.getMessage());
        }
    }

    private static boolean testerConnexion() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            System.out.println("Connexion reussie a MySQL.");
            return true;
        } catch (Exception e) {
            System.out.println("Erreur de connexion : " + e.getMessage());
            return false;
        }
    }
}