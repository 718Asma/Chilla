package com.enicarthage.chilla.model;

public class Membre_Projet {
    private int IDmem;
    private int IDpro;
    private Rolemem rolemem;

    public Membre_Projet() {
        this.IDmem = 0;
        this.IDpro = 0;
        this.rolemem = Rolemem.MEMBRE;
    }

    //Getters and Setters
    public int getIDmem() {
        return this.IDmem;
    }

    public void setIDmem(int IDmem) {
        this.IDmem = IDmem;
    }

    public int getIDpro() {
        return this.IDpro;
    }

    public void setIDpro(int IDpro) {
        this.IDpro = IDpro;
    }

    public Rolemem getRolemem() {
        return this.rolemem;
    }

    public void setRolemem(Rolemem rolemem) {
        this.rolemem = rolemem;
    }

    //Override toString() for better representation
    @Override 
    public String toString() {
        return "Membre_Projet{" +
                "IDmem='" + IDmem + "'" +
                ", IDpro='" + IDpro + "'" +
                ", rolemem='" + rolemem + "'" +
                '}';
    }
}
