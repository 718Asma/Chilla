package com.enicarthage.chilla.model;
import java.time.LocalDate;

public class Tache {
    private int IDtach;
    private String labtach;
    private String descrtach;
    private LocalDate datecreatach;
    private LocalDate dateeche;
    private int estim;
    private Etattach etat;
    private int pos;
    private int IDpro;
    private int IDmem;
    private int IDsp;

    public Tache() {
        this.IDtach = 0;
        this.labtach = "";
        this.descrtach = "";
        this.datecreatach = null;
        this.dateeche = null;
        this.estim = 0;
        this.etat = Etattach.NON_COMMENCE;
        this.pos = 0;
        this.IDpro = 0;
        this.IDmem = 0;
        this.IDsp = 0;
    }

    // Getters and Setters
    public int getIDtach() {
        return IDtach;
    }

    public void setIDtach(int IDtach) {
        this.IDtach = IDtach;
    }

    public String getLabtach() {
        return labtach;
    }

    public void setLabtach(String labtach) {
        this.labtach = labtach;
    }

    public String getDescrtach() {
        return descrtach;
    }

    public void setDescrtach(String descrtach) {
        this.descrtach = descrtach;
    }

    public LocalDate getDatecreatach() {
        return datecreatach;
    }

    public void setDatecreatach(LocalDate datecreatach) {
        this.datecreatach = datecreatach;
    }

    public LocalDate getDateeche() {
        return dateeche;
    }

    public void setDateeche(LocalDate dateeche) {
        this.dateeche = dateeche;
    }

    public int getEstim() {
        return estim;
    }

    public void setEstim(int estim) {
        this.estim = estim;
    }

    public Etattach getEtat() {
        return etat;
    }

    public void setEtat(Etattach etat) {
        this.etat = etat;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public int getIDpro() {
        return IDpro;
    }

    public void setIDpro(int IDpro) {
        this.IDpro = IDpro;
    }

    public int getIDmem() {
        return IDmem;
    }

    public void setIDmem(int IDmem) {
        this.IDmem = IDmem;
    }

    public int getIDsp() {
        return IDsp;
    }

    public void setIDsp(int IDsp) {
        this.IDsp = IDsp;
    }

    // Override toString() method for better representation
    @Override
    public String toString() {
        return "Tache{" +
                "IDtach='" + IDtach +"'" +
                ",\n labtach='" + labtach + "'" +
                ",\n descrtach='" + descrtach + "'" +
                ",\n datecreatach='" + datecreatach +"'" +
                ",\n dateeche='" + dateeche +"'" +
                ",\n estim='" + estim +"'" +
                ",\n etat='" + etat +"'" +
                ",\n pos='" + pos +"'" +
                ",\n IDpro='" + IDpro + "'" +
                ",\n IDmem='" + IDmem + "'" +
                ",\n IDsp='" + IDsp +"'"+
                '}';
    }
}
