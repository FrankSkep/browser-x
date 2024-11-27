package browser.util;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class UiTool {

    // redimensiona una imagen a un tamaño específico
    private static Image redimensionarImagen(String path, int width, int height) {
        try {
            BufferedImage originalImage = ImageIO.read(new File(path));
            return originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        } catch (
                IOException e) {
            System.err.println("Error:  " + e.getMessage());
            return null;
        }
    }

    // crea un boton con icono y margen
    public static JButton crearBotonConIcono(String text, String rutaIcono, int ancho, int alto, Integer margin) {
        ImageIcon icono = cargarIcono(rutaIcono, ancho, alto);

        JButton boton;
        if (text != null) {
            boton = new JButton(text, icono);
        } else {
            boton = new JButton(icono);
        }

        if (margin != null) {
            boton.setMargin(new Insets(3, 3, 3, 3));
        }
        return boton;
    }

    // crea una tabla e insertar datos
    public static JTable crearTabla(String titulo, String[] columnas, List<Object[]> datos) {
        DefaultTableModel model = new DefaultTableModel(null, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tabla = new JTable(model);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getTableHeader().setResizingAllowed(false);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        for (Object[] dato : datos) {
            model.addRow(dato);
        }
        return tabla;
    }

    // carga un icono y lo redimensiona
    public static ImageIcon cargarIcono(String ruta, int ancho, int alto) {
        return new ImageIcon(Objects.requireNonNull(redimensionarImagen(ruta, ancho, alto)));
    }

    // obtiene la ventana padre de un componente y la cierra
    public static void cerrarVentana(Component componente) {
        Window window = SwingUtilities.getWindowAncestor(componente);
        if (window != null) {
            window.dispose();
        }
    }
}
