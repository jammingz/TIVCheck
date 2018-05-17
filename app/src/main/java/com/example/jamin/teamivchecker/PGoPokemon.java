package com.example.jamin.teamivchecker;

// For creating Pokemon GO's version of Pokemon Objects

public class PGoPokemon {
    private int id;
    private String name;
    private int type1;
    private int type2;
    private double sta;
    private double atk;
    private double def;
    private int gen;
    private boolean legendary;

    public PGoPokemon() {
        this.id = 0;
        this.name = null;
        this.type1 = -1;
        this.type2 = -2;
        this.sta = 0.0;
        this.atk = 0.0;
        this.def = 0.0;
        this.gen = 0;
        this.legendary = false;
    }

    public PGoPokemon(int pid, String pname, int ptype1, int ptype2, double psta, double patk, double pdef, int pgen, boolean plegendary) {
        this.id = pid;
        this.name = pname;
        this.type1 = ptype1;
        this.type2 = ptype2;
        this.sta = psta;
        this.atk = patk;
        this.def = pdef;
        this.gen = pgen;
        this.legendary = plegendary;
    }

    public int getId() { return id;}

    public String getName() {
        return name;
    }

    public int getType1() {
        return type1;
    }

    public int getType2() {
        return type2;
    }

    public double getSta() {
        return sta;
    }

    public double getAtk() {
        return atk;
    }

    public double getDef() {
        return def;
    }

    public int getGen() {
        return gen;
    }

    public boolean isLegendary() {
        return legendary;
    }
}
