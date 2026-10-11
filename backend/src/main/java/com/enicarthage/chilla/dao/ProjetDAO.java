package com.enicarthage.chilla.dao;

import com.enicarthage.chilla.config.DatabaseConnection;
import com.enicarthage.chilla.exception.DaoException;
import com.enicarthage.chilla.model.Projet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProjetDAO {

    // CREATE : remplit l'id généré dans l'objet
    public void create(Projet projet) {
        String sql = "INSERT INTO Projet (labpro, descrpro, datedpro, datefpro, estarchive) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, projet.getLabpro());
            statement.setString(2, projet.getDescrpro());
            statement.setObject(3, projet.getDatedpro());
            statement.setObject(4, projet.getDatefpro());
            statement.setBoolean(5, projet.getEstarchive());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    projet.setIDpro(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la création du projet", e);
        }
    }

    // READ : tous les projets
    public List<Projet> findAll() {
        List<Projet> projets = new ArrayList<>();
        String sql = "SELECT * FROM Projet";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                projets.add(map(result));
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la lecture des projets", e);
        }
        return projets;
    }

    // READ : un projet par id (Optional.empty() si absent)
    public Optional<Projet> findById(int id) {
        String sql = "SELECT * FROM Projet WHERE IDpro = ?";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return Optional.of(map(result));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la lecture du projet " + id, e);
        }
        return Optional.empty();
    }

    // UPDATE : retourne true si une ligne a été modifiée
    public boolean update(Projet projet) {
        String sql = "UPDATE Projet SET labpro = ?, descrpro = ?, datedpro = ?, "
                + "datefpro = ?, estarchive = ? WHERE IDpro = ?";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, projet.getLabpro());
            statement.setString(2, projet.getDescrpro());
            statement.setObject(3, projet.getDatedpro());
            statement.setObject(4, projet.getDatefpro());
            statement.setBoolean(5, projet.getEstarchive());
            statement.setInt(6, projet.getIDpro());
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la modification du projet " + projet.getIDpro(), e);
        }
    }

    // DELETE : retourne true si une ligne a été supprimée
    public boolean delete(int id) {
        String sql = "DELETE FROM Projet WHERE IDpro = ?";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la suppression du projet " + id, e);
        }
    }

    // Une ligne SQL -> un objet Projet (évite de répéter ce code dans findAll et
    // findById)
    private Projet map(ResultSet result) throws SQLException {
        Projet projet = new Projet();
        projet.setIDpro(result.getInt("IDpro"));
        projet.setLabpro(result.getString("labpro"));
        projet.setDescrpro(result.getString("descrpro"));
        projet.setDatedpro(result.getObject("datedpro", LocalDate.class));
        projet.setDatefpro(result.getObject("datefpro", LocalDate.class));
        projet.setEstarchive(result.getBoolean("estarchive"));
        return projet;
    }
}