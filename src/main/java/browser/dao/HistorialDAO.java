package browser.dao;

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
public class HistorialDAO {

    // Instancia única de la clase
    private static HistorialDAO instance = null;

    // Constructor privado para evitar la instanciación directa
    private HistorialDAO() {}

    /**
     * Obtiene la instancia única de HistorialDAO.
     * @return la instancia única de HistorialDAO.
     */
    public static synchronized HistorialDAO getInstance() {
        if (instance == null) {
            instance = new HistorialDAO();
        }
        return instance;
    }

    /**
     * Guarda una entrada de historial en la base de datos.
     * @param entradaHistorial la entrada de historial a guardar.
     */
    public void guardar(EntradaHistorial entradaHistorial) {
        String sql = "INSERT INTO historial(url, fecha) VALUES(?, ?)";

        try (Connection conn = Db_Connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);) {

            pstmt.setString(1, entradaHistorial.getUrl());
            pstmt.setString(2, entradaHistorial.getFecha());
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Elimina una entrada de historial de la base de datos.
     * @param entradaHistorial la entrada de historial a eliminar.
     */
    public void eliminar(EntradaHistorial entradaHistorial) {
        String sql = "DELETE FROM historial WHERE url = ? AND fecha = ?";

        try (Connection conn = Db_Connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);) {

            pstmt.setString(1, entradaHistorial.getUrl());
            pstmt.setString(2, entradaHistorial.getFecha());
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Elimina todas las entradas de historial de la base de datos.
     */
    public void eliminarTodo() {
        String sql = "DELETE FROM historial";
        try (Connection conn = Db_Connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);) {
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Obtiene todas las entradas de historial de la base de datos.
     * @return una lista enlazada de todas las entradas de historial.
     */
    public LinkedList<EntradaHistorial> obtenerTodo() {
        String sql = "SELECT url, fecha FROM historial";
        LinkedList<EntradaHistorial> historial = new LinkedList<>();

        try (Connection conn = Db_Connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);) {
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