package browser.dao;

import browser.data_structure.Hashtable;
import browser.database.Db_Connection;
import browser.model.Favorito;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FavoritoDAO {

    private static FavoritoDAO instance = null;

    private FavoritoDAO() {}

    public static synchronized FavoritoDAO getInstance() {
        if (instance == null) {
            instance = new FavoritoDAO();
        }
        return instance;
    }

    public void guardar(Favorito favorito) {
        String sql = "INSERT INTO favoritos (nombre, url) VALUES (?, ?)";

        try (Connection conn = Db_Connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);) {

            pstmt.setString(1, favorito.getNombre());
            pstmt.setString(2, favorito.getUrl());
            pstmt.executeUpdate();
        } catch (
                SQLException e) {
            JOptionPane.showMessageDialog(null, "Ocurrio un error:" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void eliminar(String nombre) {
        String sql = "DELETE FROM favoritos WHERE nombre = ?";

        try (Connection conn = Db_Connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);) {

            pstmt.setString(1, nombre);
            pstmt.executeUpdate();
        } catch (
                SQLException e) {
            JOptionPane.showMessageDialog(null, "Ocurrio un error:" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void eliminarTodo() {
        String sql = "DELETE FROM favoritos";

        try (Connection conn = Db_Connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.executeUpdate();
        } catch (
                SQLException e) {
            JOptionPane.showMessageDialog(null, "Ocurrio un error:" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Hashtable<String, String> obtenerTodo() {
        String sql = "SELECT nombre, url FROM favoritos";
        Hashtable<String, String> favoritos = new Hashtable<>();

        try (Connection conn = Db_Connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                favoritos.put(rs.getString("nombre"), rs.getString("url"));
            }
        } catch (
                SQLException e) {
            JOptionPane.showMessageDialog(null, "Ocurrio un error:" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return favoritos;
    }

}
