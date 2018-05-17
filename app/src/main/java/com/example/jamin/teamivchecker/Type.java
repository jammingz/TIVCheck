package com.example.jamin.teamivchecker;


import android.util.Log;

public class Type {
    public static final int NONE = 0;
    public static final int FIRE = 1;
    public static final int GRASS = 2;
    public static final int WATER = 3;
    public static final int ROCK = 4;
    public static final int GROUND = 5;
    public static final int FLYING = 6;
    public static final int DARK = 7;
    public static final int STEEL = 8;
    public static final int FAIRY = 9;
    public static final int BUG = 10;
    public static final int DRAGON = 11;
    public static final int FIGHTING = 12;
    public static final int POISON = 13;
    public static final int ELECTRIC = 14;
    public static final int PSYCHIC = 15;
    public static final int NORMAL = 16;

    private static final String TAG = "Type";

    public static String getName(int num) {
        switch(num){
            case NONE: return "NONE";
            case FIRE: return "FIRE";
            case GRASS: return "GRASS";
            case WATER: return "WATER";
            case ROCK: return "ROCK";
            case GROUND: return "GROUND";
            case FLYING: return "FLYING";
            case DARK: return "DARK";
            case STEEL: return "STEEL";
            case FAIRY: return "FAIRY";
            case BUG: return "BUG";
            case DRAGON: return "DRAGON";
            case FIGHTING: return "FIGHTING";
            case POISON: return "POISON";
            case ELECTRIC: return "ELECTRIC";
            case PSYCHIC: return "PSYCHIC";
            case NORMAL: return "NORMAL";
        }
        return "Not valid typing";
    }

    public static int getType(String name) {
        switch (name.toLowerCase()) {
            case "fire": return FIRE;
            case "grass": return GRASS;
            case "water": return WATER;
            case "rock": return ROCK;
            case "ground": return GROUND;
            case "flying": return FLYING;
            case "dark": return DARK;
            case "steel": return STEEL;
            case "fairy": return FAIRY;
            case "bug": return BUG;
            case "dragon": return DRAGON;
            case "fighting": return FIGHTING;
            case "poison": return POISON;
            case "electric": return  ELECTRIC;
            case "psychic": return PSYCHIC;
            case "normal": return NORMAL;
        }

        Log.d(TAG, "WARNING: Cannot find Typing. Returning NONE");
        return NONE;
    }
}
