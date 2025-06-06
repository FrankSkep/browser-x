package browser.dao;

import browser.database.Db_Connection;
import browser.model.Favorito;
import lombok.NoArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object (DAO) para la entidad Favorito.
 */
@NoArgsConstructor
public class FavoritoDAO extends AbstractDAO<Favorito> {

    @Override
    protected String getTableName() {
        return "favoritos";
    }

    @Override
    public void guardar(Favorito favorito) {
        String sql = "INSERT INTO favoritos (nombre, url) VALUES (?, ?)";
        try (Connection conn = Db_Connection.getInstance().getConnection();
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
        try (Connection conn = Db_Connection.getInstance().getConnection();
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