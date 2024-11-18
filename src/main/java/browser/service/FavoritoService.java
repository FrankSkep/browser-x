package browser.service;

import browser.dao.FavoritosDAO;
import browser.model.Favorito;

import java.util.HashMap;

public class FavoritoService {

    private final HashMap<String, String> favoritos;

    public FavoritoService() {
        favoritos = new HashMap<>();
        favoritos.putAll(FavoritosDAO.getInstance().obtenerTodos());
    }

    public void insertarFavorito(Favorito favorito) {
        favoritos.put(favorito.getNombre(), favorito.getUrl());
        FavoritosDAO.getInstance().guardar(favorito);
    }

    public void eliminarFavorito(String nombre) {
        favoritos.remove(nombre);
        FavoritosDAO.getInstance().eliminar(nombre);
    }

    public void eliminarFavoritos() {
        favoritos.clear();
        FavoritosDAO.getInstance().eliminarTodos();
    }

    public boolean existeFavorito(String url) {
        return favoritos.containsValue(url);
    }

    public HashMap<String, String> obtenerFavoritos() {
        return favoritos;
    }
}
