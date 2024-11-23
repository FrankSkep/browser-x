package browser.service;

import browser.dao.FavoritoDAO;
import browser.data_structure.Hashtable;
import browser.model.Favorito;

public class FavoritoService {

    private final Hashtable<String, String> favoritos;

    public FavoritoService() {
        favoritos = new Hashtable<>();
        favoritos.putAll(FavoritoDAO.getInstance().obtenerTodos());
    }

    public void insertarFavorito(Favorito favorito) {
        favoritos.put(favorito.getNombre(), favorito.getUrl());
        FavoritoDAO.getInstance().guardar(favorito);
    }

    public void eliminarFavorito(String nombre) {
        favoritos.remove(nombre);
        FavoritoDAO.getInstance().eliminar(nombre);
    }

    public void eliminarFavoritos() {
        favoritos.clear();
        FavoritoDAO.getInstance().eliminarTodos();
    }

    public Hashtable<String, String> obtenerFavoritos() {
        return favoritos;
    }

    public boolean existeFavorito(String url) {
        return favoritos.containsValue(url);
    }
}
