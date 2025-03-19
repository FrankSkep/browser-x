package browser.dao;

import browser.data_structure.LinkedList;
import browser.database.Db_Connection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Clase abstracta que define las operaciones básicas de un DAO.
 *
 * @param <T> Tipo de entidad que maneja el DAO.
 */
public abstract class AbstractDAO<T> {

    /**
     * Guarda una entidad en la base de datos.
     *
     * @param entity La entidad a guardar.
     */
    public abstract void guardar(T entity);

    /**
     * Elimina una entidad de la base de datos.
     *
     * @param entity La entidad a eliminar.
     */
    public abstract void eliminar(T entity);

    /**
     * Elimina todos los registros de la tabla.
     */
    public void eliminarTodo() {
        String sql = "DELETE FROM " + getTableName();
        try (Connection conn = Db_Connection.getInstance().getConnection();
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
    public LinkedList<T> obtenerTodo() {
        String sql = "SELECT * FROM " + getTableName();
        LinkedList<T> entities = new LinkedList<>();
        try (Connection conn = Db_Connection.getInstance().getConnection();
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
     * Obtiene el nombre de la tabla en la base de datos.
     *
     * @return El nombre de la tabla en la base de datos.
     */
    protected abstract String getTableName();

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