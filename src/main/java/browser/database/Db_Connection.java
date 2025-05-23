package browser.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * La clase {@code Db_Connection} implementa el patrón Singleton para gestionar
 * la conexión con una base de datos SQLite. Utiliza el patrón
 * Initialization-on-demand holder idiom para garantizar una instancia única,
 * segura en entornos multi-hilo y con inicialización perezosa.
 */
public class Db_Connection {

    /**
     * Ruta del archivo de base de datos SQLite.
     */
    private static final String DATABASE_PATH = "src/main/resources/browser.db";

    /**
     * Constructor privado para evitar instanciación externa.
     */
    private Db_Connection() {
    }

    /**
     * Clase estática interna responsable de contener la instancia única de {@code Db_Connection}.
     * Se carga en memoria solo cuando se llama a {@link #getInstance()}, garantizando
     * inicialización perezosa y seguridad en entornos multi-hilo.
     */
    private static class Holder {
        private static final Db_Connection INSTANCE = new Db_Connection();
    }

    /**
     * Devuelve la instancia única de {@code Db_Connection}.
     *
     * @return instancia única de {@code Db_Connection}
     */
    public static Db_Connection getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Inicializa la base de datos creando las tablas necesarias.
     * Este método utiliza {@link #getConnection()} para obtener una conexión
     * y delega la creación de tablas a {@code Db_Initializer}.
     */
    public void initializeDatabase() {
        try (Connection conn = getConnection()) {
            Db_Initializer.initializeTables(conn);
        } catch (SQLException e) {
            System.out.println("Error inicializando las tablas: " + e.getMessage());
        }
    }

    /**
     * Obtiene una nueva conexión a la base de datos SQLite.
     *
     * @return conexión a la base de datos
     * @throws SQLException si ocurre un error al establecer la conexión
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + DATABASE_PATH);
    }
}
