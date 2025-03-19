package browser.util;

import javax.swing.*;
import java.awt.*;

/**
 * La clase DownloadProgressDialog representa un cuadro de diálogo modal que muestra el progreso de una descarga.
 * Utiliza un JProgressBar para indicar el progreso de la descarga.
 */
public class DownloadProgressDialog extends JDialog {
    private final JProgressBar progressBar;

    /**
     * Constructor que crea un cuadro de diálogo de progreso de descarga.
     *
     * @param parent el marco padre del cuadro de diálogo.
     */
    public DownloadProgressDialog(JFrame parent) {
        super(parent, "Descargando...", true);
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        setLayout(new BorderLayout());
        add(progressBar, BorderLayout.CENTER);
        setSize(300, 75);
        setLocationRelativeTo(parent);
    }

    /**
     * Actualiza el progreso de la barra de progreso.
     *
     * @param progress el valor del progreso a establecer (0-100).
     */
    public void updateProgress(int progress) {
        progressBar.setValue(progress);
    }
}