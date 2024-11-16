package browser.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Db_Connection {

    private static final String DATABASE_PATH = "src/main/resources/browser.db";

    public static void initializeDatabase() {
        try (Connection conn = getConnection()) {
            Db_Initializer.initializeTables(conn);
        } catch (
                SQLException e) {
            System.out.println("Error inicializando las tablas: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + DATABASE_PATH);
    }
}