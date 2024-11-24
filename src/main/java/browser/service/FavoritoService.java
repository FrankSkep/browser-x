package browser.service;

import browser.dao.FavoritoDAO;
import browser.data_structure.Hashtable;
import browser.model.Favorito;

public class FavoritoService {

    private final Hashtable<String, String> favoritos;

    public FavoritoService() {
        favoritos = new Hashtable<>();
        favoritos.putAll(FavoritoDAO.getInstance().obtenerTodo());
    }

    public void guardar(Favorito favorito) {
        favoritos.put(favorito.getNombre(), favorito.getUrl());
        FavoritoDAO.getInstance().guardar(favorito);
    }

    public void eliminar(String nombre) {
        favoritos.remove(nombre);
        FavoritoDAO.getInstance().eliminar(nombre);
    }

    public void eliminarTodo() {
        favoritos.clear();
        FavoritoDAO.getInstance().eliminarTodo();
    }

    public Hashtable<String, String> obtener() {
        return favoritos;
    }

    public boolean existe(String url) {
        return favoritos.containsValue(url);
    }
}
