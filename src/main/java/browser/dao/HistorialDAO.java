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

    public HistorialDAO() {
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