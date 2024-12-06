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

/**
 * Utilidad para la creación y manipulación de componentes de la interfaz de usuario.
 */
public class UiTool {

    /**
     * Redimensiona una imagen a un tamaño específico.
     *
     * @param path   La ruta de la imagen.
     * @param width  El ancho deseado.
     * @param height La altura deseada.
     * @return La imagen redimensionada.
     */
    private static Image redimensionarImagen(String path, int width, int height) {
        try {
            BufferedImage originalImage = ImageIO.read(new File(path));
            return originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        } catch (
                IOException e) {
            System.err.println("Error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Crea un botón con un icono y margen opcional.
     *
     * @param text      El texto del botón.
     * @param rutaIcono La ruta del icono.
     * @param ancho     El ancho del icono.
     * @param alto      La altura del icono.
     * @param margin    El margen del botón (puede ser null).
     * @return El botón creado.
     */
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

    /**
     * Crea una tabla e inserta datos en ella.
     *
     * @param titulo   El título de la tabla.
     * @param columnas Los nombres de las columnas.
     * @param datos    Los datos a insertar en la tabla.
     * @return La tabla creada.
     */
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

    /**
     * Carga un icono y lo redimensiona.
     *
     * @param ruta  La ruta del icono.
     * @param ancho El ancho deseado.
     * @param alto  La altura deseada.
     * @return El icono redimensionado.
     */
    public static ImageIcon cargarIcono(String ruta, int ancho, int alto) {
        return new ImageIcon(Objects.requireNonNull(redimensionarImagen(ruta, ancho, alto)));
    }

    /**
     * Obtiene la ventana padre de un componente y la cierra.
     *
     * @param componente El componente cuyo ventana padre se va a cerrar.
     */
    public static void cerrarVentana(Component componente) {
        Window window = SwingUtilities.getWindowAncestor(componente);
        if (window != null) {
            window.dispose();
        }
    }
}