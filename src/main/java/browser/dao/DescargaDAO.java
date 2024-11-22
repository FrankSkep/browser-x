package browser.dao;

import browser.database.Db_Connection;
import browser.model.Descarga;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import browser.data_structure.LinkedList;

public class DescargaDAO {

    private static DescargaDAO instance = null;

    private DescargaDAO() {
    }

    public static synchronized DescargaDAO getInstance() {
        if (instance == null) {
            instance = new DescargaDAO();
        }
        return instance;
    }

    public void guardar(Descarga descarga) {
        try (Connection connection = Db_Connection.getConnection();
             var statement = connection.prepareStatement("INSERT INTO descargas (nombre, url, fecha) VALUES (?, ?, ?)")) {
            statement.setString(1, descarga.getNombre());
            statement.setString(2, descarga.getUrl());
            statement.setString(3, descarga.getFecha());
            statement.executeUpdate();
        } catch (
                Exception e) {
            JOptionPane.showMessageDialog(null, "Ocurrio un error:" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void eliminar(String nombre) {
        String sql = "DELETE FROM descargas WHERE nombre = ?";
        try (Connection conn = Db_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);) {
            stmt.setString(1, nombre);
            stmt.executeUpdate();
        } catch (
                Exception e) {
            JOptionPane.showMessageDialog(null, "Ocurrio un error:" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void eliminarTodo() {
        String sql = "DELETE FROM descargas";
        try (Connection conn = Db_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);) {
            stmt.executeUpdate();
        } catch (
                Exception e) {
            JOptionPane.showMessageDialog(null, "Ocurrio un error:" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public LinkedList<Descarga> obtenerTodo() {
        String sql = "SELECT * FROM descargas";

        try (Connection conn = Db_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);) {
            ResultSet resultSet = stmt.executeQuery();

            LinkedList<Descarga> descargas = new LinkedList<>();
            while (resultSet.next()) {
                descargas.add(new Descarga(
                        resultSet.getString("nombre"),
                        resultSet.getString("url"),
                        resultSet.getString("fecha")
                ));
            }
            return descargas;
        } catch (
                Exception e) {
            JOptionPane.showMessageDialog(null, "Ocurrio un error:" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return new LinkedList<>();
    }
}
