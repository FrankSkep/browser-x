package browser.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Db_Initializer {

    public static void initializeTables(Connection conn) throws SQLException {

        String tablaHistorial = "CREATE TABLE IF NOT EXISTS historial ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "url TEXT NOT NULL, "
                + "fecha TEXT NOT NULL, "
                + "titulo TEXT NOT NULL)";

        String tablaFavoritos = "CREATE TABLE IF NOT EXISTS favoritos ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "nombre TEXT NOT NULL UNIQUE, "
                + "url TEXT NOT NULL)";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(tablaHistorial);
            stmt.execute(tablaFavoritos);
        }
    }
}
