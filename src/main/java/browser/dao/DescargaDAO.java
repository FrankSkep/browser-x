package browser.dao;

import browser.database.Db_Connection;
import browser.model.Descarga;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import browser.data_structure.LinkedList;

/**
 * DAO para manejar las operaciones de la base de datos relacionadas con las descargas.
 */
public class DescargaDAO {

    private static DescargaDAO instance = null;

    private DescargaDAO() {}

    /**
     * Obtiene la instancia única de DescargaDAO.
     *
     * @return La instancia de DescargaDAO.
     */
    public static synchronized DescargaDAO getInstance() {
        if (instance == null) {
            instance = new DescargaDAO();
        }
        return instance;
    }

    /**
     * Guarda una descarga en la base de datos.
     *
     * @param descarga La descarga a guardar.
     */
    public void guardar(Descarga descarga) {
        String sql = "INSERT INTO descargas (nombre, url, fecha) VALUES (?, ?, ?)";

        try (Connection connection = Db_Connection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, descarga.getNombre());
            statement.setString(2, descarga.getUrl());
            statement.setString(3, descarga.getFecha());
            statement.executeUpdate();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Ocurrio un error:" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Elimina una descarga de la base de datos por su nombre.
     *
     * @param nombre El nombre de la descarga a eliminar.
     */
    public void eliminar(String nombre) {
        String sql = "DELETE FROM descargas WHERE nombre = ?";

        try (Connection conn = Db_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombre);
            stmt.executeUpdate();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Ocurrio un error:" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Elimina todas las descargas de la base de datos.
     */
    public void eliminarTodo() {
        String sql = "DELETE FROM descargas";

        try (Connection conn = Db_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Ocurrio un error:" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Obtiene todas las descargas de la base de datos.
     *
     * @return Una lista enlazada de todas las descargas.
     */
    public LinkedList<Descarga> obtenerTodo() {
        String sql = "SELECT * FROM descargas";
        LinkedList<Descarga> descargas = new LinkedList<>();

        try (Connection conn = Db_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                descargas.add(new Descarga(
                        resultSet.getString("nombre"),
                        resultSet.getString("url"),
                        resultSet.getString("fecha")
                ));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Ocurrio un error:" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return descargas;
    }
}