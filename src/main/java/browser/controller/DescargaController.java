package browser.controller;

import browser.model.Descarga;
import browser.service.Impl.DescargaServiceImpl;
import browser.util.Constants;
import browser.util.UiTool;
import browser.data_structure.LinkedList;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class DescargaController {
    private final DescargaServiceImpl descargaService;

    public DescargaController(DescargaServiceImpl descargaService) {
        this.descargaService = descargaService;
    }

    public void mostrarDescargas(JFrame parent) {
        LinkedList<Descarga> historialDescargas = descargaService.obtenerTodo();

        if (historialDescargas.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "No hay descargas.", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            JTable descargasTable = UiTool.crearTabla("Historial de Descargas", new String[]{"NOMBRE", "SITIO", "FECHA"},
                    historialDescargas.stream()
                            .map(descarga -> new Object[]{descarga.getNombre(), descarga.getUrl(), descarga.getFecha()}).toList());

            DefaultTableModel tableModel = (DefaultTableModel) descargasTable.getModel();

            JScrollPane scrollPane = new JScrollPane(descargasTable);
            scrollPane.setPreferredSize(new Dimension(500, 300));

            JButton eliminarTodoBtn = UiTool.crearBotonConIcono("Eliminar todas", Constants.ICONS_PATH + "trash.png", 20, 20, null);
            JButton eliminarBtn = UiTool.crearBotonConIcono("Eliminar", Constants.ICONS_PATH + "eliminar-uno.png", 20, 20, null);
            JButton cerrarBtn = UiTool.crearBotonConIcono("Cerrar", Constants.ICONS_PATH + "close.png", 20, 20, null);

            eliminarTodoBtn.addActionListener(e -> {
                if (JOptionPane.showConfirmDialog(parent, "¿Estás seguro de eliminar todas las descargas?", "Confirmación", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    UiTool.cerrarVentana(cerrarBtn);
                    descargaService.eliminarTodo();
                    JOptionPane.showMessageDialog(parent, "Descargas eliminadas.", "Información", JOptionPane.INFORMATION_MESSAGE);
                }
            });

            eliminarBtn.addActionListener(e -> {
                int selectedRow = descargasTable.getSelectedRow();
                if (selectedRow != -1) {
                    String nombre = (String) tableModel.getValueAt(selectedRow, 0);
                    String url = (String) tableModel.getValueAt(selectedRow, 1);
                    String fecha = (String) tableModel.getValueAt(selectedRow, 2);
                    descargaService.eliminarElemento(new Descarga(nombre, url, fecha));
                    tableModel.removeRow(selectedRow);
                    JOptionPane.showMessageDialog(parent, "Descarga eliminada.", "Información", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(parent, "Por favor, selecciona una descarga.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            cerrarBtn.addActionListener(e -> UiTool.cerrarVentana(cerrarBtn));

            Object[] options = {eliminarTodoBtn, eliminarBtn, cerrarBtn};
            JOptionPane.showOptionDialog(parent, scrollPane, "Historial de Descargas",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                    options, null);
        }
    }

    public void descargarArchivo(String url, JFrame parent) {
        descargaService.descargarArchivo(url, parent);
    }
}