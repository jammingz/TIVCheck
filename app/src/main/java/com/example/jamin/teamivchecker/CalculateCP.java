package com.example.jamin.teamivchecker;

import android.content.Context;
import android.util.Log;

public class CalculateCP {
    private DatabaseHelper dbHelper;
    private Context mContext;
    private static final String TAG = "CalculateCP";

    public CalculateCP(Context context) {
        mContext = context;
        dbHelper = new DatabaseHelper(context);
    }

    public void connectToDB() {
        dbHelper.connect();
    }

    public void close() {
        dbHelper.close();
    }

    public int calculate(PGoPokemon pkmn, double level, double cpm) {
        int atkIV = 15; // default IV for perfect pokemon
        int defIV = 15; // default IV for perfect pokemon
        int staIV = 15; // default IV for perfect pokemon

        int baseAtk = (int) Math.round(pkmn.getAtk());
        int baseDef = (int) Math.round(pkmn.getDef());
        int baseSta = (int) Math.round(pkmn.getSta());


        /*
        System.out.println("CalculateCP(): " +
                            "baseAtk: " + String.valueOf(baseAtk) + ", " +
                            "baseDef: " + String.valueOf(baseDef) + ", " +
                            "baseSta: " + String.valueOf(baseSta) + ", " +
                            "cpm: " + String.valueOf(cpm));
        */

        // Calculate the CP.
        int cp = (int) Math.floor((baseAtk+atkIV) * Math.sqrt(baseDef + defIV) * Math.sqrt(baseSta + staIV) * Math.pow(cpm, 2) / 10.0);
        // System.out.println("CP: " + String.valueOf(cp) + ", ATK: " + String.valueOf(baseAtk) + ", DEF:" + String.valueOf(baseDef) + ", STA: " + String.valueOf(baseSta));
        return cp; // Default value
    }

    public int calculate(PGoPokemon pkmn, double level, int atkIV, int defIV, int staIV, double cpm) {
        int baseAtk = (int) Math.round(pkmn.getAtk());
        int baseDef = (int) Math.round(pkmn.getDef());
        int baseSta = (int) Math.round(pkmn.getSta());

        /*
        System.out.println("CalculateCP(): " +
                            "baseAtk: " + String.valueOf(baseAtk) + ", " +
                            "baseDef: " + String.valueOf(baseDef) + ", " +
                            "baseSta: " + String.valueOf(baseSta) + ", " +
                            "cpm: " + String.valueOf(cpm));
        */

        // Calculate the CP.
        int cp = (int) Math.floor((baseAtk+atkIV) * Math.sqrt(baseDef + defIV) * Math.sqrt(baseSta + staIV) * Math.pow(cpm, 2) / 10.0);
        // System.out.println("CP: " + String.valueOf(cp) + ", ATK: " + String.valueOf(baseAtk) + ", DEF:" + String.valueOf(baseDef) + ", STA: " + String.valueOf(baseSta));
        return cp; // Default value
    }


    public int calculateCPByName(String name, double level) {
        Pokemon pkmn = dbHelper.selectPokemonByName(name);
        double cpm = dbHelper.selectCpmByLevel(level);
        PGoPokemon niaPkmn = convertToNiaPokemon(pkmn);
        double cp = calculate(niaPkmn, level, cpm);
        System.out.println(name + "(" + String.valueOf(level) + "): " + String.valueOf(cp));
        return (int) Math.round(cp);
    }

    public int calculateCPById(int id, double level) {
        Pokemon pkmn = dbHelper.selectPokemonById(id);
        double cpm = dbHelper.selectCpmByLevel(level);
        PGoPokemon niaPkmn = convertToNiaPokemon(pkmn);
        return calculate(niaPkmn, level, cpm);
    }

    public double getBaseStat(int phystat, int spstat, int speed) {
        double speedMod = 1 + ((double)speed - 75.0)/500.0;
        int lower = 0;
        int higher = 0;

        if (phystat > spstat) {
            higher = phystat;
            lower = spstat;
        } else {
            higher = spstat;
            lower = phystat;
        }

        Log.d(TAG, "Higher: " + String.valueOf(higher) + ", Lower: " + String.valueOf(lower));
        double scaledAtk = Math.round(2.0 * (7.0/8.0 * higher + lower / 8.0));
        Log.d(TAG,"Scaled: " + String.valueOf(scaledAtk));
        Log.d(TAG,"Speed Mod: " + String.valueOf(speedMod));
        return scaledAtk * speedMod;
    }

    public double getBaseAttack(int attack, int spatk, int speed) {
        return getBaseStat(attack, spatk, speed);
    }

    public double getBaseDefense(int defense, int spdef, int speed) {
        return getBaseStat(defense, spdef, speed);
    }

    public int getBaseStamina(int hp) { return 2 * hp;}

    public PGoPokemon convertToNiaPokemon(Pokemon pkmn) {
        int id = pkmn.getId();
        String name = pkmn.getName();
        int hp = pkmn.getHp();
        int attack = pkmn.getAttack();
        int defense = pkmn.getDefense();
        int spAttack = pkmn.getSpAttack();
        int spDefense = pkmn.getSpDefense();
        int speed = pkmn.getSpeed();
        boolean legendary = pkmn.isLegendary();
        int type1 = pkmn.getType1();
        int type2 = pkmn.getType2();
        int gen = pkmn.getGeneration();

        // Converting original stats into Pokemon Go's base attack, defense, and stamina stats.
        double baseAtk = getBaseAttack(attack, spAttack, speed);
        double baseDef = getBaseDefense(defense, spDefense, speed);
        double baseSta = getBaseStamina(hp);

        // Adjust stats for nerfed pkmn. Multiply every stat by 0.9 if it's nerfed
        if (isNerfed(id)) {
            baseAtk *= 0.91;
            baseDef *= 0.91;
            baseSta *= 0.91;
        }

        PGoPokemon newPokemon = new PGoPokemon(id, name, type1, type2, baseSta, baseAtk, baseDef, gen, legendary);
        return newPokemon;
    }

    /*
     * Special list for nerfed pkmn
     * - Mewtwo (gen1)
     * - Ho-oh (gen2)
     * - Groudon (gen3)
     * - Kyogre (gen3)
     * - Rayquaza (gen3)
     * - Slaking (gen3)
     */
    public boolean isNerfed(int id) {
        int MEWTWO = 150;
        int HOOH = 250;
        int SLAKING = 289;
        int KYOGRE = 382;
        int GROUDON = 383;
        int RAYQUAZA = 384;

        int DIALGA = 483;
        int PALKIA = 484;
        int GIRATINA = 487;
        int RESHIRAM = 643;
        int ZEKROM = 644;
        int KYUREM = 646;

        int[] nerfedArray = {
                MEWTWO,
                HOOH,
                SLAKING,
                KYOGRE,
                GROUDON,
                RAYQUAZA

                , DIALGA,
                PALKIA,
                GIRATINA,
                RESHIRAM,
                ZEKROM,
                KYUREM
        };


        for (int i : nerfedArray) {
            if (id == i) {
                return true; // yes, this id for the pokemon is nerfed
            }
        }

        // not part of the nerf pool
        return false;
    }

    public void getIVsFromCP(String name, int CP) {
        Pokemon pkmn = dbHelper.selectPokemonByName(name);
        PGoPokemon niaPkmn = convertToNiaPokemon(pkmn);

        // Iterate across all 4096 IVs
        for (double level = 0.0; level <= 40.0; level += 0.5) {
            // Fetching cpm from database based off of pokemon's level
            double cpm = dbHelper.selectCpmByLevel(level);
            for (int atkIV = 0; atkIV < 16; atkIV++) {
                for (int defIV = 0; defIV < 16; defIV++) {
                    for (int staIV = 0; staIV < 16; staIV++) {
                        int curCP = calculate(niaPkmn, level, atkIV, defIV, staIV, cpm);
                        if (curCP == CP) {
                            System.out.println("[Level: " + String.valueOf(level) + ", IVs:(" + String.valueOf(atkIV) + "/" + String.valueOf(defIV) + "/" + String.valueOf(staIV) +" )]");
                        }
                    }
                }
            }
        }

        Log.d(TAG,"Complete!");
    }


    /*
    public void main(String[] args) {
        // calculateCPByName("Alakazam", 40);
        // calculateCPById(386, 40);
        // getIVsFromCP("Chansey", 829);
        calculateCPByName("Zapdos", 15);

    }
    */

}