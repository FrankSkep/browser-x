package browser.dao;

import browser.database.Db_Connection;

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

    public void insertarFavorito(String nombre, String url) {

        try (Connection conn = Db_Connection.getConnection()) {
            String sql = "INSERT INTO favoritos (nombre, url) VALUES (?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, nombre);
                pstmt.setString(2, url);
                pstmt.executeUpdate();
            }
        } catch (
                SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void eliminarFavorito(String nombre) {

        try (Connection conn = Db_Connection.getConnection()) {
            String sql = "DELETE FROM favoritos WHERE nombre = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, nombre);
                pstmt.executeUpdate();
            }
        } catch (
                SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void eliminarFavoritos() {
        try (Connection conn = Db_Connection.getConnection()) {
            String sql = "DELETE FROM favoritos";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.executeUpdate();
            }
        } catch (
                SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public HashMap<String, String> obtenerFavoritos() {
        String sql = "SELECT nombre, url FROM favoritos";

        try (Connection conn = Db_Connection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();

            HashMap<String, String> favoritos = new HashMap<>();
            while (rs.next()) {
                favoritos.put(rs.getString("nombre"), rs.getString("url"));
            }
            return favoritos;
        } catch (
                SQLException e) {
            System.out.println(e.getMessage());
            return new HashMap<>();
        }
    }

}
