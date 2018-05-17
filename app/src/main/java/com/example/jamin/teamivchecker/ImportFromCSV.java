package com.example.jamin.teamivchecker;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ImportFromCSV {
    private static int importFromFile(String dir) {
        BufferedReader br = null;
        String line = "";
        String csvSplit = ",";
        int counter = 0; // First version, we only import unique Pokemons. Different forms are ignored

        try {
            br = new BufferedReader(new FileReader(dir));

            while ((line = br.readLine()) != null) {
                String[] results = line.split(csvSplit);
                if (results.length > 2) {
                    try {
                        int currentID = Integer.parseInt(results[0]);
                        if (currentID > counter) {
                            // New pokemon and is not a different form of previous pokemon

                            SQLiteDriverConnection conn = new SQLiteDriverConnection();
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

                            conn.insertStats(name, type1, type2, total, hp, atk, def, spatk, spdef, spd, gen, legendary);
                            counter++;

                            System.out.println("Importing: " + name);
                        }

                    } catch (NumberFormatException e) {
                        System.out.println("Skipping header of csv");
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

    private static int importCPMFromFile(String dir) {
        BufferedReader br = null;
        String line = "";
        String csvSplit = ",";

        try {
            br = new BufferedReader(new FileReader(dir));

            while ((line = br.readLine()) != null) {
                String[] results = line.split(csvSplit);
                if (results.length > 1) {
                    try {
                        double level = Double.parseDouble(results[0]);
                        double cpm = Double.parseDouble(results[1]);

                        SQLiteDriverConnection conn = new SQLiteDriverConnection();
                        conn.createNewCpmTable();
                        conn.insertCpm(level, cpm);
                    } catch (NumberFormatException e) {
                        System.out.println("Skipping header of csv");
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


    public static void main(String[] args) {
        importFromFile(args[0]);
        importCPMFromFile(args[1]);
    }
}
