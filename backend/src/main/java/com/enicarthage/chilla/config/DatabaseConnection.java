package com.enicarthage.chilla.config;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final Dotenv DOTENV = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    private static final String URL =
            DOTENV.get("DB_URL", "jdbc:mysql://localhost:3306/chilla");
    private static final String USER =
            DOTENV.get("DB_USER", "root");
    private static final String PASSWORD =
            DOTENV.get("DB_PASSWORD", "");

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}