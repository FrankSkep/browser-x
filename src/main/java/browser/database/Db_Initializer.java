package browser.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * La clase Db_Initializer proporciona métodos para inicializar las tablas en la base de datos.
 * Contiene métodos para crear las tablas de historial, favoritos y descargas.
 */
public class Db_Initializer {

    /**
     * Inicializa las tablas en la base de datos.
     *
     * @param conn la conexión a la base de datos.
     * @throws SQLException si ocurre un error al crear las tablas.
     */
    public static void initializeTables(Connection conn) throws SQLException {

        String tablaHistorial = "CREATE TABLE IF NOT EXISTS historial ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "url TEXT NOT NULL, "
                + "fecha TEXT NOT NULL)";

        String tablaFavoritos = "CREATE TABLE IF NOT EXISTS favoritos ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "nombre TEXT NOT NULL UNIQUE, "
                + "url TEXT NOT NULL)";

        String tablaDescargas = "CREATE TABLE IF NOT EXISTS descargas ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "nombre TEXT NOT NULL, "
                + "url TEXT NOT NULL, "
                + "fecha TEXT NOT NULL)";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(tablaHistorial);
            stmt.execute(tablaFavoritos);
            stmt.execute(tablaDescargas);
        }
    }
}