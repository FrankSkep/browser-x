package browser.dao;

import browser.database.Db_Connection;
import browser.model.Descarga;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object (DAO) para la entidad Descarga.
 */
public class DescargaDAO extends AbstractDAO<Descarga> {

    private static DescargaDAO instance = null;

    private DescargaDAO() {
    }

    /**
     * Singleton para obtener la instancia de la clase.
     *
     * @return La instancia de la clase.
     */
    public static synchronized DescargaDAO getInstance() {
        if (instance == null) {
            instance = new DescargaDAO();
        }
        return instance;
    }

    @Override
    protected String getTableName() {
        return "descargas";
    }

    @Override
    public void guardar(Descarga descarga) {
        String sql = "INSERT INTO descargas (nombre, url, fecha) VALUES (?, ?, ?)";
        try (Connection conn = Db_Connection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, descarga.getNombre());
            pstmt.setString(2, descarga.getUrl());
            pstmt.setString(3, descarga.getFecha());
            pstmt.executeUpdate();
        } catch (
                SQLException e) {
            handleException(e);
        }
    }

    @Override
    public void eliminar(Descarga descarga) {
        String sql = "DELETE FROM descargas WHERE nombre = ?";
        try (Connection conn = Db_Connection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, descarga.getNombre());
            pstmt.executeUpdate();
        } catch (
                SQLException e) {
            handleException(e);
        }
    }

    @Override
    protected Descarga mapResultSetToEntity(ResultSet rs) throws SQLException {
        return new Descarga(rs.getString("nombre"), rs.getString("url"), rs.getString("fecha"));
    }
}