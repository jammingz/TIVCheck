package com.example.jamin.teamivchecker;

import java.sql.*;

public class SQLiteDriverConnection {
    public static final String url = "jdbc:sqlite:stats.db";

    public SQLiteDriverConnection() {

    }

    private Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);

            System.out.println("Connection to SQLite Esbalished");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public void createNewTable() {
        String sql = "CREATE TABLE IF NOT EXISTS stats (\n"
                + "id integer PRIMARY KEY,\n"
                + "name text NOT NULL,\n"
                + "type1 integer NOT NULL,\n"
                + "type2 integer,\n"
                + "total integer NOT NULL, \n"
                + "hp integer NOT NULL,\n"
                + "atk integer NOT NULL,\n"
                + "def integer NOT NULL,\n"
                + "spatk integer NOT NULL,\n"
                + "spdef integer NOT NULL,\n"
                + "spd integer NOT NULL,\n"
                + "generation integer NOT NULL,\n"
                + "legendary integer NOT NULL\n"
                + ");";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()
        ) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // This table is for converted Monsters.
    public void createNewNiaTable() {
        String sql = "CREATE TABLE IF NOT EXISTS converted (\n"
                + "id integer PRIMARY KEY,\n"
                + "name text NOT NULL,\n"
                + "type1 integer NOT NULL,\n"
                + "type2 integer,\n"
                + "maxCP integer NOT NULL, \n"
                + "sta real NOT NULL,\n"
                + "atk real NOT NULL,\n"
                + "def real NOT NULL,\n"
                + "generation integer NOT NULL,\n"
                + "legendary integer NOT NULL\n"
                + ");";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()
        ) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void insertNiaStats(String name, int type1, int type2, int cp, double sta, double atk, double def, int gen, boolean legendary) {
        String sql = "INSERT INTO stats(name, type1, type2, maxCP, sta, atk, def, generation, legendary) VALUES(?,?,?,?,?,?,?,?,?)";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int isLegendary = 0;
            if (legendary) {
                isLegendary = 1;
            }
            pstmt.setString(1, name);
            pstmt.setInt(2, type1);
            pstmt.setInt(3, type2);
            pstmt.setInt(4, cp);
            pstmt.setDouble(5, sta);
            pstmt.setDouble(6, atk);
            pstmt.setDouble(7, def);
            pstmt.setInt(11, gen);
            pstmt.setInt(12, isLegendary);
            pstmt.executeUpdate();

            System.out.println("Inserting Nia entry: " + name);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    public void insertStats(String name, int type1, int type2, int total, int hp, int atk, int def, int spatk, int spdef, int spd, int gen, boolean legendary) {
        String sql = "INSERT INTO stats(name, type1, type2, total, hp, atk, def, spatk, spdef, spd, generation, legendary) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int isLegendary = 0;
            if (legendary) {
                isLegendary = 1;
            }
            pstmt.setString(1, name);
            pstmt.setInt(2, type1);
            pstmt.setInt(3, type2);
            pstmt.setInt(4, total);
            pstmt.setInt(5, hp);
            pstmt.setInt(6, atk);
            pstmt.setInt(7, def);
            pstmt.setInt(8, spatk);
            pstmt.setInt(9, spdef);
            pstmt.setInt(10, spd);
            pstmt.setInt(11, gen);
            pstmt.setInt(12, isLegendary);
            pstmt.executeUpdate();

            System.out.println("Inserting entry: " + name + " , [Type1: " + Type.getName(type1) + " , Type2: " + Type.getName(type2) + "]");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public Pokemon selectPokemonByName(String pokemonName) {
        String sql = "SELECT id, name, type1, type2, total, hp, atk, def, spatk, spdef, spd, generation, legendary FROM stats WHERE name = ?";
        Pokemon pkmn = new Pokemon();
        int duplicateCounter = 0; // Keeps track of number of results. We're expecting only 1 result. More than 1 means something's wrong

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set value
            pstmt.setString(1, pokemonName.toLowerCase());

            ResultSet res = pstmt.executeQuery();

            // Loop through result set
            while (res.next()) {
                boolean isLegend = false;
                if (res.getInt("legendary") == 1) {
                    isLegend = true;
                }
                pkmn = new Pokemon(res.getInt("id"),
                        res.getString("name"),
                        res.getInt("type1"),
                        res.getInt("type2"),
                        res.getInt("total"),
                        res.getInt("hp"),
                        res.getInt("atk"),
                        res.getInt("def"),
                        res.getInt("spatk"),
                        res.getInt("spdef"),
                        res.getInt("spd"),
                        res.getInt("generation"),
                        isLegend);

                duplicateCounter++;
            }

            if (duplicateCounter > 1 ) {
                // Something's wrong. We're returning more than 1 entries
                System.out.println("More than 1 results found! Unintentional! Returning last entry");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return pkmn;
    }

    public Pokemon selectPokemonById(int pokemonId) {
        String sql = "SELECT id, name, type1, type2, total, hp, atk, def, spatk, spdef, spd, generation, legendary FROM stats WHERE id = ?";
        Pokemon pkmn = new Pokemon();
        int duplicateCounter = 0; // Keeps track of number of results. We're expecting only 1 result. More than 1 means something's wrong

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set value
            pstmt.setInt(1, pokemonId);

            ResultSet res = pstmt.executeQuery();

            // Loop through result set
            while (res.next()) {
                boolean isLegend = false;
                if (res.getInt("legendary") == 1) {
                    isLegend = true;
                }
                pkmn = new Pokemon(res.getInt("id"),
                        res.getString("name"),
                        res.getInt("type1"),
                        res.getInt("type2"),
                        res.getInt("total"),
                        res.getInt("hp"),
                        res.getInt("atk"),
                        res.getInt("def"),
                        res.getInt("spatk"),
                        res.getInt("spdef"),
                        res.getInt("spd"),
                        res.getInt("generation"),
                        isLegend);

                duplicateCounter++;
            }

            if (duplicateCounter == 0) {
                // No results found
                System.out.println("No results found!");
            } else if (duplicateCounter > 1 ) {
                // Something's wrong. We're returning more than 1 entries
                System.out.println("More than 1 results found! Unintentional! Returning last entry");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return pkmn;
    }

    public void printPokemonObject(Pokemon pkmn) {
        System.out.println("Printing Pokemon Object: {" + pkmn.getName() + "(" + Integer.toString(pkmn.getId()) + "): " + " [" + Type.getName(pkmn.getType1()) + "/" + Type.getName(pkmn.getType2()) + "]}");
    }


    // CRUD for CPMs

    public void createNewCpmTable() {
        String sql = "CREATE TABLE IF NOT EXISTS cpm (\n"
                + "id integer PRIMARY KEY,\n"
                + "level real NOT NULL,\n"
                + "multiplier real NOT NULL\n"
                + ");";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()
        ) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void insertCpm(double level, double cpm) {
        String sql = "INSERT INTO cpm(level, multiplier) VALUES(?,?)";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1,level);
            pstmt.setDouble(2, cpm);
            pstmt.executeUpdate();

            System.out.println("Inserting cpm to database: [Level: " + String.valueOf(level) + " , CPM: " + String.valueOf(cpm) + "]");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void insertNiaPkmn(PGoPokemon pkmn) {
        String sql = "INSERT INTO converted(name, type1, type2, maxCP, sta, atk, def, generation, legendary) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int maxCP = CalculateCP.calculateCP(pkmn, 40.0);
            int isLegend = 0;
            if (pkmn.isLegendary()) {
                isLegend = 1;
            }
            pstmt.setString(1, pkmn.getName());
            pstmt.setInt(2, pkmn.getType1());
            pstmt.setInt(3, pkmn.getType2());
            pstmt.setInt(4, maxCP);
            pstmt.setDouble(5, pkmn.getSta());
            pstmt.setDouble(6, pkmn.getAtk());
            pstmt.setDouble(7, pkmn.getDef());
            pstmt.setInt(8, pkmn.getGen());
            pstmt.setInt(9, isLegend);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public double selectCpmByLevel(double level) {
        String sql = "SELECT multiplier FROM cpm WHERE id = ?";
        int duplicateCounter = 0; // Keeps track of number of results. We're expecting only 1 result. More than 1 means something's wrong
        int id = ((int) (level * 2)) - 1;
        double cpm = 0.0;

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set value
            pstmt.setInt(1, id);
            ResultSet res = pstmt.executeQuery();

            // Loop through result set
            while (res.next()) {
                cpm = res.getDouble(1);
                duplicateCounter++;
            }

            if (duplicateCounter > 1 ) {
                // Something's wrong. We're returning more than 1 entries
                System.out.println("More than 1 Cpm results found! Unintentional! Returning last entry");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return cpm;
    }

    public static void main(String[] args) {
        // DEBUGGING:

        // connect();
        SQLiteDriverConnection conn = new SQLiteDriverConnection();
        conn.createNewTable();
        conn.createNewCpmTable();
        conn.createNewNiaTable();

        // Pokemon test = conn.selectPokemonById(1);
        //conn.printPokemonObject(test);

        //System.out.println("CPM: " + String.valueOf(conn.selectCpmByLevel(40)));


        // conn.createNewTable();
        // conn.insertStats("test1", Type.BUG, Type.DRAGON, 502, 50, 50, 50, 50, 50, 50, 2, false);
    }

}
