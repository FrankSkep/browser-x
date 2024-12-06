package browser.dao;

import browser.data_structure.Hashtable;
import browser.database.Db_Connection;
import browser.model.Favorito;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO para manejar las operaciones de la base de datos relacionadas con los favoritos.
 */
public class FavoritoDAO {

    private static FavoritoDAO instance = null;

    private FavoritoDAO() {}

    /**
     * Obtiene la instancia única de FavoritoDAO.
     *
     * @return La instancia de FavoritoDAO.
     */
    public static synchronized FavoritoDAO getInstance() {
        if (instance == null) {
            instance = new FavoritoDAO();
        }
        return instance;
    }

    /**
     * Guarda un favorito en la base de datos.
     *
     * @param favorito El favorito a guardar.
     */
    public void guardar(Favorito favorito) {
        String sql = "INSERT INTO favoritos (nombre, url) VALUES (?, ?)";

        try (Connection conn = Db_Connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, favorito.getNombre());
            pstmt.setString(2, favorito.getUrl());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Ocurrio un error:" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Elimina un favorito de la base de datos por su nombre.
     *
     * @param nombre El nombre del favorito a eliminar.
     */
    public void eliminar(String nombre) {
        String sql = "DELETE FROM favoritos WHERE nombre = ?";

        try (Connection conn = Db_Connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombre);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Ocurrio un error:" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Elimina todos los favoritos de la base de datos.
     */
    public void eliminarTodo() {
        String sql = "DELETE FROM favoritos";

        try (Connection conn = Db_Connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Ocurrio un error:" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Obtiene todos los favoritos de la base de datos.
     *
     * @return Un hashtable de todos los favoritos.
     */
    public Hashtable<String, String> obtenerTodo() {
        String sql = "SELECT nombre, url FROM favoritos";
        Hashtable<String, String> favoritos = new Hashtable<>();

        try (Connection conn = Db_Connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                favoritos.put(rs.getString("nombre"), rs.getString("url"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Ocurrio un error:" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return favoritos;
    }
}