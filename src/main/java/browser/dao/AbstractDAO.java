package browser.dao;

import browser.data_structure.LinkedList;
import browser.database.Db_Connection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Clase abstracta que implementa la interfaz IDAO e implementa métodos comunes para los DAO.
 *
 * @param <T> Tipo de entidad que maneja el DAO.
 */
public abstract class AbstractDAO<T> implements IDAO<T> {

    /**
     * Obtiene el nombre de la tabla en la base de datos.
     *
     * @return El nombre de la tabla en la base de datos.
     */
    protected abstract String getTableName();

    /**
     * Elimina todos los registros de la tabla.
     */
    @Override
    public void deleteAll() {
        String sql = "DELETE FROM " + getTableName();
        try (Connection conn = Db_Connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (
                SQLException e) {
            handleException(e);
        }
    }

    /**
     * Obtiene todos los registros de la tabla.
     *
     * @return Una lista con todos los registros de la tabla.
     */
    @Override
    public LinkedList<T> getAll() {
        String sql = "SELECT * FROM " + getTableName();
        LinkedList<T> entities = new LinkedList<>();
        try (Connection conn = Db_Connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                entities.add(mapResultSetToEntity(rs));
            }
        } catch (
                SQLException e) {
            handleException(e);
        }
        return entities;
    }

    /**
     * Mapea un registro de la base de datos a una entidad.
     *
     * @param rs El registro de la base de datos.
     * @return La entidad mapeada.
     * @throws SQLException Si ocurre un error al mapear el registro.
     */
    protected abstract T mapResultSetToEntity(ResultSet rs) throws SQLException;

    /**
     * Maneja una excepción de SQL.
     *
     * @param e La excepción de SQL.
     */
    protected void handleException(SQLException e) {
        System.err.println("Error en la operación de base de datos: " + e.getMessage());
    }
}
