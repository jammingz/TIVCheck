package com.example.jamin.teamivchecker;

import android.provider.BaseColumns;

public class NiaPokemonStatContract {

    private NiaPokemonStatContract() {}

    public static class NiaPokemonStatEntry implements BaseColumns {
        public static final String TABLE_NAME = "NiaPkmnStats";
        public static final String COLUMN_NAME_PKMN_NAME = "Name";
        public static final String COLUMN_NAME_LEVEL = "Level";
        public static final String COLUMN_NAME_CP = "Cp";
        public static final String COLUMN_NAME_ATKIV = "Atkiv";
        public static final String COLUMN_NAME_DEFIV = "Defiv";
        public static final String COLUMN_NAME_STAIV = "Staiv";
        public static final String COLUMN_NAME_IVPERCENT = "IvPercent"; // percent in terms of the total ivs out of the max 48(15/15/15)

        /*
        public static final String COLUMN_NAME_TYPE1 = "Type1";
        public static final String COLUMN_NAME_TYPE2 = "Type2";
        public static final String COLUMN_NAME_MAX_CP = "MaxCP";
        public static final String COLUMN_NAME_STAMINA = "Stamina";
        public static final String COLUMN_NAME_ATTACK = "Attack";
        public static final String COLUMN_NAME_DEFENSE = "Defense";
        public static final String COLUMN_NAME_GENERATION = "Generation";
        public static final String COLUMN_NAME_LEGENDARY = "Legendary";
        */
    }

}
