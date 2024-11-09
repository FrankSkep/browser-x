import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

public class InterfazHistorialNavegacion extends JFrame {
    private JTextField urlTextField;
    private JTextArea historialTextArea;
    private JLabel paginaActualLabel;
    private HistorialNavegacion historial;
    private WebView webView;
    private WebEngine webEngine;

    public InterfazHistorialNavegacion() {
        historial = new HistorialNavegacion();

        try {
            // UIManager.getSystemLookAndFeelClassName() establece el aspecto del sistema
            UIManager.setLookAndFeel(NimbusLookAndFeel.class.getName());
        } catch (
                Exception e) {
            System.err.println("No se pudo establecer el aspecto del sistema.");
        }

        setTitle("SimpleBrowse");
        setSize(1280, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel superior para la URL y botones de navegación
        JPanel panelSuperior = new JPanel();
        panelSuperior.setLayout(new FlowLayout());

        // iconos para los botones
//        ImageIcon retrocederIcon = new ImageIcon("src/main/resources/icons/retroceder.png");
//        ImageIcon avanzarIcon = new ImageIcon("src/main/resources/icons/avanzar.png");
        ImageIcon refrescarIcon = new ImageIcon(Objects.requireNonNull(UtilsUI.redimensionarImagen("src/main/resources/icons/refresh.png", 20, 20)));
        ImageIcon visitarIcon = new ImageIcon(Objects.requireNonNull(UtilsUI.redimensionarImagen("src/main/resources/icons/search.png", 20, 20)));

        JButton retrocederButton = new JButton("Retroceder");
        JButton avanzarButton = new JButton("Avanzar");
        JButton refrescarButton = new JButton(refrescarIcon);
        urlTextField = new JTextField(40);
        urlTextField.setPreferredSize(new Dimension(400, 30));
        JButton visitarButton = new JButton(visitarIcon);

        panelSuperior.add(retrocederButton);
        panelSuperior.add(avanzarButton);
        panelSuperior.add(refrescarButton);
        panelSuperior.add(new JLabel("URL:"));
        panelSuperior.add(urlTextField);
        panelSuperior.add(visitarButton);
        add(panelSuperior, BorderLayout.NORTH);

        // Panel central para mostrar el contenido web usando WebView (JavaFX)
        JFXPanel fxPanel = new JFXPanel();
        add(fxPanel, BorderLayout.CENTER);

        // Inicializar JavaFX
        Platform.runLater(() -> {
            webView = new WebView();
            webEngine = webView.getEngine();
            fxPanel.setScene(new Scene(webView));
        });

        // Panel inferior para mostrar el historial y la página actual
        JPanel panelInferior = new JPanel();
        paginaActualLabel = new JLabel("Página actual: Ninguna");
        historialTextArea = new JTextArea(5, 20);
        historialTextArea.setEditable(false);
        panelInferior.setLayout(new BorderLayout());
        panelInferior.add(paginaActualLabel, BorderLayout.NORTH);
        panelInferior.add(new JScrollPane(historialTextArea), BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);

        // Listeners para los botones
        visitarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                visitarPagina();
            }
        });

        retrocederButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                retrocederPagina();
            }
        });

        avanzarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                avanzarPagina();
            }
        });

        refrescarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refrescarPagina();
            }
        });

        setVisible(true);
    }

    // visitar una página nueva
    private void visitarPagina() {
        String url = urlTextField.getText();
        if (!url.isEmpty()) {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url; // agrega protocolo a la URL si no tiene
            }
            String finalUrl = url;
            Platform.runLater(() -> {
                webEngine.load(finalUrl); // carga la página web en el WebView
                historial.visitar(finalUrl);
                actualizarInterfaz();
                urlTextField.setText("");
            });
        }
    }

    // retroceder en el historial
    private void retrocederPagina() {
        String urlAnterior = historial.retroceder();
        if (urlAnterior != null) {
            Platform.runLater(() -> {
                webEngine.load(urlAnterior); // carga la página web anterior
                actualizarInterfaz();
            });
        } else {
            JOptionPane.showMessageDialog(this, "No hay páginas anteriores.");
        }
    }

    // avanzar en el historial
    private void avanzarPagina() {
        String urlSiguiente = historial.avanzar();
        if (urlSiguiente != null) {
            Platform.runLater(() -> {
                webEngine.load(urlSiguiente); // carga la página web siguiente
                actualizarInterfaz();
            });
        } else {
            JOptionPane.showMessageDialog(this, "No hay páginas siguientes.");
        }
    }

    // refrescar la página actual
    private void refrescarPagina() {
        Platform.runLater(() -> {
            webEngine.reload(); // recarga la página web actual
        });
    }

    // actualizar la interfaz grafica
    private void actualizarInterfaz() {
        String urlActual = historial.obtenerURLActual();
        if (urlActual != null) {
            paginaActualLabel.setText("Página actual: " + urlActual);
        } else {
            paginaActualLabel.setText("Página actual: Ninguna");
        }

        historialTextArea.setText(historial.obtenerHistorialCompleto());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new JFXPanel(); // Inicializar el toolkit de JavaFX
            new InterfazHistorialNavegacion();
        });
    }
}