package com.example.jamin.teamivchecker;


// CRUD methods for accessing the database

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseHelper {
    private static final String TAG = "DatabaseHelper";

    // Database constants
    private static final String TEXT_TYPE = " TEXT";
    private static final String REAL_TYPE = " REAL";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";

    private SQLiteDatabase pokemonWriteDB;
    private SQLiteDatabase pokemonReadDB;
    private SQLiteDatabase niaPokemonWriteDB;
    private SQLiteDatabase niaPokemonReadDB;
    private SQLiteDatabase cpmWriteDB;
    private SQLiteDatabase cpmReadDB;

    private boolean isConnected;
    private Context mContext;

    // Private constructor
    protected DatabaseHelper(Context context) {
        isConnected = false;
        mContext = context;
    }


    public void connect() {
        SQLiteOpenHelper helper1 = new PokemonDBHelper(mContext);
        pokemonReadDB = helper1.getReadableDatabase();
        pokemonWriteDB = helper1.getWritableDatabase();

        SQLiteOpenHelper helper2 = new NiaPokemonDBHelper(mContext);
        niaPokemonReadDB = helper2.getReadableDatabase();
        niaPokemonWriteDB = helper2.getWritableDatabase();

        SQLiteOpenHelper helper3 = new CPMultiplierDBHelper(mContext);
        cpmWriteDB = helper3.getWritableDatabase();
        cpmReadDB = helper3.getReadableDatabase();

        isConnected = true;
    }

    public void close() {
        if (isConnected) {
            pokemonReadDB.close();
            pokemonWriteDB.close();
            niaPokemonReadDB.close();
            niaPokemonWriteDB.close();
            cpmReadDB.close();
            cpmWriteDB.close();
            isConnected = false;
        }
    }


    // Read Methods

    public Pokemon selectPokemonByName(String pokemonName) {
        Pokemon pkmn = new Pokemon();

        SQLiteDatabase db = pokemonReadDB;

        String[] projection = new String[] {
                PokemonStatContract.PokemonStatEntry._ID,
                PokemonStatContract.PokemonStatEntry.COLUMN_NAME_PKMN_NAME,
                PokemonStatContract.PokemonStatEntry.COLUMN_NAME_TYPE1,
                PokemonStatContract.PokemonStatEntry.COLUMN_NAME_TYPE2,
                PokemonStatContract.PokemonStatEntry.COLUMN_NAME_TOTAL,
                PokemonStatContract.PokemonStatEntry.COLUMN_NAME_HP,
                PokemonStatContract.PokemonStatEntry.COLUMN_NAME_ATTACK,
                PokemonStatContract.PokemonStatEntry.COLUMN_NAME_DEFENSE,
                PokemonStatContract.PokemonStatEntry.COLUMN_NAME_SP_ATTACK,
                PokemonStatContract.PokemonStatEntry.COLUMN_NAME_SP_DEFENSE,
                PokemonStatContract.PokemonStatEntry.COLUMN_NAME_SPEED,
                PokemonStatContract.PokemonStatEntry.COLUMN_NAME_GENERATION,
                PokemonStatContract.PokemonStatEntry.COLUMN_NAME_LEGENDARY
        };


        String sortOrder = null;
        String selection = PokemonStatContract.PokemonStatEntry.COLUMN_NAME_PKMN_NAME + " = ?"; // Looking for row with this column name
        String[] selectionArgs = {pokemonName};

        Cursor cursor = db.query(
                PokemonStatContract.PokemonStatEntry.TABLE_NAME,      // The table to query
                projection,                                           // The columns to return
                selection,                                            // The columns for the WHERE clause
                selectionArgs,                                        // The values for the WHERE clause
                null,                                       // don't group the rows
                null,                                         // don't filter by row groups
                sortOrder                                             // The sort order
        );

        int count = cursor.getCount();
        Log.d(TAG, "Length of cursor: " + String.valueOf(count));

        if (count > 0 ) {
            cursor.moveToFirst();
            boolean isLegend = false;
            if (cursor.getInt(cursor.getColumnIndex(PokemonStatContract.PokemonStatEntry.COLUMN_NAME_GENERATION)) == 1) {
                isLegend = true;
            }

            pkmn = new Pokemon(cursor.getInt(cursor.getColumnIndex(PokemonStatContract.PokemonStatEntry._ID)),
                    cursor.getString(cursor.getColumnIndex(PokemonStatContract.PokemonStatEntry.COLUMN_NAME_PKMN_NAME)),
                    cursor.getInt(cursor.getColumnIndex(PokemonStatContract.PokemonStatEntry.COLUMN_NAME_TYPE1)),
                    cursor.getInt(cursor.getColumnIndex(PokemonStatContract.PokemonStatEntry.COLUMN_NAME_TYPE2)),
                    cursor.getInt(cursor.getColumnIndex(PokemonStatContract.PokemonStatEntry.COLUMN_NAME_TOTAL)),
                    cursor.getInt(cursor.getColumnIndex(PokemonStatContract.PokemonStatEntry.COLUMN_NAME_HP)),
                    cursor.getInt(cursor.getColumnIndex(PokemonStatContract.PokemonStatEntry.COLUMN_NAME_ATTACK)),
                    cursor.getInt(cursor.getColumnIndex(PokemonStatContract.PokemonStatEntry.COLUMN_NAME_DEFENSE)),
                    cursor.getInt(cursor.getColumnIndex(PokemonStatContract.PokemonStatEntry.COLUMN_NAME_SP_ATTACK)),
                    cursor.getInt(cursor.getColumnIndex(PokemonStatContract.PokemonStatEntry.COLUMN_NAME_SP_DEFENSE)),
                    cursor.getInt(cursor.getColumnIndex(PokemonStatContract.PokemonStatEntry.COLUMN_NAME_SPEED)),
                    cursor.getInt(cursor.getColumnIndex(PokemonStatContract.PokemonStatEntry.COLUMN_NAME_GENERATION)),
                    isLegend);

            cursor.close();
        }

        return pkmn;
    }


    public Pokemon selectPokemonById(int pokemonId) {
        Pokemon pkmn = new Pokemon();

        SQLiteDatabase db = pokemonReadDB;

        String[] projection = new String[] {
                PokemonStatContract.PokemonStatEntry._ID,
                PokemonStatContract.PokemonStatEntry.COLUMN_NAME_PKMN_NAME,
                PokemonStatContract.PokemonStatEntry.COLUMN_NAME_TYPE1,
                PokemonStatContract.PokemonStatEntry.COLUMN_NAME_TYPE2,
                PokemonStatContract.PokemonStatEntry.COLUMN_NAME_TOTAL,
                PokemonStatContract.PokemonStatEntry.COLUMN_NAME_HP,
                PokemonStatContract.PokemonStatEntry.COLUMN_NAME_ATTACK,
                PokemonStatContract.PokemonStatEntry.COLUMN_NAME_DEFENSE,
                PokemonStatContract.PokemonStatEntry.COLUMN_NAME_SP_ATTACK,
                PokemonStatContract.PokemonStatEntry.COLUMN_NAME_SP_DEFENSE,
                PokemonStatContract.PokemonStatEntry.COLUMN_NAME_SPEED,
                PokemonStatContract.PokemonStatEntry.COLUMN_NAME_GENERATION,
                PokemonStatContract.PokemonStatEntry.COLUMN_NAME_LEGENDARY
        };


        String sortOrder = null;
        String selection = PokemonStatContract.PokemonStatEntry._ID + " = ?"; // Looking for row with this column name
        String[] selectionArgs = {String.valueOf(pokemonId)};

        Cursor cursor = db.query(
                PokemonStatContract.PokemonStatEntry.TABLE_NAME,      // The table to query
                projection,                                           // The columns to return
                selection,                                            // The columns for the WHERE clause
                selectionArgs,                                        // The values for the WHERE clause
                null,                                       // don't group the rows
                null,                                         // don't filter by row groups
                sortOrder                                             // The sort order
        );

        int count = cursor.getCount();
        Log.d(TAG, "Length of cursor: " + String.valueOf(count));

        if (count > 0 ) {
            cursor.moveToFirst();
            boolean isLegend = false;
            if (cursor.getInt(cursor.getColumnIndex(PokemonStatContract.PokemonStatEntry.COLUMN_NAME_GENERATION)) == 1) {
                isLegend = true;
            }

            pkmn = new Pokemon(cursor.getInt(cursor.getColumnIndex(PokemonStatContract.PokemonStatEntry._ID)),
                    cursor.getString(cursor.getColumnIndex(PokemonStatContract.PokemonStatEntry.COLUMN_NAME_PKMN_NAME)),
                    cursor.getInt(cursor.getColumnIndex(PokemonStatContract.PokemonStatEntry.COLUMN_NAME_TYPE1)),
                    cursor.getInt(cursor.getColumnIndex(PokemonStatContract.PokemonStatEntry.COLUMN_NAME_TYPE2)),
                    cursor.getInt(cursor.getColumnIndex(PokemonStatContract.PokemonStatEntry.COLUMN_NAME_TOTAL)),
                    cursor.getInt(cursor.getColumnIndex(PokemonStatContract.PokemonStatEntry.COLUMN_NAME_HP)),
                    cursor.getInt(cursor.getColumnIndex(PokemonStatContract.PokemonStatEntry.COLUMN_NAME_ATTACK)),
                    cursor.getInt(cursor.getColumnIndex(PokemonStatContract.PokemonStatEntry.COLUMN_NAME_DEFENSE)),
                    cursor.getInt(cursor.getColumnIndex(PokemonStatContract.PokemonStatEntry.COLUMN_NAME_SP_ATTACK)),
                    cursor.getInt(cursor.getColumnIndex(PokemonStatContract.PokemonStatEntry.COLUMN_NAME_SP_DEFENSE)),
                    cursor.getInt(cursor.getColumnIndex(PokemonStatContract.PokemonStatEntry.COLUMN_NAME_SPEED)),
                    cursor.getInt(cursor.getColumnIndex(PokemonStatContract.PokemonStatEntry.COLUMN_NAME_GENERATION)),
                    isLegend);

            cursor.close();
        }

        return pkmn;
    }


    public double selectCpmByLevel(double level) {
        double cpm = 0.0;
        int id = ((int) (level * 2)) - 1; // converting level to the row's id
        SQLiteDatabase db = cpmReadDB;



        String[] projection = new String[] {
                CPMultiplierContract.CPMEntry._ID,
                CPMultiplierContract.CPMEntry.COLUMN_NAME_LEVEL,
                CPMultiplierContract.CPMEntry.COLUMN_NAME_MULTIPLIER
        };

        String sortOrder = null;
        String selection = CPMultiplierContract.CPMEntry._ID + " = ?"; // Looking for row with this column name
        String[] selectionArgs = {String.valueOf(id)};

        Cursor cursor = db.query(
                CPMultiplierContract.CPMEntry.TABLE_NAME,      // The table to query
                projection,                                           // The columns to return
                selection,                                            // The columns for the WHERE clause
                selectionArgs,                                        // The values for the WHERE clause
                null,                                       // don't group the rows
                null,                                         // don't filter by row groups
                sortOrder                                             // The sort order
        );


        int count = cursor.getCount();
        Log.d(TAG, "Length of CPM cursor: " + String.valueOf(count));

        if (count > 0 ) {
            cursor.moveToFirst();
            cpm = cursor.getDouble(cursor.getColumnIndex(CPMultiplierContract.CPMEntry.COLUMN_NAME_MULTIPLIER));
            cursor.close();
        }

        return cpm;
    }



    // Write Methods

    public void insertStats(String name, int type1, int type2, int total, int hp, int atk, int def, int spatk, int spdef, int spd, int gen, boolean legendary) {
        if (!isConnected) {
            Log.d(TAG, "Not connected to DB!");
            return;
        }

        int isLegendary = 0;
        if (legendary) {
            isLegendary = 1;
        }

        SQLiteDatabase db = pokemonWriteDB;

        ContentValues values = new ContentValues();
        values.put(PokemonStatContract.PokemonStatEntry.COLUMN_NAME_PKMN_NAME, name);
        values.put(PokemonStatContract.PokemonStatEntry.COLUMN_NAME_TYPE1, type1);
        values.put(PokemonStatContract.PokemonStatEntry.COLUMN_NAME_TYPE2, type2);
        values.put(PokemonStatContract.PokemonStatEntry.COLUMN_NAME_TOTAL, total);
        values.put(PokemonStatContract.PokemonStatEntry.COLUMN_NAME_HP, hp);
        values.put(PokemonStatContract.PokemonStatEntry.COLUMN_NAME_ATTACK, atk);
        values.put(PokemonStatContract.PokemonStatEntry.COLUMN_NAME_DEFENSE, def);
        values.put(PokemonStatContract.PokemonStatEntry.COLUMN_NAME_SP_ATTACK, spatk);
        values.put(PokemonStatContract.PokemonStatEntry.COLUMN_NAME_SP_DEFENSE, spdef);
        values.put(PokemonStatContract.PokemonStatEntry.COLUMN_NAME_SPEED, spd);
        values.put(PokemonStatContract.PokemonStatEntry.COLUMN_NAME_GENERATION, gen);
        values.put(PokemonStatContract.PokemonStatEntry.COLUMN_NAME_LEGENDARY, isLegendary);


        // Insert the new row,
        db.insert(PokemonStatContract.PokemonStatEntry.TABLE_NAME, null, values);
        Log.d(TAG, "Inserting entry: " + name);
    }


    public void insertNiaStats(String name, int type1, int type2, int cp, double sta, double atk, double def, int gen, boolean legendary) {
        if (!isConnected) {
            Log.d(TAG, "Not connected to DB!");
            return;
        }

        int isLegendary = 0;
        if (legendary) {
            isLegendary = 1;
        }

        SQLiteDatabase db = niaPokemonWriteDB;

        ContentValues values = new ContentValues();
        values.put(NiaPokemonStatContract.NiaPokemonStatEntry.COLUMN_NAME_PKMN_NAME, name);
        values.put(NiaPokemonStatContract.NiaPokemonStatEntry.COLUMN_NAME_TYPE1, type1);
        values.put(NiaPokemonStatContract.NiaPokemonStatEntry.COLUMN_NAME_TYPE2, type2);
        values.put(NiaPokemonStatContract.NiaPokemonStatEntry.COLUMN_NAME_MAX_CP, cp);
        values.put(NiaPokemonStatContract.NiaPokemonStatEntry.COLUMN_NAME_STAMINA, sta);
        values.put(NiaPokemonStatContract.NiaPokemonStatEntry.COLUMN_NAME_ATTACK, atk);
        values.put(NiaPokemonStatContract.NiaPokemonStatEntry.COLUMN_NAME_DEFENSE, def);
        values.put(NiaPokemonStatContract.NiaPokemonStatEntry.COLUMN_NAME_GENERATION, gen);
        values.put(NiaPokemonStatContract.NiaPokemonStatEntry.COLUMN_NAME_LEGENDARY, isLegendary);


        // Insert the new row,
        db.insert(NiaPokemonStatContract.NiaPokemonStatEntry.TABLE_NAME, null, values);

        Log.d(TAG, "Inserting Nia entry: " + name);
    }

    public void insertNiaPkmn(PGoPokemon pkmn) {
        CalculateCP calculator = new CalculateCP(mContext);
        int maxCP = calculator.calculate(pkmn, 40.0);

        insertNiaStats(
                pkmn.getName(),
                pkmn.getType1(),
                pkmn.getType2(),
                maxCP,
                pkmn.getSta(),
                pkmn.getAtk(),
                pkmn.getDef(),
                pkmn.getGen(),
                pkmn.isLegendary()
        );
    }


    public void insertCpm(double level, double cpm) {
        if (!isConnected) {
            Log.d(TAG, "Not connected to DB!");
            return;
        }

        SQLiteDatabase db = cpmWriteDB;

        ContentValues values = new ContentValues();
        values.put(CPMultiplierContract.CPMEntry.COLUMN_NAME_LEVEL, level);
        values.put(CPMultiplierContract.CPMEntry.COLUMN_NAME_MULTIPLIER, cpm);


        // Insert the new row,
        db.insert(CPMultiplierContract.CPMEntry.TABLE_NAME, null, values);

        Log.d(TAG, "Inserting cpm for level: " + String.valueOf(level));
    }




    private class PokemonDBHelper extends SQLiteOpenHelper {

        // If you change the database schema, you must increment the database version.
        private final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + PokemonStatContract.PokemonStatEntry.TABLE_NAME + " (" +
                        PokemonStatContract.PokemonStatEntry._ID + " INTEGER PRIMARY KEY," +
                        PokemonStatContract.PokemonStatEntry.COLUMN_NAME_PKMN_NAME + TEXT_TYPE + COMMA_SEP +
                        PokemonStatContract.PokemonStatEntry.COLUMN_NAME_TYPE1 + INTEGER_TYPE + COMMA_SEP +
                        PokemonStatContract.PokemonStatEntry.COLUMN_NAME_TYPE2 + INTEGER_TYPE + COMMA_SEP +
                        PokemonStatContract.PokemonStatEntry.COLUMN_NAME_TOTAL + INTEGER_TYPE + COMMA_SEP +
                        PokemonStatContract.PokemonStatEntry.COLUMN_NAME_HP + INTEGER_TYPE + COMMA_SEP +
                        PokemonStatContract.PokemonStatEntry.COLUMN_NAME_ATTACK + INTEGER_TYPE + COMMA_SEP +
                        PokemonStatContract.PokemonStatEntry.COLUMN_NAME_DEFENSE + INTEGER_TYPE + COMMA_SEP +
                        PokemonStatContract.PokemonStatEntry.COLUMN_NAME_SP_ATTACK + INTEGER_TYPE + COMMA_SEP +
                        PokemonStatContract.PokemonStatEntry.COLUMN_NAME_SP_DEFENSE + INTEGER_TYPE + COMMA_SEP +
                        PokemonStatContract.PokemonStatEntry.COLUMN_NAME_SPEED + INTEGER_TYPE + COMMA_SEP +
                        PokemonStatContract.PokemonStatEntry.COLUMN_NAME_GENERATION + INTEGER_TYPE + COMMA_SEP +
                        PokemonStatContract.PokemonStatEntry.COLUMN_NAME_LEGENDARY + INTEGER_TYPE +
                        " )";

        private final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + PokemonStatContract.PokemonStatEntry.TABLE_NAME;

        private static final int DATABASE_VERSION = 1;
        private static final String DATABASE_NAME = "TeamIVChecker.db";


        private PokemonDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }


        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            Log.d(TAG, "PokemonDBHelper.onCreate() Called");
            sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            // Discard data and start over
            sqLiteDatabase.execSQL(SQL_DELETE_ENTRIES);
            onCreate(sqLiteDatabase);
        }
    }

    private class NiaPokemonDBHelper extends SQLiteOpenHelper {

        // If you change the database schema, you must increment the database version.
        private final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + NiaPokemonStatContract.NiaPokemonStatEntry.TABLE_NAME + " (" +
                        NiaPokemonStatContract.NiaPokemonStatEntry._ID + " INTEGER PRIMARY KEY," +
                        NiaPokemonStatContract.NiaPokemonStatEntry.COLUMN_NAME_PKMN_NAME + TEXT_TYPE + COMMA_SEP +
                        NiaPokemonStatContract.NiaPokemonStatEntry.COLUMN_NAME_TYPE1 + INTEGER_TYPE + COMMA_SEP +
                        NiaPokemonStatContract.NiaPokemonStatEntry.COLUMN_NAME_TYPE2 + INTEGER_TYPE + COMMA_SEP +
                        NiaPokemonStatContract.NiaPokemonStatEntry.COLUMN_NAME_MAX_CP + INTEGER_TYPE + COMMA_SEP +
                        NiaPokemonStatContract.NiaPokemonStatEntry.COLUMN_NAME_STAMINA + REAL_TYPE + COMMA_SEP +
                        NiaPokemonStatContract.NiaPokemonStatEntry.COLUMN_NAME_ATTACK + REAL_TYPE + COMMA_SEP +
                        NiaPokemonStatContract.NiaPokemonStatEntry.COLUMN_NAME_DEFENSE + REAL_TYPE + COMMA_SEP +
                        NiaPokemonStatContract.NiaPokemonStatEntry.COLUMN_NAME_GENERATION + INTEGER_TYPE + COMMA_SEP +
                        NiaPokemonStatContract.NiaPokemonStatEntry.COLUMN_NAME_LEGENDARY+ INTEGER_TYPE +
                        " )";

        private final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + NiaPokemonStatContract.NiaPokemonStatEntry.TABLE_NAME;

        private static final int DATABASE_VERSION = 1;
        private static final String DATABASE_NAME = "TeamIVChecker.db";


        private NiaPokemonDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }


        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            Log.d(TAG, "NiaPokemonDBHelper.onCreate() Called");
            sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            // Discard data and start over
            sqLiteDatabase.execSQL(SQL_DELETE_ENTRIES);
            onCreate(sqLiteDatabase);
        }
    }

    private class CPMultiplierDBHelper extends SQLiteOpenHelper {
        private final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + CPMultiplierContract.CPMEntry.TABLE_NAME + " (" +
                        CPMultiplierContract.CPMEntry._ID + " INTEGER PRIMARY KEY," +
                        CPMultiplierContract.CPMEntry.COLUMN_NAME_LEVEL + TEXT_TYPE + COMMA_SEP +
                        CPMultiplierContract.CPMEntry.COLUMN_NAME_MULTIPLIER + REAL_TYPE +
                        " )";

        private final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + CPMultiplierContract.CPMEntry.TABLE_NAME;

        private static final int DATABASE_VERSION = 1;
        private static final String DATABASE_NAME = "TeamIVChecker.db";


        private CPMultiplierDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }


        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            Log.d(TAG, "CPMultiplierDBHelper.onCreate() Called");
            sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            // Discard data and start over
            sqLiteDatabase.execSQL(SQL_DELETE_ENTRIES);
            onCreate(sqLiteDatabase);
        }
    }


}
