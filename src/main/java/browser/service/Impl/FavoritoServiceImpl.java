package browser.service.Impl;

import browser.dao.FavoritoDAO;
import browser.data_structure.Hashtable;
import browser.data_structure.LinkedList;
import browser.model.Favorito;
import browser.service.IService;

/**
 * Servicio para gestionar los favoritos del navegador.
 */
public class FavoritoServiceImpl implements IService<Hashtable<String, String>, Favorito> {

    private final Hashtable<String, String> favoritos;
    private final FavoritoDAO favoritoDAO;

    /**
     * Constructor que inicializa la tabla hash de favoritos y carga los datos desde el DAO.
     */
    public FavoritoServiceImpl(FavoritoDAO favoritoDAO) {
        favoritos = new Hashtable<>();
        this.favoritoDAO = favoritoDAO;
        LinkedList<Favorito> listaFavoritos = favoritoDAO.obtenerTodo();
        for (Favorito favorito : listaFavoritos) {
            favoritos.put(favorito.getNombre(), favorito.getUrl());
        }
    }

    /**
     * Agrega un nuevo favorito.
     *
     * @param favorito El favorito a agregar.
     */
    @Override
    public void agregarElemento(Favorito favorito) {
        favoritos.put(favorito.getNombre(), favorito.getUrl());
        favoritoDAO.guardar(favorito);
    }


    /**
     * Elimina todos los favoritos.
     */
    @Override
    public void eliminarTodo() {
        favoritos.clear();
        favoritoDAO.eliminarTodo();
    }

    /**
     * Obtiene todos los favoritos.
     *
     * @return Una tabla hash con todos los favoritos.
     */

    @Override
    public Hashtable<String, String> obtenerTodo() {
        return favoritos;
    }

    /**
     * Elimina un favorito.
     *
     * @param favorito El nombre del favorito a eliminar.
     */
    @Override
    public void eliminarElemento(Favorito favorito) {
        favoritos.remove(favorito.getNombre());
        favoritoDAO.eliminar(favorito);
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