
package bisu.tms;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private static final String DB_FOLDER = "data";
    private static final String DB_FILE = "bisu_tms.db";

    public static Connection getConnection() throws SQLException {

        File folder = new File(DB_FOLDER);

        if (!folder.exists() && !folder.mkdirs()) {
            throw new SQLException(
                "Unable to create database folder."
            );
        }

        File dbFile = new File(folder, DB_FILE);

        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();

        Connection conn = DriverManager.getConnection(url);

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        }

        return conn;
    }

    public static void initializeDatabase() throws SQLException {

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // USER ACCOUNTS
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT NOT NULL UNIQUE, " +
                "password_hash TEXT NOT NULL, " +
                "password_salt TEXT NOT NULL, " +
                "role TEXT NOT NULL DEFAULT 'ADMIN'" +
                ")"
            );

            // REGISTERED TEAMS
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS teams (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL UNIQUE, " +
                "sport TEXT, " +
                "coach TEXT, " +
                "created_at TEXT DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );

            // TOURNAMENTS
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS tournaments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "sport TEXT NOT NULL, " +
                "status TEXT NOT NULL DEFAULT 'PENDING', " +
                "created_at TEXT DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );

            // TEAMS JOINED TO TOURNAMENTS
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS tournament_teams (" +
                "tournament_id INTEGER NOT NULL, " +
                "team_id INTEGER NOT NULL, " +
                "seed INTEGER, " +
                "PRIMARY KEY (tournament_id, team_id), " +
                "FOREIGN KEY(tournament_id) " +
                "REFERENCES tournaments(id), " +
                "FOREIGN KEY(team_id) REFERENCES teams(id)" +
                ")"
            );

            // TOURNAMENT MATCHES
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS matches (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "tournament_id INTEGER NOT NULL, " +
                "round INTEGER NOT NULL, " +
                "match_number INTEGER NOT NULL, " +
                "team_a_id INTEGER, " +
                "team_b_id INTEGER, " +
                "score_a INTEGER, " +
                "score_b INTEGER, " +
                "winner_id INTEGER, " +
                "status TEXT NOT NULL DEFAULT 'PENDING', " +
                "FOREIGN KEY(tournament_id) " +
                "REFERENCES tournaments(id), " +
                "FOREIGN KEY(team_a_id) REFERENCES teams(id), " +
                "FOREIGN KEY(team_b_id) REFERENCES teams(id), " +
                "FOREIGN KEY(winner_id) REFERENCES teams(id)" +
                ")"
            );

            System.out.println(
                "BISU-TMS database initialized successfully!"
            );

        }
    }

    // Run this class once to test database creation
    public static void main(String[] args) {

        try {
            initializeDatabase();

            File db = new File(DB_FOLDER, DB_FILE);

            System.out.println(
                "Database location: " + db.getAbsolutePath()
            );

        } catch (SQLException ex) {

            System.err.println(
                "Database error: " + ex.getMessage()
            );

            ex.printStackTrace();
        }
    }
}
