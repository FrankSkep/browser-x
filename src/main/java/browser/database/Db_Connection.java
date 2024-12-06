package browser.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * La clase Db_Connection proporciona métodos para establecer una conexión con la base de datos SQLite.
 * Contiene métodos para inicializar la base de datos y obtener una conexión.
 */
public class Db_Connection {

    // Ruta de la base de datos SQLite
    private static final String DATABASE_PATH = "src/main/resources/browser.db";

    /**
     * Inicializa la base de datos creando las tablas necesarias.
     */
    public static void initializeDatabase() {
        try (Connection conn = getConnection()) {
            Db_Initializer.initializeTables(conn);
        } catch (SQLException e) {
            System.out.println("Error inicializando las tablas: " + e.getMessage());
        }
    }

    /**
     * Obtiene una conexión a la base de datos SQLite.
     * @return una conexión a la base de datos.
     * @throws SQLException si ocurre un error al establecer la conexión.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + DATABASE_PATH);
    }
}