package com.example.jamin.teamivchecker;

import android.provider.BaseColumns;

public class PokemonStatContract {

    private PokemonStatContract() {}

    public static class PokemonStatEntry implements BaseColumns {
        public static final String TABLE_NAME = "PkmnStats";
        public static final String COLUMN_NAME_PKMN_NAME = "Name";
        public static final String COLUMN_NAME_TYPE1 = "Type1";
        public static final String COLUMN_NAME_TYPE2 = "Type2";
        public static final String COLUMN_NAME_TOTAL = "Total";
        public static final String COLUMN_NAME_HP = "Hp";
        public static final String COLUMN_NAME_ATTACK = "Attack";
        public static final String COLUMN_NAME_DEFENSE = "Defense";
        public static final String COLUMN_NAME_SP_ATTACK = "SpAttack";
        public static final String COLUMN_NAME_SP_DEFENSE = "SpDefense";
        public static final String COLUMN_NAME_SPEED = "Speed";
        public static final String COLUMN_NAME_GENERATION = "Generation";
        public static final String COLUMN_NAME_LEGENDARY = "Legendary";
    }
}
