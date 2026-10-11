package com.enicarthage.chilla.model;
import java.time.LocalDate;

public class Notification {
    private int IDnotif;
    private String message;
    private LocalDate datecreanotif;
    private boolean estlue;
    private int IDmem;

    public Notification() {
        this.IDnotif = 0;
        this.message = "";
        this.datecreanotif = null;
        this.estlue = false;
        this.IDmem = 0;
    }

    //Getters and Setters
    public int getIDnotif() {
        return this.IDnotif;
    }

    public void setIDnotif(int IDnotif) {
        this.IDnotif = IDnotif;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDate getDateCreaNotif() {
        return this.datecreanotif;
    }

    public void setDateCreaNotif(LocalDate datecreanotif) {
        this.datecreanotif = datecreanotif;
    }

    public boolean getEstlue() {
        return this.estlue;
    }

    public void setEstlue(boolean estlue) {
        this.estlue = estlue;
    }

    public int getIDmem() {
        return this.IDmem;
    }

    public void setIDmem(int IDmem) {
        this.IDmem = IDmem;
    }

    //Override toString() for better representation
    @Override 
    public String toString() {
        return "Notification{" +
                "IDnotif='" + IDnotif + "'" +
                ", message='" + message + "'" +
                ", datecreanotif='" + datecreanotif + "'" +
                ", estlue='" + estlue + "'" +
                ", IDmem='" + IDmem + "'" +
                '}';
    }
}
