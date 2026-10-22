package com.enicarthage.chilla.dao;

import com.enicarthage.chilla.config.DatabaseConnection;
import com.enicarthage.chilla.exception.DaoException;
import com.enicarthage.chilla.model.Membre_Projet;
import com.enicarthage.chilla.model.Rolemem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Table d'association : la clé primaire est composée (IDmem, IDpro), il n'y a pas d'id généré.
public class Membre_ProjetDAO {

    // CREATE : ajoute un membre dans un projet
    public void create(Membre_Projet membreProjet) {
        String sql = "INSERT INTO Membre_Projet (IDmem, IDpro, rolemem) VALUES (?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, membreProjet.getIDmem());
            statement.setInt(2, membreProjet.getIDpro());
            statement.setString(3, membreProjet.getRolemem().name());
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de l'ajout du membre " + membreProjet.getIDmem()
                    + " au projet " + membreProjet.getIDpro(), e);
        }
    }

    // READ : toutes les associations
    public List<Membre_Projet> findAll() {
        return findListBy("SELECT * FROM Membre_Projet", null,
                "Erreur lors de la lecture des membres de projets");
    }

    // READ : une association par clé composée (Optional.empty() si absente)
    public Optional<Membre_Projet> findById(int idMembre, int idProjet) {
        String sql = "SELECT * FROM Membre_Projet WHERE IDmem = ? AND IDpro = ?";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idMembre);
            statement.setInt(2, idProjet);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return Optional.of(map(result));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la lecture du membre " + idMembre
                    + " du projet " + idProjet, e);
        }
        return Optional.empty();
    }

    // READ : tous les membres d'un projet
    public List<Membre_Projet> findByProjet(int idProjet) {
        return findListBy("SELECT * FROM Membre_Projet WHERE IDpro = ?", idProjet,
                "Erreur lors de la lecture des membres du projet " + idProjet);
    }

    // READ : tous les projets d'un membre
    public List<Membre_Projet> findByMembre(int idMembre) {
        return findListBy("SELECT * FROM Membre_Projet WHERE IDmem = ?", idMembre,
                "Erreur lors de la lecture des projets du membre " + idMembre);
    }

    // UPDATE : seul le rôle peut changer (la clé primaire identifie la ligne)
    public boolean update(Membre_Projet membreProjet) {
        String sql = "UPDATE Membre_Projet SET rolemem = ? WHERE IDmem = ? AND IDpro = ?";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, membreProjet.getRolemem().name());
            statement.setInt(2, membreProjet.getIDmem());
            statement.setInt(3, membreProjet.getIDpro());
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la modification du rôle du membre "
                    + membreProjet.getIDmem() + " dans le projet " + membreProjet.getIDpro(), e);
        }
    }

    // DELETE : retire un membre d'un projet
    public boolean delete(int idMembre, int idProjet) {
        String sql = "DELETE FROM Membre_Projet WHERE IDmem = ? AND IDpro = ?";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idMembre);
            statement.setInt(2, idProjet);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DaoException("Erreur lors du retrait du membre " + idMembre
                    + " du projet " + idProjet, e);
        }
    }

    // Factorise les requêtes de liste (param = null si la requête n'a pas de paramètre)
    private List<Membre_Projet> findListBy(String sql, Integer param, String errorMessage) {
        List<Membre_Projet> liste = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            if (param != null) {
                statement.setInt(1, param);
            }
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    liste.add(map(result));
                }
            }
        } catch (SQLException e) {
            throw new DaoException(errorMessage, e);
        }
        return liste;
    }

    // Une ligne SQL -> un objet Membre_Projet
    private Membre_Projet map(ResultSet result) throws SQLException {
        Membre_Projet membreProjet = new Membre_Projet();
        membreProjet.setIDmem(result.getInt("IDmem"));
        membreProjet.setIDpro(result.getInt("IDpro"));
        membreProjet.setRolemem(Rolemem.valueOf(result.getString("rolemem")));
        return membreProjet;
    }
}