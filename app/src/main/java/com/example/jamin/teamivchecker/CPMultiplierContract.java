package com.example.jamin.teamivchecker;

import android.provider.BaseColumns;

public class CPMultiplierContract {

    private CPMultiplierContract() {}

    public static class CPMEntry implements BaseColumns {
        public static final String TABLE_NAME = "CPM";
        public static final String COLUMN_NAME_LEVEL = "Level";
        public static final String COLUMN_NAME_MULTIPLIER = "Multiplier";
    }
}
