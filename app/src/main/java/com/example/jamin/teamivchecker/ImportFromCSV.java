package com.example.jamin.teamivchecker;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ImportFromCSV {
    private static final String TAG = "ImportFromCSV";
    private Context mContext;
    private DatabaseHelper mDBHelper;
    private static final String POKEMON_CSV_DIR = "Pokemon.csv";
    private static final String CPM_CSV_DIR = "cpm.csv";



    public ImportFromCSV(Context context) {
        mContext = context;
        mDBHelper = new DatabaseHelper(context);
        mDBHelper.connect();
    }

    public void close() {
        mDBHelper.close();
    }

    public int importFromCSV() {
        BufferedReader br = null;
        String line = "";
        String csvSplit = ",";
        int counter = 0; // First version, we only import unique Pokemons. Different forms are ignored

        try {
            InputStreamReader is = new InputStreamReader(mContext.getAssets().open(POKEMON_CSV_DIR));
            br = new BufferedReader(is);

            while ((line = br.readLine()) != null) {
                String[] results = line.split(csvSplit);
                if (results.length > 2) {
                    try {
                        int currentID = Integer.parseInt(results[0]);
                        if (currentID > counter) {
                            // New pokemon and is not a different form of previous pokemon

                            String name = results[1].toLowerCase();
                            int type1 = Type.getType(results[2]);
                            int type2 = Type.getType(results[3]);
                            int total = Integer.parseInt(results[4]);
                            int hp = Integer.parseInt(results[5]);
                            int atk = Integer.parseInt(results[6]);
                            int def = Integer.parseInt(results[7]);
                            int spatk = Integer.parseInt(results[8]);
                            int spdef = Integer.parseInt(results[9]);
                            int spd = Integer.parseInt(results[10]);
                            int gen = Integer.parseInt(results[11]);
                            boolean legendary = Boolean.parseBoolean(results[12]);

                            mDBHelper.insertStats(name, type1, type2, total, hp, atk, def, spatk, spdef, spd, gen, legendary);
                            counter++;

                            Log.d(TAG, "Importing: " + name);
                        }

                    } catch (NumberFormatException e) {
                        Log.d(TAG,"Skipping header of csv");
                    }
                }

            }

        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            return 0;
        } catch (IOException e2) {
            System.out.println(e2.getMessage());
            return 0;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e3) {
                    System.out.println(e3.getMessage());
                    return 0;
                }
            }
        }
        return 1;
    }

    public int importCPMFromCSV() {
        BufferedReader br = null;
        String line = "";
        String csvSplit = ",";

        try {
            InputStreamReader is = new InputStreamReader(mContext.getAssets().open(CPM_CSV_DIR));
            br = new BufferedReader(is);

            while ((line = br.readLine()) != null) {
                String[] results = line.split(csvSplit);
                if (results.length > 1) {
                    try {
                        double level = Double.parseDouble(results[0]);
                        double cpm = Double.parseDouble(results[1]);

                        mDBHelper.insertCpm(level, cpm);
                    } catch (NumberFormatException e) {
                        Log.d(TAG,"Skipping header of csv");
                    }
                }

            }

        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            return 0;
        } catch (IOException e2) {
            System.out.println(e2.getMessage());
            return 0;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e3) {
                    System.out.println(e3.getMessage());
                    return 0;
                }
            }
        }
        return 1;
    }

    public void exportToNiaDatabase(NotificationManager manager, Notification.Builder builder) {
        CalculateCP calculator = new CalculateCP(mContext);
       // for (int i = 1; i < 649; i++) { // Uncomment to import database for all gens

        int progressCheckPoint = 0;
        for (int i = 1; i <= 386; i++) {
            // Update notification progress every 2%
            if (i * 50 / 386 > progressCheckPoint) {
                progressCheckPoint++;
                builder.setProgress(100, progressCheckPoint * 2, false);
                manager.notify(MainButtonService.NOTIFICATION_ID, builder.build());
            }

            Pokemon pkmn = mDBHelper.selectPokemonById(i);
            PGoPokemon niaPkmn = calculator.convertToNiaPokemon(pkmn);
            Log.d(TAG, "Inserting Nia Pokemon dex: " + String.valueOf(i) + "(" + niaPkmn.getName() + ")");
            // Insert into database
            mDBHelper.insertNiaPkmn(niaPkmn);
        }

        mDBHelper.close();
    }


    /*
    public static void main(String[] args) {
        importFromFile(args[0]);
        importCPMFromFile(args[1]);
    }

    */
}
