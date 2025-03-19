package browser.dao;

import browser.database.Db_Connection;
import browser.model.EntradaHistorial;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object (DAO) para la entidad EntradaHistorial.
 */
public class HistorialDAO extends AbstractDAO<EntradaHistorial> {

    private static HistorialDAO instance = null;

    private HistorialDAO() {
    }

    /**
     * Singleton para obtener la instancia de la clase.
     *
     * @return La instancia de la clase.
     */
    public static synchronized HistorialDAO getInstance() {
        if (instance == null) {
            instance = new HistorialDAO();
        }
        return instance;
    }

    @Override
    protected String getTableName() {
        return "historial";
    }

    @Override
    public void guardar(EntradaHistorial entradaHistorial) {
        String sql = "INSERT INTO historial (url, fecha) VALUES (?, ?)";
        try (Connection conn = Db_Connection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, entradaHistorial.getUrl());
            pstmt.setString(2, entradaHistorial.getFecha());
            pstmt.executeUpdate();
        } catch (
                SQLException e) {
            handleException(e);
        }
    }

    @Override
    public void eliminar(EntradaHistorial entradaHistorial) {
        String sql = "DELETE FROM historial WHERE url = ? AND fecha = ?";
        try (Connection conn = Db_Connection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, entradaHistorial.getUrl());
            pstmt.setString(2, entradaHistorial.getFecha());
            pstmt.executeUpdate();
        } catch (
                SQLException e) {
            handleException(e);
        }
    }

    @Override
    protected EntradaHistorial mapResultSetToEntity(ResultSet rs) throws SQLException {
        return new EntradaHistorial(rs.getString("url"), rs.getString("fecha"));
    }
}