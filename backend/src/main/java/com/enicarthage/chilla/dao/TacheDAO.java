package com.enicarthage.chilla.dao;

import com.enicarthage.chilla.config.DatabaseConnection;
import com.enicarthage.chilla.exception.DaoException;
import com.enicarthage.chilla.model.Etattach;
import com.enicarthage.chilla.model.Tache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TacheDAO {

    // CREATE : remplit l'id généré dans l'objet
    // (si datecreatach est null, la base utilise CURRENT_TIMESTAMP)
    public void create(Tache tache) {
        String sql = "INSERT INTO Tache (labtach, descrtach, datecreatach, dateeche, estim, etattach, pos, "
                + "IDpro, IDmem, IDsp) VALUES (?, ?, COALESCE(?, CURRENT_TIMESTAMP), ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, tache.getLabtach());
            statement.setString(2, tache.getDescrtach());
            statement.setObject(3, tache.getDatecreatach());
            statement.setObject(4, tache.getDateeche());
            statement.setInt(5, tache.getEstim());
            statement.setString(6, tache.getEtat().name());
            statement.setInt(7, tache.getPos());
            statement.setInt(8, tache.getIDpro());
            statement.setInt(9, tache.getIDmem());
            statement.setInt(10, tache.getIDsp());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    tache.setIDtach(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la création de la tâche", e);
        }
    }

    // READ : toutes les tâches
    public List<Tache> findAll() {
        List<Tache> taches = new ArrayList<>();
        String sql = "SELECT * FROM Tache";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                taches.add(map(result));
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la lecture des tâches", e);
        }
        return taches;
    }

    // READ : une tâche par id (Optional.empty() si absente)
    public Optional<Tache> findById(int id) {
        String sql = "SELECT * FROM Tache WHERE IDtach = ?";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return Optional.of(map(result));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la lecture de la tâche " + id, e);
        }
        return Optional.empty();
    }

    // READ : les tâches d'un projet
    public List<Tache> findByProjet(int idProjet) {
        return findListBy("SELECT * FROM Tache WHERE IDpro = ? ORDER BY pos", idProjet,
                "Erreur lors de la lecture des tâches du projet " + idProjet);
    }

    // READ : les tâches d'un sprint
    public List<Tache> findBySprint(int idSprint) {
        return findListBy("SELECT * FROM Tache WHERE IDsp = ? ORDER BY pos", idSprint,
                "Erreur lors de la lecture des tâches du sprint " + idSprint);
    }

    // READ : les tâches assignées à un membre
    public List<Tache> findByMembre(int idMembre) {
        return findListBy("SELECT * FROM Tache WHERE IDmem = ? ORDER BY dateeche", idMembre,
                "Erreur lors de la lecture des tâches du membre " + idMembre);
    }

    // UPDATE : retourne true si une ligne a été modifiée
    public boolean update(Tache tache) {
        String sql = "UPDATE Tache SET labtach = ?, descrtach = ?, datecreatach = COALESCE(?, datecreatach), "
                + "dateeche = ?, estim = ?, etattach = ?, pos = ?, IDpro = ?, IDmem = ?, IDsp = ? "
                + "WHERE IDtach = ?";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, tache.getLabtach());
            statement.setString(2, tache.getDescrtach());
            statement.setObject(3, tache.getDatecreatach());
            statement.setObject(4, tache.getDateeche());
            statement.setInt(5, tache.getEstim());
            statement.setString(6, tache.getEtat().name());
            statement.setInt(7, tache.getPos());
            statement.setInt(8, tache.getIDpro());
            statement.setInt(9, tache.getIDmem());
            statement.setInt(10, tache.getIDsp());
            statement.setInt(11, tache.getIDtach());
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la modification de la tâche " + tache.getIDtach(), e);
        }
    }

    // DELETE : retourne true si une ligne a été supprimée
    public boolean delete(int id) {
        String sql = "DELETE FROM Tache WHERE IDtach = ?";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la suppression de la tâche " + id, e);
        }
    }

    // Factorise les requêtes "liste filtrée par un entier"
    private List<Tache> findListBy(String sql, int param, String errorMessage) {
        List<Tache> taches = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, param);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    taches.add(map(result));
                }
            }
        } catch (SQLException e) {
            throw new DaoException(errorMessage, e);
        }
        return taches;
    }

    // Une ligne SQL -> un objet Tache
    private Tache map(ResultSet result) throws SQLException {
        Tache tache = new Tache();
        tache.setIDtach(result.getInt("IDtach"));
        tache.setLabtach(result.getString("labtach"));
        tache.setDescrtach(result.getString("descrtach"));
        tache.setDatecreatach(result.getObject("datecreatach", LocalDate.class));
        tache.setDateeche(result.getObject("dateeche", LocalDate.class));
        tache.setEstim(result.getInt("estim"));
        tache.setEtat(Etattach.valueOf(result.getString("etattach")));
        tache.setPos(result.getInt("pos"));
        tache.setIDpro(result.getInt("IDpro"));
        tache.setIDmem(result.getInt("IDmem"));
        tache.setIDsp(result.getInt("IDsp"));
        return tache;
    }
}