package com.example.jamin.teamivchecker;

public class PokemonIVs {
    private int atk;
    private int def;
    private int sta;
    private double level;
    private int CP;
    private String name; // dex number


    public PokemonIVs(int a, int d, int s, double l, int c, String n) {
        atk = a;
        def = d;
        sta = s;
        level = l;
        CP = c;
        name = n;
    }

    public double getLevel() {
        return level;
    }

    public int getAtk() {
        return atk;
    }

    public int getCP() {
        return CP;
    }

    public int getDef() {
        return def;
    }

    public String getName() {
        return name;
    }

    public int getSta() {
        return sta;
    }
}
