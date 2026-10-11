package com.enicarthage.chilla.model;
import java.time.LocalDate;

public class Sprint {
    private int IDsp;
    private String nomsp;
    private String objectifsp;
    private LocalDate datedsp;
    private LocalDate datefsp;
    private Etatsp etat;
    private int IDpro;

    public Sprint() {
        this.IDsp = 0;
        this.nomsp = "";
        this.objectifsp = "";
        this.datedsp = null;
        this.datefsp = null;
        this.etat = Etatsp.NON_COMMENCE;
        this.IDpro = 0;
    }

    // Getters and Setters
    public int getIDsp() {
        return IDsp;
    }

    public void setIDsp(int IDsp) {
        this.IDsp = IDsp;
    }

    public String getNomsp() {
        return nomsp;
    }

    public void setNomsp(String nomsp) {
        this.nomsp = nomsp;
    }

    public String getObjectifsp() {
        return objectifsp;
    }

    public void setObjectifsp(String objectifsp) {
        this.objectifsp = objectifsp;
    }

    public LocalDate getDatedsp() {
        return datedsp;
    }

    public void setDatedsp(LocalDate datedsp) {
        this.datedsp = datedsp;
    }

    public LocalDate getDatefsp() {
        return datefsp;
    }

    public void setDatefsp(LocalDate datefsp) {
        this.datefsp = datefsp;
    }

    public Etatsp getEtat() {
        return etat;
    }

    public void setEtat(Etatsp etat) {
        this.etat = etat;
    }

    public int getIDpro() {
        return IDpro;
    }

    public void setIDpro(int IDpro) {
        this.IDpro = IDpro;
    }

    // Override toString() method for better representation
    @Override
    public String toString() {
        return "Sprint{" +
                "IDsp='" + IDsp + "'" +
                ",\n nomsp='" + nomsp + "'" +
                ",\n objectifsp='" + objectifsp + "'" +
                ",\n datedsp='" + datedsp +"'" +
                ",\n datefsp='" + datefsp +"'" +
                ",\n etatsp='" + etat +"'" +
                ",\n IDpro='" + IDpro + "'" +
                '}';
    }
}
