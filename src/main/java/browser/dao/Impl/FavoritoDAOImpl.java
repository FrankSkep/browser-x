package browser.dao.Impl;

import browser.dao.AbstractDAO;
import browser.database.Db_Connection;
import browser.model.Favorito;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FavoritoDAOImpl extends AbstractDAO<Favorito> {

    private static FavoritoDAOImpl instance = null;

    private FavoritoDAOImpl() {
    }

    /**
     * Singleton para obtener la instancia de la clase.
     *
     * @return La instancia de la clase.
     */
    public static synchronized FavoritoDAOImpl getInstance() {
        if (instance == null) {
            instance = new FavoritoDAOImpl();
        }
        return instance;
    }

    @Override
    protected String getTableName() {
        return "favoritos";
    }

    @Override
    public void guardar(Favorito favorito) {
        String sql = "INSERT INTO favoritos (nombre, url) VALUES (?, ?)";
        try (Connection conn = Db_Connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, favorito.getNombre());
            pstmt.setString(2, favorito.getUrl());
            pstmt.executeUpdate();
        } catch (
                SQLException e) {
            handleException(e);
        }
    }

    @Override
    public void eliminar(Favorito favorito) {
        String sql = "DELETE FROM favoritos WHERE nombre = ?";
        try (Connection conn = Db_Connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, favorito.getNombre());
            pstmt.executeUpdate();
        } catch (
                SQLException e) {
            handleException(e);
        }
    }

    @Override
    protected Favorito mapResultSetToEntity(ResultSet rs) throws SQLException {
        return new Favorito(rs.getString("nombre"), rs.getString("url"));
    }
}