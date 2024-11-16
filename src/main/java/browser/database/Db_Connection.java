package browser.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Db_Connection {

    private static String DATABASE_PATH = "src/main/resources/browser.db";

    public static void initializeDatabase() {
        // Crear las tablas después de inicializar la conexión si es necesario
        try (Connection conn = getConnection()) {
            Db_Initializer.initializeTables(conn);
        } catch (
                SQLException e) {
            System.out.println("Error inicializando las tablas: " + e.getMessage());
        }
    }

    // Obtener conexion
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + DATABASE_PATH);
    }
}