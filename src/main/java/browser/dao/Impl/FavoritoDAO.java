package browser.dao.Impl;

import browser.dao.IDAO;
import browser.data_structure.LinkedList;
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
public class FavoritoDAO implements IDAO<Favorito> {

    private static FavoritoDAO instance = null;

    private FavoritoDAO() {
    }

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
    @Override
    public void save(Favorito favorito) {
        String sql = "INSERT INTO favoritos (nombre, url) VALUES (?, ?)";

        try (Connection conn = Db_Connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, favorito.getNombre());
            pstmt.setString(2, favorito.getUrl());
            pstmt.executeUpdate();
        } catch (
                SQLException e) {
            JOptionPane.showMessageDialog(null, "Ocurrio un error:" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Elimina un favorito de la base de datos por su nombre.
     *
     * @param favorito Objeto del favorito a eliminar.
     */
    @Override
    public void delete(Favorito favorito) {
        String sql = "DELETE FROM favoritos WHERE nombre = ?";

        try (Connection conn = Db_Connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, favorito.getNombre());
            pstmt.executeUpdate();
        } catch (
                SQLException e) {
            JOptionPane.showMessageDialog(null, "Ocurrio un error:" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Elimina todos los favoritos de la base de datos.
     */
    @Override
    public void deleteAll() {
        String sql = "DELETE FROM favoritos";

        try (Connection conn = Db_Connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.executeUpdate();
        } catch (
                SQLException e) {
            JOptionPane.showMessageDialog(null, "Ocurrio un error:" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Obtiene todos los favoritos de la base de datos.
     *
     * @return una lista enlazada con todos los favoritos.
     */
    @Override
    public LinkedList<Favorito> getAll() {
        String sql = "SELECT nombre, url FROM favoritos";
        LinkedList<Favorito> favoritos = new LinkedList<>();

        try (Connection conn = Db_Connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                favoritos.add(new Favorito(rs.getString("nombre"), rs.getString("url")));
            }
        } catch (
                SQLException e) {
            JOptionPane.showMessageDialog(null, "Ocurrio un error:" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return favoritos;
    }
}