import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class InterfazHistorialNavegacion extends JFrame {
    private final JTextField urlTextField;
    private final HistorialNavegacion historial;
    private WebView webView;
    private WebEngine webEngine;

    private Map<String, String> programUrls = Map.of(
            "Home", "file:///C:/Users/fran/Documents/Development/Java/SimpleBrowse/target/classes/templates/home.html",
            "Error", "file:///C:/Users/fran/Documents/Development/Java/SimpleBrowse/target/classes/templates/error.html"
    );

    // bandera para saber si la navegación fue realizada por el usuario o por el historial
    private boolean navegacionUsuario = false;

    public InterfazHistorialNavegacion() {
        historial = new HistorialNavegacion();

        try {
            // UIManager.getSystemLookAndFeelClassName() establece el aspecto del sistema
            UIManager.setLookAndFeel(NimbusLookAndFeel.class.getName());
        } catch (
                Exception e) {
            System.err.println("No se pudo establecer el aspecto del sistema.");
        }

        try {
            Image icon = ImageIO.read(Objects.requireNonNull(getClass().getResource("/icons/browser.png")));
            setIconImage(icon);
        } catch (
                IOException e) {
            e.printStackTrace();
        }

        setTitle("SimpleBrowse");
        setSize(1280, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel superior para la URL y botones de navegación
        JPanel panelSuperior = new JPanel();
        panelSuperior.setLayout(new BorderLayout());

        // Panel para botones de navegacion
        JPanel panelBotones = new JPanel();
        panelBotones.setLayout(new FlowLayout(FlowLayout.LEFT));

        ImageIcon retrocederIcon = new ImageIcon(Objects.requireNonNull(UtilsUI.redimensionarImagen("src/main/resources/icons/left.png", 20, 20)));
        ImageIcon avanzarIcon = new ImageIcon(Objects.requireNonNull(UtilsUI.redimensionarImagen("src/main/resources/icons/right.png", 20, 20)));
        ImageIcon refrescarIcon = new ImageIcon(Objects.requireNonNull(UtilsUI.redimensionarImagen("src/main/resources/icons/refresh.png", 20, 20)));
        ImageIcon visitarIcon = new ImageIcon(Objects.requireNonNull(UtilsUI.redimensionarImagen("src/main/resources/icons/search.png", 20, 20)));
        ImageIcon toggleHistorialIcon = new ImageIcon(Objects.requireNonNull(UtilsUI.redimensionarImagen("src/main/resources/icons/historial.png", 20, 20)));

        // Creación de los botones y campos de texto
        JButton retrocederButton = new JButton(retrocederIcon);
        JButton avanzarButton = new JButton(avanzarIcon);
        JButton refrescarButton = new JButton(refrescarIcon);
        panelBotones.add(retrocederButton);
        panelBotones.add(avanzarButton);
        panelBotones.add(refrescarButton);

        // Panel para el campo de texto y los botones de visitar e historial
        JPanel panelURL = new JPanel();
        panelURL.setLayout(new BorderLayout());
        urlTextField = new JTextField(40);
        panelURL.add(urlTextField, BorderLayout.CENTER);

        JPanel panelVisitarHistorial = new JPanel();
        panelVisitarHistorial.setLayout(new FlowLayout(FlowLayout.RIGHT));

        JButton visitarButton = new JButton(visitarIcon);
        JButton toggleHistorialButton = new JButton(toggleHistorialIcon);
        panelVisitarHistorial.add(visitarButton);
        panelVisitarHistorial.add(toggleHistorialButton);

        panelURL.add(panelVisitarHistorial, BorderLayout.EAST);

        panelSuperior.add(panelBotones, BorderLayout.WEST);
        panelSuperior.add(panelURL, BorderLayout.CENTER);

        add(panelSuperior, BorderLayout.NORTH);

        // Panel central para mostrar el contenido web usando WebView (JavaFX)
        JFXPanel fxPanel = new JFXPanel();
        add(fxPanel, BorderLayout.CENTER);

        // Inicializar JavaFX
        Platform.runLater(() -> {
            webView = new WebView();
            webEngine = webView.getEngine();
            fxPanel.setScene(new Scene(webView));

            // Listener para el cambio de URL en el WebView
            webEngine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    String finalUrl = webEngine.getLocation();
                    if (!navegacionUsuario && !programUrls.containsValue(finalUrl)) {
                        historial.visitar(finalUrl);
                    }
                    navegacionUsuario = false;
                    actualizarInterfaz();
                }
            });
        });

        // Listeners para los botones
        visitarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                visitarPagina();
            }
        });

        urlTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    visitarPagina();
                }
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

        toggleHistorialButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String[] options = {"Eliminar", "Cerrar"};
                String historialCompleto = historial.obtenerHistorialCompleto();
                if (historialCompleto.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "El historial está vacío.");
                } else {
                    JScrollPane scrollPane = new JScrollPane(new JTextArea(historialCompleto));
                    scrollPane.setPreferredSize(new Dimension(400, 300));
                    scrollPane.setEnabled(false);

                    int choice = JOptionPane.showOptionDialog(
                            null,
                            scrollPane,
                            "Historial de Navegación",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.INFORMATION_MESSAGE,
                            null,
                            options,
                            options[1]
                    );

                    if (choice == 0) { // Eliminar
                        historial.deleteHistory();
                        actualizarInterfaz();
                        JOptionPane.showMessageDialog(null, "Historial eliminado.");
                    }
                }
            }
        });

        setVisible(true);
    }

    // visitar una página nueva
    private void visitarPagina() {
        String url = urlTextField.getText();
        if (!url.isEmpty()) {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url; // adds protocol to the URL if it doesn't have one
            }
            String finalUrl = url;
            Platform.runLater(() -> {
                webEngine.load(finalUrl); // loads the web page in the WebView
            });
        }
    }

    // retroceder en el historial
    private void retrocederPagina() {
        String urlAnterior = historial.retroceder();
        if (urlAnterior != null) {
            navegacionUsuario = true;
            Platform.runLater(() -> {
                webEngine.load(urlAnterior); // carga la página web anterior
            });
        } else {
            JOptionPane.showMessageDialog(this, "No hay páginas anteriores.");
        }
    }

    // avanzar en el historial
    private void avanzarPagina() {
        String urlSiguiente = historial.avanzar();
        if (urlSiguiente != null) {
            navegacionUsuario = true;
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
            urlTextField.setText(urlActual);
        } else {
            urlTextField.setText(""); // or set to a default message
            Platform.runLater(() -> {
                String homeUrl = Objects.requireNonNull(getClass().getResource("/templates/home.html")).toExternalForm();
                webEngine.load(homeUrl);
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new JFXPanel(); // Inicializar el toolkit de JavaFX
            new InterfazHistorialNavegacion();
        });
    }
}