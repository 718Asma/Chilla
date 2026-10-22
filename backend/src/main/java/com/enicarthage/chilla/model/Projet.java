package com.enicarthage.chilla.model;
import java.time.LocalDate;

public class Projet {
    private int IDpro;
    private String labpro;
    private String descrpro;
    private LocalDate datedpro;
    private LocalDate datefpro;
    private Boolean estarchive;
    
    public Projet() {
        this.IDpro = 0;
        this.labpro = "";
        this.descrpro = "";
        this.datedpro = null;
        this.datefpro = null;
        this.estarchive = false;
    }

    // Getters and Setters
    public int getIDpro() {
        return IDpro;
    }

    public void setIDpro(int IDpro) {
        this.IDpro = IDpro;
    }

    public String getLabpro() {
        return labpro;
    }

    public void setLabpro(String labpro) {
        this.labpro = labpro;
    }

    public String getDescrpro() {
        return descrpro;
    }

    public void setDescrpro(String descrpro) {
        this.descrpro = descrpro;
    }

    public LocalDate getDatedpro() {
        return datedpro;
    }

    public void setDatedpro(LocalDate datedpro) {
        this.datedpro = datedpro;
    }

    public LocalDate getDatefpro() {
        return datefpro;
    }

    public void setDatefpro(LocalDate datefpro) {
        this.datefpro = datefpro;
    }

    public Boolean getEstarchive() {
        return estarchive;
    }

    public void setEstarchive(Boolean estarchive) {
        this.estarchive = estarchive;
    }
    
    // Override toString() method for better representation
    @Override
    public String toString() {
        return "Projet {" +
                "IDpro :'" + IDpro + "'" +
                ",\nlabpro :'" + labpro + "'" +
                ",\ndescrpro :'" + descrpro + "'" +
                ",\ndatedpro :'" + datedpro + "'" +
                ",\ndatefpro :'" + datefpro + "'" +
                ",\nestarchive :'" + estarchive + "'" +
                '}';
    }
}

