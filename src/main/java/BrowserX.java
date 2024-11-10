import com.formdev.flatlaf.FlatLightLaf;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.Objects;

public class BrowserX extends JFrame {
    private final JTextField urlTextField;
    private final HistorialNavegacion historial;
    private WebView webView;
    private WebEngine webEngine;

    private final Map<String, String> defaultPages = Map.of(
            "Home", Objects.requireNonNull(getClass().getResource("/templates/home.html")).toExternalForm(),
            "Error", Objects.requireNonNull(getClass().getResource("/templates/error.html")).toExternalForm()
    );

    // bandera para saber si la navegación fue realizada por el usuario o por el historial
    private boolean navegacionUsuario = false;

    public BrowserX() {
        historial = new HistorialNavegacion();

        try {
            FlatLightLaf.setup(); // Para el tema claro
            UIManager.setLookAndFeel(new FlatLightLaf());
            // UIManager.getSystemLookAndFeelClassName() // Establece el aspecto del sistema
            // UIManager.setLookAndFeel(new NimbusLookAndFeel()); // Establece el aspecto Nimbus
            Image icon = ImageIO.read(Objects.requireNonNull(getClass().getResource("/icons/browser.png")));
            setIconImage(icon);
        } catch (
                Exception e) {
            System.err.println("Ocurrio un error: " + e.getMessage());
        }

        setTitle("BrowserX");
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

        // Iconos para los botones
        ImageIcon retrocederIcon = new ImageIcon(Objects.requireNonNull(Utils.redimensionarImagen("src/main/resources/icons/left.png", 25, 25)));
        ImageIcon avanzarIcon = new ImageIcon(Objects.requireNonNull(Utils.redimensionarImagen("src/main/resources/icons/right.png", 25, 25)));
        ImageIcon homeIcon = new ImageIcon(Objects.requireNonNull(Utils.redimensionarImagen("src/main/resources/icons/home.png", 25, 25)));
        ImageIcon refrescarIcon = new ImageIcon(Objects.requireNonNull(Utils.redimensionarImagen("src/main/resources/icons/refresh.png", 25, 25)));
        ImageIcon visitarIcon = new ImageIcon(Objects.requireNonNull(Utils.redimensionarImagen("src/main/resources/icons/search.png", 25, 25)));
        ImageIcon toggleHistorialIcon = new ImageIcon(Objects.requireNonNull(Utils.redimensionarImagen("src/main/resources/icons/historial.png", 25, 25)));

        // Creación de los botones y campos de texto
        JButton retrocederButton = new JButton(retrocederIcon);
        JButton avanzarButton = new JButton(avanzarIcon);
        JButton homeButton = new JButton(homeIcon);
        JButton refrescarButton = new JButton(refrescarIcon);
        panelBotones.add(retrocederButton);
        panelBotones.add(avanzarButton);
        panelBotones.add(homeButton);
        panelBotones.add(refrescarButton);

        // Panel para el campo de texto y los botones de visitar e historial
        JPanel panelURL = new JPanel();
        panelURL.setLayout(new BorderLayout());
        urlTextField = new JTextField(40);
        panelURL.add(urlTextField, BorderLayout.CENTER);

        // Panel para los botones de visitar e historial
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
                if (newState == Worker.State.RUNNING) {
                    String finalUrl = webEngine.getLocation();
                    if (!navegacionUsuario) {
                        historial.visitar(finalUrl);
                    }
                    navegacionUsuario = false;
                    actualizarInterfaz();
                } else if (newState == Worker.State.FAILED) {
                    urlTextField.setText("Ingresa una URL valida");
                    urlTextField.setForeground(Color.RED);
                }
            });

            // Cargar la página inicial
            webEngine.load("https://www.google.com");
        });

        // Listeners para los botones
        visitarButton.addActionListener(e -> visitarPagina());

        retrocederButton.addActionListener(e -> retrocederPagina());

        avanzarButton.addActionListener(e -> avanzarPagina());

        homeButton.addActionListener(e -> {
            Platform.runLater(() -> {
                webEngine.load("https://www.google.com");
                webView = new WebView();
                webEngine = webView.getEngine();
            });
            historial.deleteHistory();
        });

        refrescarButton.addActionListener(e -> refrescarPagina());

        toggleHistorialButton.addActionListener(e -> {
            String[] options = {"Eliminar", "Cerrar"};
            String historialCompleto = historial.obtenerHistorialCompleto();
            if (historialCompleto.isEmpty()) {
                JOptionPane.showMessageDialog(null, "El historial está vacío.");
            } else {
                JTextArea textArea = new JTextArea(historialCompleto);
                textArea.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(400, 300));

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
        });

        // Listeners para el campo de texto
        urlTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (urlTextField.getText().equals("Ingresa una URL")) {
                    urlTextField.setText("");
                    urlTextField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (urlTextField.getText().isEmpty()) {
                    urlTextField.setText("Ingresa una URL");
                    urlTextField.setForeground(Color.GRAY);
                }
            }
        });

        urlTextField.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                urlTextField.selectAll();
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

        setVisible(true);
    }

    // visitar una página nueva
    private void visitarPagina() {
        String url = urlTextField.getText();
        if (!url.isEmpty()) {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url;

                String finalUrl = url;
                Platform.runLater(() -> webEngine.load(finalUrl));
            }
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
        Platform.runLater(() -> webEngine.reload());
    }

    // actualizar la interfaz grafica
    private void actualizarInterfaz() {
        String urlActual = historial.obtenerURLActual();
        if (urlActual != null) {
            urlTextField.setForeground(Color.BLACK);
            urlTextField.setText(urlActual);
        } else {
            urlTextField.setText("Ingrese una URL o realiza una busqueda");
            urlTextField.setForeground(Color.GRAY);
            Platform.runLater(() -> webEngine.load(defaultPages.get("Home")));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new JFXPanel(); // Inicializar el toolkit de JavaFX
            new BrowserX();
        });
    }
}