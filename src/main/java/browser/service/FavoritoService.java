package browser.service;

import browser.dao.FavoritosDAO;

import java.util.HashMap;

public class FavoritoService {

    private final HashMap<String, String> favoritos;

    public FavoritoService() {
        favoritos = new HashMap<>();
        favoritos.putAll(FavoritosDAO.getInstance().obtenerFavoritos());
    }

    public void insertarFavorito(String nombre, String url) {
        favoritos.put(nombre, url);
        FavoritosDAO.getInstance().insertarFavorito(nombre, url);
    }

    public void eliminarFavorito(String nombre) {
        favoritos.remove(nombre);
        FavoritosDAO.getInstance().eliminarFavorito(nombre);
    }

    public void eliminarFavoritos() {
        favoritos.clear();
        FavoritosDAO.getInstance().eliminarFavoritos();
    }

    public boolean existeFavorito(String url) {
        return favoritos.containsValue(url);
    }

    public HashMap<String, String> obtenerFavoritos() {
        return favoritos;
    }
}
