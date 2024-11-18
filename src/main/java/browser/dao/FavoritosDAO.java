package browser.dao;

import browser.database.Db_Connection;
import browser.model.Favorito;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class FavoritosDAO {

    private static FavoritosDAO instance = null;

    private FavoritosDAO() {
    }

    public static synchronized FavoritosDAO getInstance() {
        if (instance == null) {
            instance = new FavoritosDAO();
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

    public void eliminarTodos() {
        String sql = "DELETE FROM favoritos";
        try (Connection conn = Db_Connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.executeUpdate();
        } catch (
                SQLException e) {
            JOptionPane.showMessageDialog(null, "Ocurrio un error:" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public HashMap<String, String> obtenerTodos() {
        String sql = "SELECT nombre, url FROM favoritos";

        try (Connection conn = Db_Connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();

            HashMap<String, String> favoritos = new HashMap<>();
            while (rs.next()) {
                favoritos.put(rs.getString("nombre"), rs.getString("url"));
            }
            return favoritos;
        } catch (
                SQLException e) {
            JOptionPane.showMessageDialog(null, "Ocurrio un error:" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return new HashMap<>();
        }
    }

}
