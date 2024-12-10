package browser.service.Impl;

import browser.dao.Impl.FavoritoDAO;
import browser.data_structure.Hashtable;
import browser.data_structure.LinkedList;
import browser.model.Favorito;
import browser.service.IService;

/**
 * Servicio para gestionar los favoritos del navegador.
 */
public class FavoritoService implements IService<Hashtable<String, String>, Object,Favorito> {

    private final Hashtable<String, String> favoritos;

    /**
     * Constructor que inicializa la tabla hash de favoritos y carga los datos desde el DAO.
     */
    public FavoritoService() {
        favoritos = new Hashtable<>();
        LinkedList<Favorito> listaFavoritos = FavoritoDAO.getInstance().getAll();
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
        FavoritoDAO.getInstance().save(favorito);
    }


    /**
     * Elimina todos los favoritos.
     */
    @Override
    public void eliminarTodo() {
        favoritos.clear();
        FavoritoDAO.getInstance().deleteAll();
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
     * Funcion no implementada.
     *
     * @return El elemento seleccionado de favoritos. (Siempre null lolxd)
     */
    @Override
    public Object obtenerElemento() {
        return null;
    }

    /**
     * Elimina un favorito por su nombre.
     *
     * @param favorito El nombre del favorito a eliminar.
     */
    @Override
    public void eliminarElemento(Favorito favorito) {
        favoritos.remove(favorito.getNombre());
        FavoritoDAO.getInstance().delete(favorito);
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