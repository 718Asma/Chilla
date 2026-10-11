package com.enicarthage.chilla.model;
import java.time.LocalDate;

public class Membre {
    private int IDmem;
    private String nommem;
    private String prenommem;
    private String emailmem;
    private String motdepassemem;
    private LocalDate datecreamem;

    public Membre() {
        this.IDmem = 0;
        this.nommem = "";
        this.prenommem = "";
        this.emailmem = "";
        this.motdepassemem = "";
        this.datecreamem = null;
    }

    // Getters and Setters
    public int getIDmem() {
        return IDmem;
    }

    public void setIDmem(int IDmem) {
        this.IDmem = IDmem;
    }

    public String getNommem() {
        return nommem;
    }

    public void setNommem(String nommem) {
        this.nommem = nommem;
    }

    public String getPrenommem() {
        return prenommem;
    }

    public void setPrenommem(String prenommem) {
        this.prenommem = prenommem;
    }

    public String getEmailmem() {
        return emailmem;
    }

    public void setEmailmem(String emailmem) {
        this.emailmem = emailmem;
    }

    public String getMotdepassemem() {
        return motdepassemem;
    }

    public void setMotdepassemem(String motdepassemem) {
        this.motdepassemem = motdepassemem;
    }

    public LocalDate getDatecreamem() {
        return datecreamem;
    }

    public void setDatecreamem(LocalDate datecreamem) {
        this.datecreamem = datecreamem;
    }

    // Override toString() method for better representation
    @Override
    public String toString() {
        return "Membre{" +
                "IDmem='" + IDmem +"'" +
                ",\n nommem='" + nommem + "'" +
                ",\n prenommem='" + prenommem + "'" +
                ",\n emailmem='" + emailmem + "'" +
                ",\n motdepassemem='" + motdepassemem + "'" +
                ",\n datecreamem='" + datecreamem +"'"+
                '}';
    }
}
