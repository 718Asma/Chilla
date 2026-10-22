package com.enicarthage.chilla.dao;

import com.enicarthage.chilla.config.DatabaseConnection;
import com.enicarthage.chilla.exception.DaoException;
import com.enicarthage.chilla.model.Invitation;
import com.enicarthage.chilla.model.Statut;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InvitationDAO {

    // CREATE : remplit l'id généré dans l'objet
    // (si dateevoi est null, la base utilise CURRENT_TIMESTAMP)
    public void create(Invitation invitation) {
        String sql = "INSERT INTO Invitation (statut, dateevoi, IDpro, IDemetteur, IDrecepteur) "
                + "VALUES (?, COALESCE(?, CURRENT_TIMESTAMP), ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, invitation.getStatut().name());
            statement.setObject(2, invitation.getDateevoi());
            statement.setInt(3, invitation.getIDpro());
            statement.setInt(4, invitation.getIDemetteur());
            statement.setInt(5, invitation.getIDrecepteur());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    invitation.setIDinvit(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la création de l'invitation", e);
        }
    }

    // READ : toutes les invitations
    public List<Invitation> findAll() {
        List<Invitation> invitations = new ArrayList<>();
        String sql = "SELECT * FROM Invitation";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                invitations.add(map(result));
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la lecture des invitations", e);
        }
        return invitations;
    }

    // READ : une invitation par id (Optional.empty() si absente)
    public Optional<Invitation> findById(int id) {
        String sql = "SELECT * FROM Invitation WHERE IDinvit = ?";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return Optional.of(map(result));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la lecture de l'invitation " + id, e);
        }
        return Optional.empty();
    }

    // READ : les invitations reçues par un membre
    public List<Invitation> findByRecepteur(int idRecepteur) {
        List<Invitation> invitations = new ArrayList<>();
        String sql = "SELECT * FROM Invitation WHERE IDrecepteur = ? ORDER BY dateevoi DESC";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idRecepteur);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    invitations.add(map(result));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la lecture des invitations du membre " + idRecepteur, e);
        }
        return invitations;
    }

    // UPDATE : retourne true si une ligne a été modifiée
    public boolean update(Invitation invitation) {
        String sql = "UPDATE Invitation SET statut = ?, dateevoi = COALESCE(?, dateevoi), IDpro = ?, "
                + "IDemetteur = ?, IDrecepteur = ? WHERE IDinvit = ?";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, invitation.getStatut().name());
            statement.setObject(2, invitation.getDateevoi());
            statement.setInt(3, invitation.getIDpro());
            statement.setInt(4, invitation.getIDemetteur());
            statement.setInt(5, invitation.getIDrecepteur());
            statement.setInt(6, invitation.getIDinvit());
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la modification de l'invitation " + invitation.getIDinvit(), e);
        }
    }

    // DELETE : retourne true si une ligne a été supprimée
    public boolean delete(int id) {
        String sql = "DELETE FROM Invitation WHERE IDinvit = ?";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la suppression de l'invitation " + id, e);
        }
    }

    // Une ligne SQL -> un objet Invitation
    private Invitation map(ResultSet result) throws SQLException {
        Invitation invitation = new Invitation();
        invitation.setIDinvit(result.getInt("IDinvit"));
        invitation.setStatut(Statut.valueOf(result.getString("statut")));
        invitation.setDateevoi(result.getObject("dateevoi", LocalDate.class));
        invitation.setIDpro(result.getInt("IDpro"));
        invitation.setIDemetteur(result.getInt("IDemetteur"));
        invitation.setIDrecepteur(result.getInt("IDrecepteur"));
        return invitation;
    }
}