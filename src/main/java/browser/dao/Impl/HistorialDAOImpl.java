package browser.dao.Impl;

import browser.dao.AbstractDAO;
import browser.database.Db_Connection;
import browser.model.EntradaHistorial;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HistorialDAOImpl extends AbstractDAO<EntradaHistorial> {

    private static HistorialDAOImpl instance = null;

    private HistorialDAOImpl() {
    }

    /**
     * Singleton para obtener la instancia de la clase.
     *
     * @return La instancia de la clase.
     */
    public static synchronized HistorialDAOImpl getInstance() {
        if (instance == null) {
            instance = new HistorialDAOImpl();
        }
        return instance;
    }

    @Override
    protected String getTableName() {
        return "historial";
    }

    @Override
    public void save(EntradaHistorial entradaHistorial) {
        String sql = "INSERT INTO historial (url, fecha) VALUES (?, ?)";
        try (Connection conn = Db_Connection.getConnection();
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
    public void delete(EntradaHistorial entradaHistorial) {
        String sql = "DELETE FROM historial WHERE url = ? AND fecha = ?";
        try (Connection conn = Db_Connection.getConnection();
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