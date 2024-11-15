package browser;

import browser.ui.UI_Utils;
import browser.data_structures.LinkedList;
import browser.service.HistorialNavegacion;
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

    // bandera para saber si la navegación fue natural o por avanzar/retroceder
    private boolean navegacionUsuario = false;

    public BrowserX() {
        historial = new HistorialNavegacion();

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            // UIManager.getSystemLookAndFeelClassName() // Establece el aspecto del sistema
            // UIManager.setLookAndFeel(new NimbusLookAndFeel()); // Establece el aspecto Nimbus
            Image icon = ImageIO.read(Objects.requireNonNull(getClass().getResource("/icons/browserx-icon.png")));
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

        // Panel para botones de navegacion
        JPanel panelBotones = new JPanel();
        panelBotones.setLayout(new FlowLayout(FlowLayout.LEFT));

        // Creación de los botones
        JButton retrocederButton = UI_Utils.crearBotonConIcono(null, "src/main/resources/icons/previous-page.png", 25, 25, 3);
        JButton avanzarButton = UI_Utils.crearBotonConIcono(null, "src/main/resources/icons/next-page.png", 25, 25, 3);
        JButton inicioBtn = UI_Utils.crearBotonConIcono(null, "src/main/resources/icons/home.png", 25, 25, 3);
        JButton refrescarButton = UI_Utils.crearBotonConIcono(null, "src/main/resources/icons/refresh.png", 25, 25, 3);
        panelBotones.add(retrocederButton);
        panelBotones.add(avanzarButton);
        panelBotones.add(inicioBtn);
        panelBotones.add(refrescarButton);

        // Panel para los botones de visitar y menu
        JPanel panelMenu = new JPanel();
        panelMenu.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JButton visitarButton = UI_Utils.crearBotonConIcono(null, "src/main/resources/icons/browse.png", 25, 25, 3);
        JButton showMenu = UI_Utils.crearBotonConIcono(null, "src/main/resources/icons/menu.png", 25, 25, 3);
        panelMenu.add(visitarButton);
        panelMenu.add(showMenu);

        // Panel para el campo de texto y los botones de visitar y menu
        JPanel panelURL = new JPanel();
        panelURL.setLayout(new BorderLayout());
        urlTextField = new JTextField();
        panelURL.add(urlTextField, BorderLayout.CENTER);
        panelURL.add(panelMenu, BorderLayout.EAST);

        // Panel superior para la URL y botones de navegación
        JPanel panelSuperior = new JPanel();
        panelSuperior.setLayout(new BorderLayout());
        panelSuperior.add(panelBotones, BorderLayout.WEST);
        panelSuperior.add(panelURL, BorderLayout.CENTER);

        add(panelSuperior, BorderLayout.NORTH);

        // Panel central para mostrar el WebView embebido de JavaFX en Swing
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
                    actualizarInterfazSwing();
                } else if (newState == Worker.State.FAILED) {
                    urlTextField.setText("");
                    JOptionPane.showMessageDialog(this, "No se pudo cargar la página, verifica la URL.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            // carga pagina inicial (Google)
            cargarURL("https://www.google.com");
        });

        // Listeners para los botones
        visitarButton.addActionListener(e -> visitarPagina());

        retrocederButton.addActionListener(e -> retrocederPagina());

        avanzarButton.addActionListener(e -> avanzarPagina());

        inicioBtn.addActionListener(e -> cargarURL("https://www.google.com"));

        refrescarButton.addActionListener(e -> refrescarPagina());

        showMenu.addActionListener(e -> mostrarMenuEmergente(showMenu));

        // Listeners para el campo de texto
        urlTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (urlTextField.getText().equals("Ingrese una URL")) {
                    urlTextField.setText("");
                    urlTextField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (urlTextField.getText().isEmpty()) {
                    urlTextField.setText("Ingrese una URL");
                    urlTextField.setForeground(Color.GRAY);
                }
            }
        });

        // Seleccionar todo el texto al hacer clic en el campo de texto
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

    // visitar una pagina nueva
    private void visitarPagina() {
        String url = urlTextField.getText();
        url = url.equals("Ingrese una URL") ? "" : url;
        if (!url.isEmpty() || !url.isBlank()) {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url;
            }
            String finalUrl = url;
            cargarURL(finalUrl);
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese una URL.");
        }
    }

    // retroceder en el historial
    private void retrocederPagina() {
        String urlAnterior = historial.retroceder();
        if (urlAnterior != null) {
            navegacionUsuario = true;
            cargarURL(urlAnterior);
        } else {
            JOptionPane.showMessageDialog(this, "No hay páginas anteriores.");
        }
    }

    // avanzar en el historial
    private void avanzarPagina() {
        String urlSiguiente = historial.avanzar();
        if (urlSiguiente != null) {
            navegacionUsuario = true;
            cargarURL(urlSiguiente);
        } else {
            JOptionPane.showMessageDialog(this, "No hay páginas siguientes.");
        }
    }

    // carga una URL en el WebView
    private void cargarURL(String url) {
        Platform.runLater(() -> webEngine.load(url));
    }

    // refrescar la página actual
    private void refrescarPagina() {
        Platform.runLater(() -> webEngine.reload());
    }

    // actualizar la URL en el campo de texto
    private void actualizarInterfazSwing() {
        String urlActual = historial.obtenerURLActual();
        if (urlActual != null) {
            urlTextField.setForeground(Color.BLACK);
            urlTextField.setText(urlActual);
        }
    }

    // muestra menu de opciones
    private void mostrarMenuEmergente(JButton menuButton) {
        // iconos para las opciones del menu
        ImageIcon historialIcon = UI_Utils.cargarIcono("src/main/resources/icons/record.png", 25, 25);
        ImageIcon favoritosIcon = UI_Utils.cargarIcono("src/main/resources/icons/favoritos.png", 25, 25);
        ImageIcon configuracionIcon = UI_Utils.cargarIcono("src/main/resources/icons/settings.png", 25, 25);

        // botones del menu
        JMenuItem historialOpc = new JMenuItem("Historial", historialIcon);
        JMenuItem favoritosOpc = new JMenuItem("Favoritos", favoritosIcon);
        JMenuItem configuracionOpc = new JMenuItem("Configuración", configuracionIcon);
        historialOpc.setIconTextGap(20);
        favoritosOpc.setIconTextGap(20);
        configuracionOpc.setIconTextGap(20);
        Font font = new Font("Arial", Font.PLAIN, 16);
        historialOpc.setFont(font);
        favoritosOpc.setFont(font);
        configuracionOpc.setFont(font);

        JPopupMenu menuEmergente = new JPopupMenu();
        menuEmergente.add(historialOpc);
        menuEmergente.add(favoritosOpc);
        menuEmergente.add(configuracionOpc);
        menuEmergente.setPreferredSize(new Dimension(200, 150));

        Point location = SwingUtilities.convertPoint(menuButton, 0, 0, this);
        int x = location.x;
        int y = location.y + menuButton.getHeight();

        if (x + menuEmergente.getPreferredSize().width > getWidth()) {
            x = getWidth() - menuEmergente.getPreferredSize().width;
        }
        menuEmergente.show(this, x, y);

        historialOpc.addActionListener(e -> mostrarVentanaHistorial());
        favoritosOpc.addActionListener(e -> JOptionPane.showMessageDialog(null, "Favoritos"));
    }

    // crea y muestra ventana de historial de navegación
    private void mostrarVentanaHistorial() {
        LinkedList<String> historialCompleto = historial.obtenerHistorialList();

        if (historialCompleto.isEmpty()) {
            JOptionPane.showMessageDialog(null, "El historial está vacío.");
        } else {
            // JList para mostrar el historial
            DefaultListModel<String> listModel = new DefaultListModel<>();
            JList<String> historialList = new JList<>(listModel);
            JScrollPane scrollPane = new JScrollPane(historialList);
            scrollPane.setPreferredSize(new Dimension(400, 300));

            // agg. historial al JList
            for (String url : historialCompleto) {
                listModel.addElement(url);
            }

            JButton eliminarButton = UI_Utils.crearBotonConIcono("Borrar historial", "src/main/resources/icons/trash.png", 20, 20, null);
            JButton visitar = UI_Utils.crearBotonConIcono("Abrir", "src/main/resources/icons/browse.png", 20, 20, null);
            JButton cerrarButton = UI_Utils.crearBotonConIcono("Cerrar", "src/main/resources/icons/close.png", 20, 20, null);

            // accion para el boton de eliminar
            eliminarButton.addActionListener(e -> {
                if (JOptionPane.showConfirmDialog(null, "¿Estás seguro de eliminar el historial?", "Confirmación", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    cerrarVentana(cerrarButton);
                    historial.deleteHistory();
                    JOptionPane.showMessageDialog(null, "Historial eliminado.");
                }
            });

            // accion para el botón de visitar
            visitar.addActionListener(e -> {
                String urlSeleccionada = historialList.getSelectedValue();
                if (urlSeleccionada != null) {
                    cargarURL(urlSeleccionada);
                    cerrarVentana(cerrarButton);
                } else {
                    JOptionPane.showMessageDialog(null, "Por favor, selecciona una URL.");
                }
            });

            // accion para el botón de cerrar
            cerrarButton.addActionListener(e -> {
                cerrarVentana(cerrarButton);
            });

            // muestra historial y botones
            Object[] options = {eliminarButton, visitar, cerrarButton};
            JOptionPane.showOptionDialog(null, scrollPane, "Historial de Navegación",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                    options, null);
        }
    }

    // obtiene la ventana padre y la cierra
    private void cerrarVentana(Component componente) {
        Window window = SwingUtilities.getWindowAncestor(componente);
        if (window != null) {
            window.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new JFXPanel(); // Inicializacion de JavaFX
            new BrowserX(); // Creacion ventana principal
        });
    }
}