package browser.service;

import browser.dao.FavoritoDAO;
import browser.data_structure.Hashtable;
import browser.model.Favorito;

/**
 * Servicio para gestionar los favoritos del navegador.
 */
public class FavoritoService {

    private final Hashtable<String, String> favoritos;

    /**
     * Constructor que inicializa la tabla hash de favoritos y carga los datos desde el DAO.
     */
    public FavoritoService() {
        favoritos = new Hashtable<>();
        favoritos.putAll(FavoritoDAO.getInstance().obtenerTodo());
    }

    /**
     * Agrega un nuevo favorito.
     *
     * @param favorito El favorito a agregar.
     */
    public void agregarFavorito(Favorito favorito) {
        favoritos.put(favorito.getNombre(), favorito.getUrl());
        FavoritoDAO.getInstance().guardar(favorito);
    }

    /**
     * Elimina un favorito por su nombre.
     *
     * @param nombre El nombre del favorito a eliminar.
     */
    public void eliminarFavorito(String nombre) {
        favoritos.remove(nombre);
        FavoritoDAO.getInstance().eliminar(nombre);
    }

    /**
     * Elimina todos los favoritos.
     */
    public void eliminarTodo() {
        favoritos.clear();
        FavoritoDAO.getInstance().eliminarTodo();
    }

    /**
     * Obtiene todos los favoritos.
     *
     * @return Una tabla hash con todos los favoritos.
     */
    public Hashtable<String, String> obtenerTodo() {
        return favoritos;
    }

    /**
     * Verifica si un favorito con la URL especificada ya existe.
     *
     * @param url La URL del favorito a verificar.
     * @return true si el favorito existe, false en caso contrario.
     */
    public boolean existeFavorito(String url) {
        return favoritos.containsValue(url);
    }
}