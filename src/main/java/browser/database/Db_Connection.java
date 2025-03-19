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
    private static Db_Connection instance;

    // Constructor privado para evitar la creación de instancias
    private Db_Connection() {
    }

    /**
     * Obtiene la instancia única de Db_Connection.
     *
     * @return la instancia única de Db_Connection.
     */
    public static synchronized Db_Connection getInstance() {
        if (instance == null) {
            instance = new Db_Connection();
        }
        return instance;
    }

    /**
     * Inicializa la base de datos creando las tablas necesarias.
     */
    public void initializeDatabase() {
        try (Connection conn = getConnection()) {
            Db_Initializer.initializeTables(conn);
        } catch (
                SQLException e) {
            System.out.println("Error inicializando las tablas: " + e.getMessage());
        }
    }

    /**
     * Obtiene una conexión a la base de datos SQLite.
     *
     * @return una conexión a la base de datos.
     * @throws SQLException si ocurre un error al establecer la conexión.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + DATABASE_PATH);
    }
}