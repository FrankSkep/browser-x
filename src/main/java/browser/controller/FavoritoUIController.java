package browser.controller;

import browser.data_structure.Hashtable;
import browser.model.Favorito;
import browser.service.Impl.FavoritoServiceImpl;
import browser.util.Constants;
import browser.util.UiTool;
import lombok.RequiredArgsConstructor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Controlador encargado de gestionar los favoritos en la aplicación.
 * Permite mostrar, agregar, eliminar y consultar favoritos.
 */
@RequiredArgsConstructor
public class FavoritoUIController {
    /** Servicio para la gestión de favoritos. */
    private final FavoritoServiceImpl favoritoService;

    /**
     * Muestra una ventana con la lista de favoritos.
     * Permite eliminar, abrir o cerrar la ventana de favoritos.
     *
     * @param parent ventana principal sobre la que se muestra el diálogo
     * @param actualizarEstadoBotones acción para actualizar el estado de los botones (puede ser null)
     * @param cargarURL consumidor para cargar una URL seleccionada (puede ser null)
     */
    public void mostrarFavoritos(JFrame parent, Runnable actualizarEstadoBotones, java.util.function.Consumer<String> cargarURL) {
        var favoritosMap = favoritoService.obtenerTodo();

        if (favoritosMap.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "No hay favoritos.");
        } else {
            JTable favoritosTable = UiTool.crearTabla(Constants.TITULO_FAVORITOS, new String[]{"NOMBRE", "SITIO"},
                    favoritosMap.keySet().stream().map(key -> new Object[]{key, favoritosMap.get(key)}).toList());

            DefaultTableModel tableModel = (DefaultTableModel) favoritosTable.getModel();

            JScrollPane scrollPane = new JScrollPane(favoritosTable);
            scrollPane.setPreferredSize(new Dimension(500, 300));

            JButton eliminarTodoBtn = UiTool.crearBotonConIcono("Eliminar todos", Constants.ICONS_PATH + "trash.png", 20, 20, null);
            JButton eliminarBtn = UiTool.crearBotonConIcono("Eliminar", Constants.ICONS_PATH + "eliminar-uno.png", 20, 20, null);
            JButton visitarBtn = UiTool.crearBotonConIcono("Abrir", Constants.ICONS_PATH + "browse.png", 20, 20, null);
            JButton cerrarBtn = UiTool.crearBotonConIcono("Cerrar", Constants.ICONS_PATH + "close.png", 20, 20, null);

            eliminarTodoBtn.addActionListener(e -> {
                if (JOptionPane.showConfirmDialog(parent, "¿Estás seguro de eliminar todos los favoritos?", "Confirmación", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    UiTool.cerrarVentana(cerrarBtn);
                    favoritoService.eliminarTodo();
                    if (actualizarEstadoBotones != null) actualizarEstadoBotones.run();
                    JOptionPane.showMessageDialog(parent, "Favoritos eliminados.");
                }
            });

            eliminarBtn.addActionListener(e -> {
                int selectedRow = favoritosTable.getSelectedRow();
                if (selectedRow != -1) {
                    String nombreFavorito = (String) tableModel.getValueAt(selectedRow, 0);
                    String urlFavorito = (String) tableModel.getValueAt(selectedRow, 1);
                    favoritoService.eliminarElemento(new Favorito(nombreFavorito, urlFavorito));
                    tableModel.removeRow(selectedRow);
                    if (actualizarEstadoBotones != null) actualizarEstadoBotones.run();
                    JOptionPane.showMessageDialog(parent, "Favorito eliminado.");
                } else {
                    JOptionPane.showMessageDialog(parent, "Por favor, selecciona un favorito.");
                }
            });

            visitarBtn.addActionListener(e -> {
                int selectedRow = favoritosTable.getSelectedRow();
                if (selectedRow != -1) {
                    String urlFavorito = (String) tableModel.getValueAt(selectedRow, 1);
                    if (cargarURL != null) cargarURL.accept(urlFavorito);
                    UiTool.cerrarVentana(cerrarBtn);
                } else {
                    JOptionPane.showMessageDialog(parent, "Por favor, selecciona un favorito.");
                }
            });

            cerrarBtn.addActionListener(e -> UiTool.cerrarVentana(cerrarBtn));

            Object[] options = {eliminarTodoBtn, eliminarBtn, visitarBtn, cerrarBtn};
            JOptionPane.showOptionDialog(parent, scrollPane, Constants.TITULO_FAVORITOS,
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                    options, null);
        }
    }

    /**
     * Agrega una nueva URL a la lista de favoritos.
     *
     * @param parent ventana principal para mostrar mensajes
     * @param url URL a agregar como favorito
     * @param actualizarEstadoBotones acción para actualizar el estado de los botones (puede ser null)
     */
    public void agregarFavorito(JFrame parent, String url, Runnable actualizarEstadoBotones) {
        if (url == null || url.isBlank()) {
            JOptionPane.showMessageDialog(parent, "No hay URL para agregar a favoritos.", "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String nombre = JOptionPane.showInputDialog(parent, "Ingresa un nombre para el favorito:");
        if (nombre != null && !nombre.isBlank()) {
            if (favoritoService.existeFavorito(url)) {
                JOptionPane.showMessageDialog(parent, "La URL ya está en favoritos.", "Información", JOptionPane.INFORMATION_MESSAGE);
            } else {
                favoritoService.agregarElemento(new Favorito(nombre, url));
                JOptionPane.showMessageDialog(parent, "Favorito agregado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            }
            if (actualizarEstadoBotones != null) actualizarEstadoBotones.run();
        } else if (nombre != null) {
            JOptionPane.showMessageDialog(parent, "Por favor, ingresa un nombre válido.", "Información", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Verifica si una URL ya existe en la lista de favoritos.
     *
     * @param url URL a verificar
     * @return true si la URL ya es un favorito, false en caso contrario
     */
    public boolean existeFavorito(String url) {
        return favoritoService.existeFavorito(url);
    }

    /**
     * Obtiene todos los favoritos almacenados.
     *
     * @return tabla hash con los nombres y URLs de los favoritos
     */
    public Hashtable<String, String> obtenerFavoritos() {
        return favoritoService.obtenerTodo();
    }

    /**
     * Elimina un favorito específico.
     *
     * @param favorito favorito a eliminar
     */
    public void eliminarFavorito(Favorito favorito) {
        favoritoService.eliminarElemento(favorito);
    }

    /**
     * Elimina todos los favoritos almacenados.
     */
    public void eliminarTodo() {
        favoritoService.eliminarTodo();
    }
}