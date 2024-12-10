package browser.dao.Impl;

import browser.dao.DAO;
import browser.data_structure.LinkedList;
import browser.database.Db_Connection;
import browser.model.EntradaHistorial;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * La clase HistorialDAO es responsable de realizar operaciones de acceso a datos
 * para el historial de navegación en la base de datos.
 * Implementa el patrón Singleton para asegurar que solo exista una instancia de la clase.
 */
public class HistorialDAOImpl implements DAO<EntradaHistorial> {

    // Instancia única de la clase
    private static HistorialDAOImpl instance = null;

    // Constructor privado para evitar la instanciación directa
    private HistorialDAOImpl() {}

    /**
     * Obtiene la instancia única de HistorialDAO.
     * @return la instancia única de HistorialDAO.
     */
    public static synchronized HistorialDAOImpl getInstance() {
        if (instance == null) {
            instance = new HistorialDAOImpl();
        }
        return instance;
    }

    @Override
    public void save(EntradaHistorial entradaHistorial) {
        String sql = "INSERT INTO historial(url, fecha) VALUES(?, ?)";

        try (Connection conn = Db_Connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, entradaHistorial.getUrl());
            pstmt.setString(2, entradaHistorial.getFecha());
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(EntradaHistorial entradaHistorial) {
        String sql = "DELETE FROM historial WHERE url = ? AND fecha = ?";

        try (Connection conn = Db_Connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, entradaHistorial.getUrl());
            pstmt.setString(2, entradaHistorial.getFecha());
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void deleteAll() {
        String sql = "DELETE FROM historial";
        try (Connection conn = Db_Connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public LinkedList<EntradaHistorial> getAll() {
        String sql = "SELECT url, fecha FROM historial";
        LinkedList<EntradaHistorial> historial = new LinkedList<>();

        try (Connection conn = Db_Connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                historial.add(new EntradaHistorial(rs.getString("url"), rs.getString("fecha")));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return historial;
    }
}