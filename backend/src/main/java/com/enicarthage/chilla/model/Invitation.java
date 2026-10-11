package com.enicarthage.chilla.model;

public class Invitation {
    private int IDinvit;
    private Statut statut;
    private String dateevoi;
    private int IDpro;
    private int IDemetteur;
    private int IDrecepteur;

    public Invitation() {
        this.IDinvit = 0;
        this.statut = Statut.EN_ATTENTE;
        this.dateevoi = "";
        this.IDpro = 0;
        this.IDemetteur = 0;
        this.IDrecepteur = 0;
    }

    // Getters and Setters
    public int getIDinvit() {
        return IDinvit;
    }

    public void setIDinvit(int IDinvit) {
        this.IDinvit = IDinvit;
    }

    public Statut getStatut() {
        return statut;
    }

    public void setStatut(Statut statut) {
        this.statut = statut;
    }

    public String getDateevoi() {
        return dateevoi;
    }

    public void setDateevoi(String dateevoi) {
        this.dateevoi = dateevoi;
    }

    public int getIDpro() {
        return IDpro;
    }

    public void setIDpro(int IDpro) {
        this.IDpro = IDpro;
    }

    public int getIDemetteur() {
        return IDemetteur;
    }

    public void setIDemetteur(int IDemetteur) {
        this.IDemetteur = IDemetteur;
    }

    public int getIDrecepteur() {
        return IDrecepteur;
    }

    public void setIDrecepteur(int IDrecepteur) {
        this.IDrecepteur = IDrecepteur;
    }

    // Override toString() method for debugging purposes
    @Override
    public String toString() {
        return "Invitation{" +
                "IDinvit='" + IDinvit + "'" +
                ", statut='" + statut + "'" +
                ", dateevoi='" + dateevoi + "'" +
                ", IDpro='" + IDpro + "'" +
                ", IDemetteur='" + IDemetteur + "'" +
                ", IDrecepteur='" + IDrecepteur + "'" +
                '}';
    }
}
