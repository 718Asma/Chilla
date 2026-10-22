package com.enicarthage.chilla.dao;

import com.enicarthage.chilla.config.DatabaseConnection;
import com.enicarthage.chilla.exception.DaoException;
import com.enicarthage.chilla.model.Etatsp;
import com.enicarthage.chilla.model.Sprint;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SprintDAO {

    // CREATE : remplit l'id généré dans l'objet
    public void create(Sprint sprint) {
        String sql = "INSERT INTO Sprint (nomsp, objectifsp, datedsp, datefsp, etatsp, IDpro) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, sprint.getNomsp());
            statement.setString(2, sprint.getObjectifsp());
            statement.setObject(3, sprint.getDatedsp());
            statement.setObject(4, sprint.getDatefsp());
            statement.setString(5, sprint.getEtat().name());
            statement.setInt(6, sprint.getIDpro());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    sprint.setIDsp(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la création du sprint", e);
        }
    }

    // READ : tous les sprints
    public List<Sprint> findAll() {
        List<Sprint> sprints = new ArrayList<>();
        String sql = "SELECT * FROM Sprint";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                sprints.add(map(result));
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la lecture des sprints", e);
        }
        return sprints;
    }

    // READ : un sprint par id (Optional.empty() si absent)
    public Optional<Sprint> findById(int id) {
        String sql = "SELECT * FROM Sprint WHERE IDsp = ?";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return Optional.of(map(result));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la lecture du sprint " + id, e);
        }
        return Optional.empty();
    }

    // READ : tous les sprints d'un projet
    public List<Sprint> findByProjet(int idProjet) {
        List<Sprint> sprints = new ArrayList<>();
        String sql = "SELECT * FROM Sprint WHERE IDpro = ? ORDER BY datedsp";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idProjet);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    sprints.add(map(result));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la lecture des sprints du projet " + idProjet, e);
        }
        return sprints;
    }

    // UPDATE : retourne true si une ligne a été modifiée
    public boolean update(Sprint sprint) {
        String sql = "UPDATE Sprint SET nomsp = ?, objectifsp = ?, datedsp = ?, "
                + "datefsp = ?, etatsp = ?, IDpro = ? WHERE IDsp = ?";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, sprint.getNomsp());
            statement.setString(2, sprint.getObjectifsp());
            statement.setObject(3, sprint.getDatedsp());
            statement.setObject(4, sprint.getDatefsp());
            statement.setString(5, sprint.getEtat().name());
            statement.setInt(6, sprint.getIDpro());
            statement.setInt(7, sprint.getIDsp());
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la modification du sprint " + sprint.getIDsp(), e);
        }
    }

    // DELETE : retourne true si une ligne a été supprimée
    public boolean delete(int id) {
        String sql = "DELETE FROM Sprint WHERE IDsp = ?";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la suppression du sprint " + id, e);
        }
    }

    // Une ligne SQL -> un objet Sprint
    private Sprint map(ResultSet result) throws SQLException {
        Sprint sprint = new Sprint();
        sprint.setIDsp(result.getInt("IDsp"));
        sprint.setNomsp(result.getString("nomsp"));
        sprint.setObjectifsp(result.getString("objectifsp"));
        sprint.setDatedsp(result.getObject("datedsp", LocalDate.class));
        sprint.setDatefsp(result.getObject("datefsp", LocalDate.class));
        sprint.setEtat(Etatsp.valueOf(result.getString("etatsp")));
        sprint.setIDpro(result.getInt("IDpro"));
        return sprint;
    }
}