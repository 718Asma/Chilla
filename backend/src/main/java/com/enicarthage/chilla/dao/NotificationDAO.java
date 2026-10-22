package com.enicarthage.chilla.dao;

import com.enicarthage.chilla.config.DatabaseConnection;
import com.enicarthage.chilla.exception.DaoException;
import com.enicarthage.chilla.model.Notification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NotificationDAO {

    // CREATE : remplit l'id généré dans l'objet
    // (si datecreanotif est null, la base utilise CURRENT_TIMESTAMP)
    public void create(Notification notification) {
        String sql = "INSERT INTO Notification (message, datecreanotif, estlue, IDmem) "
                + "VALUES (?, COALESCE(?, CURRENT_TIMESTAMP), ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, notification.getMessage());
            statement.setObject(2, notification.getDateCreaNotif());
            statement.setBoolean(3, notification.getEstlue());
            statement.setInt(4, notification.getIDmem());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    notification.setIDnotif(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la création de la notification", e);
        }
    }

    // READ : toutes les notifications
    public List<Notification> findAll() {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM Notification";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                notifications.add(map(result));
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la lecture des notifications", e);
        }
        return notifications;
    }

    // READ : une notification par id (Optional.empty() si absente)
    public Optional<Notification> findById(int id) {
        String sql = "SELECT * FROM Notification WHERE IDnotif = ?";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return Optional.of(map(result));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la lecture de la notification " + id, e);
        }
        return Optional.empty();
    }

    // READ : les notifications d'un membre (les plus récentes d'abord)
    public List<Notification> findByMembre(int idMembre) {
        return findListBy("SELECT * FROM Notification WHERE IDmem = ? ORDER BY datecreanotif DESC", idMembre,
                "Erreur lors de la lecture des notifications du membre " + idMembre);
    }

    // READ : les notifications non lues d'un membre
    public List<Notification> findUnreadByMembre(int idMembre) {
        return findListBy(
                "SELECT * FROM Notification WHERE IDmem = ? AND estlue = FALSE ORDER BY datecreanotif DESC",
                idMembre, "Erreur lors de la lecture des notifications non lues du membre " + idMembre);
    }

    // UPDATE : retourne true si une ligne a été modifiée
    public boolean update(Notification notification) {
        String sql = "UPDATE Notification SET message = ?, datecreanotif = COALESCE(?, datecreanotif), "
                + "estlue = ?, IDmem = ? WHERE IDnotif = ?";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, notification.getMessage());
            statement.setObject(2, notification.getDateCreaNotif());
            statement.setBoolean(3, notification.getEstlue());
            statement.setInt(4, notification.getIDmem());
            statement.setInt(5, notification.getIDnotif());
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la modification de la notification "
                    + notification.getIDnotif(), e);
        }
    }

    // UPDATE : marque une notification comme lue
    public boolean markAsRead(int id) {
        String sql = "UPDATE Notification SET estlue = TRUE WHERE IDnotif = ?";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DaoException("Erreur lors du marquage de la notification " + id, e);
        }
    }

    // DELETE : retourne true si une ligne a été supprimée
    public boolean delete(int id) {
        String sql = "DELETE FROM Notification WHERE IDnotif = ?";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la suppression de la notification " + id, e);
        }
    }

    // Factorise les requêtes "liste filtrée par un entier"
    private List<Notification> findListBy(String sql, int param, String errorMessage) {
        List<Notification> notifications = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, param);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    notifications.add(map(result));
                }
            }
        } catch (SQLException e) {
            throw new DaoException(errorMessage, e);
        }
        return notifications;
    }

    // Une ligne SQL -> un objet Notification
    private Notification map(ResultSet result) throws SQLException {
        Notification notification = new Notification();
        notification.setIDnotif(result.getInt("IDnotif"));
        notification.setMessage(result.getString("message"));
        notification.setDateCreaNotif(result.getObject("datecreanotif", LocalDate.class));
        notification.setEstlue(result.getBoolean("estlue"));
        notification.setIDmem(result.getInt("IDmem"));
        return notification;
    }
}