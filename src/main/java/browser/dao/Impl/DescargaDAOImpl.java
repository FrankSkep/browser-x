package browser.dao.Impl;

import browser.dao.DAO;
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
public class DescargaDAOImpl implements DAO<Descarga> {

    private static DescargaDAOImpl instance = null;

    private DescargaDAOImpl() {}

    /**
     * Obtiene la instancia única de DescargaDAO.
     *
     * @return La instancia de DescargaDAO.
     */
    public static synchronized DescargaDAOImpl getInstance() {
        if (instance == null) {
            instance = new DescargaDAOImpl();
        }
        return instance;
    }

    @Override
    public void save(Descarga descarga) {
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

    @Override
    public void delete(Descarga descarga) {
        String sql = "DELETE FROM descargas WHERE nombre = ?";

        try (Connection conn = Db_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, descarga.getNombre());
            stmt.executeUpdate();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Ocurrio un error:" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void deleteAll() {
        String sql = "DELETE FROM descargas";

        try (Connection conn = Db_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Ocurrio un error:" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public LinkedList<Descarga> getAll() {
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