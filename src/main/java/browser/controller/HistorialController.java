package browser.controller;

import browser.data_structure.LinkedList;
import browser.model.EntradaHistorial;
import browser.service.Impl.HistorialServiceImpl;
import browser.util.UiTool;
import lombok.RequiredArgsConstructor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

@RequiredArgsConstructor
public class HistorialController {
    private final HistorialServiceImpl historialService;
    private final String ICONS_PATH;

    public void agregarElemento(EntradaHistorial entradaHistorial) {
        historialService.agregarElemento(entradaHistorial);
    }

    public void mostrarHistorial(JFrame parent, Runnable actualizarEstadoBotones, Runnable restablecerNavegacion, java.util.function.Consumer<String> cargarURL) {
        LinkedList<EntradaHistorial> historialCompleto = historialService.obtenerTodo();

        if (historialCompleto.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "El historial está vacío.");
        } else {
            JTable historialTable = UiTool.crearTabla("Historial de Navegación", new String[]{"SITIO", "FECHA"}, historialCompleto.stream()
                    .map(entrada -> new Object[]{entrada.getUrl(), entrada.getFecha()}).toList());
            DefaultTableModel tableModel = (DefaultTableModel) historialTable.getModel();
            JScrollPane scrollPane = new JScrollPane(historialTable);

            scrollPane.setPreferredSize(new Dimension(500, 300));
            historialTable.getColumnModel().getColumn(0).setPreferredWidth(350);

            JButton eliminarTodoBtn = UiTool.crearBotonConIcono("Eliminar todo", ICONS_PATH + "trash.png", 20, 20, null);
            JButton eliminarBtn = UiTool.crearBotonConIcono("Eliminar", ICONS_PATH + "eliminar-uno.png", 20, 20, null);
            JButton visitarBtn = UiTool.crearBotonConIcono("Abrir", ICONS_PATH + "browse.png", 20, 20, null);
            JButton cerrarBtn = UiTool.crearBotonConIcono("Cerrar", ICONS_PATH + "close.png", 20, 20, null);

            eliminarTodoBtn.addActionListener(e -> {
                if (JOptionPane.showConfirmDialog(parent, "¿Estás seguro de eliminar todo el historial?", "Confirmación", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    UiTool.cerrarVentana(cerrarBtn);
                    historialService.eliminarTodo();
                    restablecerNavegacion.run();
                    actualizarEstadoBotones.run();
                    JOptionPane.showMessageDialog(parent, "Historial eliminado.");
                }
            });

            eliminarBtn.addActionListener(e -> {
                int selectedRow = historialTable.getSelectedRow();
                if (selectedRow != -1) {
                    EntradaHistorial entradaSeleccionada = new EntradaHistorial((String) tableModel.getValueAt(selectedRow, 0),
                            (String) tableModel.getValueAt(selectedRow, 1));
                    historialService.eliminarElemento(entradaSeleccionada);
                    tableModel.removeRow(selectedRow);
                    JOptionPane.showMessageDialog(parent, "Entrada de historial eliminada.");
                } else {
                    JOptionPane.showMessageDialog(parent, "Por favor, selecciona una entrada.");
                }
            });

            visitarBtn.addActionListener(e -> {
                int selectedRow = historialTable.getSelectedRow();
                if (selectedRow != -1) {
                    String urlSeleccionada = (String) tableModel.getValueAt(selectedRow, 0);
                    UiTool.cerrarVentana(cerrarBtn);
                    
                    if(cargarURL != null) {
                        cargarURL.accept(urlSeleccionada);
                    } else {
                        JOptionPane.showMessageDialog(parent, "No se ha definido una acción para cargar la URL.");
                    }

                } else {
                    JOptionPane.showMessageDialog(parent, "Por favor, selecciona una URL.");
                }
            });

            cerrarBtn.addActionListener(e -> UiTool.cerrarVentana(cerrarBtn));

            Object[] options = {eliminarTodoBtn, eliminarBtn, visitarBtn, cerrarBtn};
            JOptionPane.showOptionDialog(parent, scrollPane, "Historial de Navegación",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                    options, null);
        }
    }
}