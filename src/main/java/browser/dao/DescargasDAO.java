package browser.dao;

import browser.database.Db_Connection;
import browser.model.Descarga;

import java.sql.Connection;
import java.sql.Timestamp;

public class DescargasDAO {

    private static DescargasDAO instance = null;

    private DescargasDAO() {
    }

    public static synchronized DescargasDAO getInstance() {
        if (instance == null) {
            instance = new DescargasDAO();
        }
        return instance;
    }

    public void guardar(Descarga descarga) {
        try (Connection connection = Db_Connection.getConnection();
             var statement = connection.prepareStatement("INSERT INTO descargas (nombre, url, fecha) VALUES (?, ?, ?)")) {
            statement.setString(1, descarga.getNombre());
            statement.setString(2, descarga.getUrl());
            statement.setTimestamp(3, Timestamp.valueOf(descarga.getFecha()));
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void eliminar(String nombre) {
        // implementar
    }

    public void eliminar() {
        // implementar
    }

    public void obtener() {
        // implementar
    }
}
