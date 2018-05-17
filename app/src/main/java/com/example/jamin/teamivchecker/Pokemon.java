package com.example.jamin.teamivchecker;

public class Pokemon {
    private int id;
    private String name;
    private int type1;
    private int type2;
    private int total;
    private int hp;
    private int atk;
    private int def;
    private int spatk;
    private int spdef;
    private int spd;
    private int gen;
    private boolean legendary;

    public Pokemon() {
        this.id = 0;
        this.name = null;
        this.type1 = -1;
        this.type2 = -1;
        this.total = 0;
        this.hp = 0;
        this.atk = 0;
        this.def = 0;
        this.spatk = 0;
        this.spdef = 0;
        this.spd = 0;
        this.gen = 0;
        this.legendary = false;
    }

    public Pokemon(int pid, String pname, int ptype1, int ptype2, int ptotal, int php, int patk, int pdef, int pspatk, int pspdef, int pspd, int pgen, boolean plegendary) {
        this.id = pid;
        this.name = pname;
        this.type1 = ptype1;
        this.type2 = ptype2;
        this.total = ptotal;
        this.hp = php;
        this.atk = patk;
        this.def = pdef;
        this.spatk = pspatk;
        this.spdef = pspdef;
        this.spd = pspd;
        this.gen = pgen;
        this.legendary = plegendary;
    }


    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public int getType1() {
        return this.type1;
    }

    public int getType2() {
        return this.type2;
    }

    public int getTotalStats() {
        return this.total;
    }

    public int getHp() {return this.hp;}

    public int getAttack() {
        return this.atk;
    }

    public int getDefense() {
        return this.def;
    }

    public int getSpAttack() {
        return this.spatk;
    }

    public int getSpDefense() {
        return this.spdef;
    }

    public int getSpeed() {
        return this.spd;
    }

    public int getGeneration() {
        return this.gen;
    }

    public boolean isLegendary() {
        return this.legendary;
    }
}
