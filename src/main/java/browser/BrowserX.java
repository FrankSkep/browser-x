package browser;

import browser.controller.DescargaUIController;
import browser.controller.FavoritoUIController;
import browser.controller.HistorialUIController;
import browser.model.EntradaHistorial;
import browser.service.NavegacionManager;
import browser.util.Constants;
import browser.util.UiTool;
import browser.util.ValidationUtil;
import com.formdev.flatlaf.FlatLightLaf;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.util.List;

/**
 * La clase BrowserX representa un navegador web.
 * Extiende JFrame para crear una interfaz gráfica de usuario.
 */
public class BrowserX extends JFrame {
    private final NavegacionManager navegacionManager;
    private final HistorialUIController historialUIController;
    // private final FavoritoServiceImpl favoritoService;
    private final FavoritoUIController favoritoUIController;
    private final DescargaUIController descargaUIController;
    private WebView webView;
    private WebEngine webEngine;

    private JTextField urlTextField;
    private JButton retrocederBtn;
    private JButton avanzarBtn;
    private JButton favoritosBtn;

    // bandera para saber si la navegación fue por avanzar/retroceder
    private boolean navegacionUsuario = false;

    public BrowserX(NavegacionManager navegacionManager, HistorialUIController historialUIController,
                    FavoritoUIController favoritoUIController, DescargaUIController descargaUIController) {
        this.navegacionManager = navegacionManager;
        this.historialUIController = historialUIController;
        this.favoritoUIController = favoritoUIController;
        this.descargaUIController = descargaUIController;

        applyUiTheme();
        setWindowProperties();
        JPanel panelSuperior = crearPanelSuperior();
        add(panelSuperior, BorderLayout.NORTH);

        JFXPanel fxPanel = new JFXPanel();
        add(fxPanel, BorderLayout.CENTER);

        actualizarEstadoBotones();
        inicializarJavaFX(fxPanel);
        inicializarListeners();
        setVisible(true);
    }

    private void setWindowProperties() {
        ImageIcon iconoBase = new ImageIcon(Constants.ICONS_PATH + "browser-icon.png");
        Image iconoRedimensionado = iconoBase.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
        setIconImage(iconoRedimensionado);
        setTitle("BrowserX");
        setSize(1280, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    private JPanel crearPanelSuperior() {
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        retrocederBtn = UiTool.crearBotonConIcono(null, Constants.ICONS_PATH + "previous-page.png", 25, 25, 3);
        avanzarBtn = UiTool.crearBotonConIcono(null, Constants.ICONS_PATH + "next-page.png", 25, 25, 3);
        JButton inicioBtn = UiTool.crearBotonConIcono(null, Constants.ICONS_PATH + "home.png", 25, 25, 3);
        JButton refrescarBtn = UiTool.crearBotonConIcono(null, Constants.ICONS_PATH + "refresh.png", 25, 25, 3);
        panelBotones.add(retrocederBtn);
        panelBotones.add(avanzarBtn);
        panelBotones.add(inicioBtn);
        panelBotones.add(refrescarBtn);

        JPanel panelMenu = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton visitarBtn = UiTool.crearBotonConIcono(null, Constants.ICONS_PATH + "browse.png", 25, 25, 3);
        favoritosBtn = UiTool.crearBotonConIcono(null, Constants.ICONS_PATH + "estrella.png", 25, 25, 3);
        JButton showMenuBtn = UiTool.crearBotonConIcono(null, Constants.ICONS_PATH + "menu.png", 25, 25, 3);
        panelMenu.add(visitarBtn);
        panelMenu.add(favoritosBtn);
        panelMenu.add(showMenuBtn);

        JPanel panelURL = new JPanel(new BorderLayout());
        urlTextField = new JTextField();
        panelURL.add(urlTextField, BorderLayout.CENTER);
        panelURL.add(panelMenu, BorderLayout.EAST);

        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.add(panelBotones, BorderLayout.WEST);
        panelSuperior.add(panelURL, BorderLayout.CENTER);

        // Guardar referencias para listeners
        this.inicioBtn = inicioBtn;
        this.refrescarBtn = refrescarBtn;
        this.visitarBtn = visitarBtn;
        this.showMenuBtn = showMenuBtn;

        return panelSuperior;
    }

    // Declarar estos botones como atributos privados si se usan en listeners
    private JButton inicioBtn;
    private JButton refrescarBtn;
    private JButton visitarBtn;
    private JButton showMenuBtn;

    private void inicializarJavaFX(JFXPanel fxPanel) {
        Platform.runLater(() -> {
            webView = new WebView();
            webEngine = webView.getEngine();
            fxPanel.setScene(new Scene(webView));
            inicializarWebEngineListeners();
            cargarURL(Constants.GOOGLE_URL);
        });
    }

    private void inicializarWebEngineListeners() {
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
            if (newState == Worker.State.RUNNING) {
                String finalUrl = webEngine.getLocation();
                if (!navegacionUsuario) {
                    if (!ValidationUtil.isDownloadUrl(finalUrl)) {
                        navegacionManager.agregarUrlNavegacion(finalUrl);
                    }
                    if (!finalUrl.equals(Constants.GOOGLE_URL) && !finalUrl.equals("about:blank")) {
                        historialUIController.agregarElemento(
                                new EntradaHistorial(finalUrl, ValidationUtil.dateFormat(LocalDateTime.now())));
                    }
                }
                navegacionUsuario = false;
                actualizarCampoUrl();
                actualizarEstadoBotones();
            } else if (newState == Worker.State.FAILED) {
                urlTextField.setText("");
                JOptionPane.showMessageDialog(this, "No se pudo cargar la página, verifica la URL.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        webEngine.locationProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != null && !oldValue.equals(newValue)) {
                if (ValidationUtil.isValidFile(newValue) ||
                        ValidationUtil.isValidMimeType(ValidationUtil.getContentType(newValue))) {
                    descargaUIController.descargarArchivo(newValue, this);
                }
            }
        });
    }

    private void inicializarListeners() {
        urlTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (urlTextField.getText().equals(Constants.PLACEHOLDER_URL)) {
                    urlTextField.setText("");
                    urlTextField.setForeground(Color.BLACK);
                } else {
                    urlTextField.selectAll();
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (urlTextField.getText().isEmpty()) {
                    urlTextField.setText(Constants.PLACEHOLDER_URL);
                    urlTextField.setForeground(Color.GRAY);
                }
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

        retrocederBtn.addActionListener(e -> retrocederPagina());
        avanzarBtn.addActionListener(e -> avanzarPagina());
        inicioBtn.addActionListener(e -> cargarURL(Constants.GOOGLE_URL));
        refrescarBtn.addActionListener(e -> refrescarPagina());
        visitarBtn.addActionListener(e -> visitarPagina());
        favoritosBtn.addActionListener(e -> favoritoUIController.agregarFavorito(
                this,
                urlTextField.getText(),
                this::actualizarEstadoBotones));
        showMenuBtn.addActionListener(e -> mostrarMenuEmergente(showMenuBtn));

        retrocederBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getButton() == MouseEvent.BUTTON3 && retrocederBtn.isEnabled()) {
                    mostrarMenuNavegacion(navegacionManager.obtenerPilaAtrasList(), true);
                }
            }
        });

        avanzarBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getButton() == MouseEvent.BUTTON3 && avanzarBtn.isEnabled()) {
                    mostrarMenuNavegacion(navegacionManager.obtenerPilaAdelanteList(), false);
                }
            }
        });
    }

    private static void applyUiTheme() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (
                Exception e) {
            System.err.println("Ocurrio un error: " + e.getMessage());
        }
    }

    // muestra el contenido de la pila en un menú emergente
    private void mostrarMenuNavegacion(List<String> pila, boolean esRetroceso) {
        JPopupMenu menuPila = new JPopupMenu();
        for (String url : pila) {
            String titulo = navegacionManager.obtenerTituloPorUrl(url, () -> {
                // Refrescar el menú cuando el título esté disponible
                SwingUtilities.invokeLater(() -> mostrarMenuNavegacion(pila, esRetroceso));
            });
            if (titulo == null || titulo.isBlank()) {
                titulo = url.length() > 30 ? url.substring(0, 30) + "..." : url;
            }
            JMenuItem item = new JMenuItem(titulo);
            item.setToolTipText(url);
            item.addActionListener(e -> {
                if (esRetroceso) {
                    navegacionManager.irAtrasHasta(url);
                } else {
                    navegacionManager.irAdelanteHasta(url);
                }
                cargarURL(url);
            });
            menuPila.add(item);
        }
        menuPila.show(this, retrocederBtn.getX(), retrocederBtn.getY() + retrocederBtn.getHeight());
    }

    private void cargarURL(String url) {
        Platform.runLater(() -> webEngine.load(url));
    }

    private void refrescarPagina() {
        Platform.runLater(() -> webEngine.reload());
    }

    private String formatearUrl(String url) {
        if (url.equals(Constants.PLACEHOLDER_URL)) {
            url = "";
        }
        if (!url.isBlank()) {
            if (ValidationUtil.isValidUrl(url)) {
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "http://" + url;
                }
            } else {
                url = Constants.GOOGLE_URL + "/search?q=" + url.replace(" ", "+");
            }
        }
        return url;
    }

    private void visitarPagina() {
        String url = formatearUrl(urlTextField.getText());
        if (!url.isBlank()) {
            cargarURL(url);
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese una URL.");
        }
    }

    private void retrocederPagina() {
        String urlAnterior = navegacionManager.retroceder();
        if (urlAnterior != null) {
            navegacionUsuario = true;
            cargarURL(urlAnterior);
        }
    }

    private void avanzarPagina() {
        String urlSiguiente = navegacionManager.avanzar();
        if (urlSiguiente != null) {
            navegacionUsuario = true;
            cargarURL(urlSiguiente);
        }
    }

    private void actualizarCampoUrl() {
        String urlActual = navegacionManager.obtenerUrlActual();
        if (urlActual != null) {
            urlTextField.setForeground(Color.BLACK);
            urlTextField.setText(urlActual);
        }
    }

    private void actualizarEstadoBotones() {
        retrocederBtn.setEnabled(navegacionManager.puedeRetroceder());
        avanzarBtn.setEnabled(navegacionManager.puedeAvanzar());
        favoritosBtn.setEnabled(!favoritoUIController.existeFavorito(navegacionManager.obtenerUrlActual()));
    }

    // muestra menu de opciones
    private void mostrarMenuEmergente(JButton menuButton) {

        // opciones del menu
        JMenuItem historialOpc = new JMenuItem("Historial",
                UiTool.cargarIcono(Constants.ICONS_PATH + "record.png", 25, 25));
        JMenuItem favoritosOpc = new JMenuItem(Constants.TITULO_FAVORITOS,
                UiTool.cargarIcono(Constants.ICONS_PATH + "favoritos.png", 25, 25));
        JMenuItem descargasOpc = new JMenuItem("Descargas",
                UiTool.cargarIcono(Constants.ICONS_PATH + "downloads.png", 25, 25));

        // espacio entre icono y texto
        historialOpc.setIconTextGap(20);
        favoritosOpc.setIconTextGap(20);
        descargasOpc.setIconTextGap(20);

        // fuente para las opciones del menu
        Font font = new Font("Arial", Font.PLAIN, 16);
        historialOpc.setFont(font);
        favoritosOpc.setFont(font);
        descargasOpc.setFont(font);

        // menu emergente
        JPopupMenu menuEmergente = new JPopupMenu();
        menuEmergente.add(historialOpc);
        menuEmergente.add(favoritosOpc);
        menuEmergente.add(descargasOpc);
        menuEmergente.setPreferredSize(new Dimension(200, 150));

        // mostrar menu en la posición correcta
        Point location = SwingUtilities.convertPoint(menuButton, 0, 0, this);
        int x = location.x;
        int y = location.y + menuButton.getHeight();

        if (x + menuEmergente.getPreferredSize().width > getWidth()) {
            x = getWidth() - menuEmergente.getPreferredSize().width;
        }
        menuEmergente.show(this, x, y);

        // listeners para las opciones del menu
        historialOpc.addActionListener(e -> historialUIController.mostrarHistorial(
                this,
                this::actualizarEstadoBotones,
                navegacionManager::restablecerNavegacion,
                this::cargarURL));
        favoritosOpc.addActionListener(e -> favoritoUIController.mostrarFavoritos(
                this,
                this::actualizarEstadoBotones,
                this::cargarURL));

        descargasOpc.addActionListener(e -> descargaUIController.mostrarDescargas(this));
    }
}
