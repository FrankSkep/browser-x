package browser.dao;

import browser.data_structure.LinkedList;
import browser.database.Db_Connection;
import browser.model.EntradaHistorial;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class HistorialDAO {

    private static HistorialDAO instance = null;

    private HistorialDAO() {
    }

    public static synchronized HistorialDAO getInstance() {
        if (instance == null) {
            instance = new HistorialDAO();
        }
        return instance;
    }

    public void guardar(EntradaHistorial entradaHistorial) {
        String sql = "INSERT INTO historial(url, fecha) VALUES(?, ?)";
        try (Connection conn = Db_Connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);) {
            pstmt.setString(1, entradaHistorial.getUrl());
            pstmt.setString(2, entradaHistorial.getFecha());
            pstmt.executeUpdate();
        } catch (
                Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void eliminar(EntradaHistorial entradaHistorial) {
        String sql = "DELETE FROM historial WHERE url = ? AND fecha = ?";
        try (Connection conn = Db_Connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);) {
            pstmt.setString(1, entradaHistorial.getUrl());
            pstmt.setString(2, entradaHistorial.getFecha());
            pstmt.executeUpdate();
        } catch (
                Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void eliminarTodo() {
        String sql = "DELETE FROM historial";
        try (Connection conn = Db_Connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);) {
            pstmt.executeUpdate();
        } catch (
                Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static LinkedList<EntradaHistorial> obtenerTodo() {
        LinkedList<EntradaHistorial> historial = new LinkedList<>();

        String sql = "SELECT url, fecha FROM historial";

        try (Connection conn = Db_Connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                historial.add(new EntradaHistorial(rs.getString("url"), rs.getString("fecha")));
            }
        } catch (
                Exception e) {
            System.out.println(e.getMessage());
        }

        return historial;
    }

}
