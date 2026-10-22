package com.enicarthage.chilla.dao;

import com.enicarthage.chilla.config.DatabaseConnection;
import com.enicarthage.chilla.exception.DaoException;
import com.enicarthage.chilla.model.Membre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MembreDAO {

    // CREATE : remplit l'id généré dans l'objet
    // (si datecreamem est null, la base utilise CURRENT_TIMESTAMP)
    public void create(Membre membre) {
        String sql = "INSERT INTO Membre (nommem, prenommem, emailmem, motdepassemem, datecreamem) "
                + "VALUES (?, ?, ?, ?, COALESCE(?, CURRENT_TIMESTAMP))";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, membre.getNommem());
            statement.setString(2, membre.getPrenommem());
            statement.setString(3, membre.getEmailmem());
            statement.setString(4, membre.getMotdepassemem());
            statement.setObject(5, membre.getDatecreamem());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    membre.setIDmem(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la création du membre", e);
        }
    }

    // READ : tous les membres
    public List<Membre> findAll() {
        List<Membre> membres = new ArrayList<>();
        String sql = "SELECT * FROM Membre";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                membres.add(map(result));
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la lecture des membres", e);
        }
        return membres;
    }

    // READ : un membre par id (Optional.empty() si absent)
    public Optional<Membre> findById(int id) {
        String sql = "SELECT * FROM Membre WHERE IDmem = ?";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return Optional.of(map(result));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la lecture du membre " + id, e);
        }
        return Optional.empty();
    }

    // READ : un membre par email (utile pour la connexion)
    public Optional<Membre> findByEmail(String email) {
        String sql = "SELECT * FROM Membre WHERE emailmem = ?";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return Optional.of(map(result));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la lecture du membre " + email, e);
        }
        return Optional.empty();
    }

    // UPDATE : retourne true si une ligne a été modifiée
    public boolean update(Membre membre) {
        String sql = "UPDATE Membre SET nommem = ?, prenommem = ?, emailmem = ?, "
                + "motdepassemem = ?, datecreamem = COALESCE(?, datecreamem) WHERE IDmem = ?";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, membre.getNommem());
            statement.setString(2, membre.getPrenommem());
            statement.setString(3, membre.getEmailmem());
            statement.setString(4, membre.getMotdepassemem());
            statement.setObject(5, membre.getDatecreamem());
            statement.setInt(6, membre.getIDmem());
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la modification du membre " + membre.getIDmem(), e);
        }
    }

    // DELETE : retourne true si une ligne a été supprimée
    public boolean delete(int id) {
        String sql = "DELETE FROM Membre WHERE IDmem = ?";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la suppression du membre " + id, e);
        }
    }

    // Une ligne SQL -> un objet Membre
    private Membre map(ResultSet result) throws SQLException {
        Membre membre = new Membre();
        membre.setIDmem(result.getInt("IDmem"));
        membre.setNommem(result.getString("nommem"));
        membre.setPrenommem(result.getString("prenommem"));
        membre.setEmailmem(result.getString("emailmem"));
        membre.setMotdepassemem(result.getString("motdepassemem"));
        membre.setDatecreamem(result.getObject("datecreamem", LocalDate.class));
        return membre;
    }
}