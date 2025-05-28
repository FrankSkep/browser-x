package browser;

import browser.controller.DescargaController;
import browser.controller.HistorialController;
import browser.util.Constants;
import browser.data_structure.Hashtable;
import browser.model.EntradaHistorial;
import browser.model.Favorito;
import browser.service.Impl.FavoritoServiceImpl;
import browser.service.NavegacionManager;
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
import javax.swing.table.DefaultTableModel;
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
    private final HistorialController historialController;
    private final FavoritoServiceImpl favoritoService;
    private final DescargaController descargaController;
    private WebView webView;
    private WebEngine webEngine;

    private final JTextField urlTextField;
    private final JButton retrocederBtn;
    private final JButton avanzarBtn;
    private final JButton favoritosBtn;

    // bandera para saber si la navegación fue por avanzar/retroceder
    private boolean navegacionUsuario = false;

    public BrowserX(NavegacionManager navegacionManager, HistorialController historialController,
                    FavoritoServiceImpl favoritoService, DescargaController descargaController) {
        this.navegacionManager = navegacionManager;
        this.historialController = historialController;
        this.favoritoService = favoritoService;
        this.descargaController = descargaController;

        applyUiTheme();

        // icono ventana
        ImageIcon iconoBase = new ImageIcon(Constants.ICONS_PATH + "browser-icon.png");
        Image iconoRedimensionado = iconoBase.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
        setIconImage(iconoRedimensionado);

        setTitle("BrowserX");
        setSize(1280, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel para botones de navegacion
        JPanel panelBotones = new JPanel();
        panelBotones.setLayout(new FlowLayout(FlowLayout.LEFT));

        // Creación de los botones
        retrocederBtn = UiTool.crearBotonConIcono(null, Constants.ICONS_PATH + "previous-page.png", 25, 25, 3);
        avanzarBtn = UiTool.crearBotonConIcono(null, Constants.ICONS_PATH + "next-page.png", 25, 25, 3);
        JButton inicioBtn = UiTool.crearBotonConIcono(null, Constants.ICONS_PATH + "home.png", 25, 25, 3);
        JButton refrescarBtn = UiTool.crearBotonConIcono(null, Constants.ICONS_PATH + "refresh.png", 25, 25, 3);
        panelBotones.add(retrocederBtn);
        panelBotones.add(avanzarBtn);
        panelBotones.add(inicioBtn);
        panelBotones.add(refrescarBtn);

        // Panel para los botones de visitar y menu
        JPanel panelMenu = new JPanel();
        panelMenu.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JButton visitarBtn = UiTool.crearBotonConIcono(null, Constants.ICONS_PATH + "browse.png", 25, 25, 3);
        favoritosBtn = UiTool.crearBotonConIcono(null, Constants.ICONS_PATH + "estrella.png", 25, 25, 3);
        JButton showMenuBtn = UiTool.crearBotonConIcono(null, Constants.ICONS_PATH + "menu.png", 25, 25, 3);
        panelMenu.add(visitarBtn);
        panelMenu.add(favoritosBtn);
        panelMenu.add(showMenuBtn);

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

        // Actualizar el estado de los botones de retroceder y avanzar
        actualizarEstadoBotones();

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
                        if (!ValidationUtil.isDownloadUrl(finalUrl)) {
                            navegacionManager.agregarUrlNavegacion(finalUrl);
                        }
                        if (!finalUrl.equals(Constants.GOOGLE_URL) && !finalUrl.equals("about:blank")) {
                            historialController.agregarElemento(new EntradaHistorial(finalUrl, ValidationUtil.dateFormat(LocalDateTime.now())));
                        }
                    }
                    navegacionUsuario = false;
                    actualizarCampoUrl();
                    actualizarEstadoBotones();
                } else if (newState == Worker.State.FAILED) {
                    urlTextField.setText("");
                    JOptionPane.showMessageDialog(this, "No se pudo cargar la página, verifica la URL.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            // Listener para descargar archivos
            webEngine.locationProperty().addListener((observable, oldValue, newValue) -> {
                if (oldValue != null && !oldValue.equals(newValue)) {
                    if (ValidationUtil.isValidFile(newValue) ||
                            ValidationUtil.isValidMimeType(ValidationUtil.getContentType(newValue))) {
                        descargaController.descargarArchivo(newValue, this);
                    }
                }
            });

            // carga pagina inicial (Google)
            cargarURL(Constants.GOOGLE_URL);
        });

        // Listener para el campo de texto de la URL
        urlTextField.addFocusListener(new java.awt.event.FocusAdapter() {

            // Eliminar placeholder al obtener el foco
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (urlTextField.getText().equals(Constants.PLACEHOLDER_URL)) {
                    urlTextField.setText("");
                    urlTextField.setForeground(Color.BLACK);
                } else { // seleccionar todo el texto al obtener el foco
                    urlTextField.selectAll();
                }
            }

            // Mostrar placeholder si el campo está vacío
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (urlTextField.getText().isEmpty()) {
                    urlTextField.setText(Constants.PLACEHOLDER_URL);
                    urlTextField.setForeground(Color.GRAY);
                }
            }
        });

        // Listener para visitar la URL al presionar Enter
        urlTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    visitarPagina();
                }
            }
        });

        // Listeners para los botones
        retrocederBtn.addActionListener(e -> retrocederPagina());
        avanzarBtn.addActionListener(e -> avanzarPagina());
        inicioBtn.addActionListener(e -> cargarURL(Constants.GOOGLE_URL));
        refrescarBtn.addActionListener(e -> refrescarPagina());
        visitarBtn.addActionListener(e -> visitarPagina());
        favoritosBtn.addActionListener(e -> agregarFavorito());
        showMenuBtn.addActionListener(e -> mostrarMenuEmergente(showMenuBtn));

        retrocederBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getButton() == MouseEvent.BUTTON3 && retrocederBtn.isEnabled()) {
                    System.out.println("Botón de retroceder clic derecho presionado");
                    mostrarContenidoPila(navegacionManager.obtenerPilaAtrasList(), true);
                }
            }
        });

        avanzarBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getButton() == MouseEvent.BUTTON3 && avanzarBtn.isEnabled()) {
                    System.out.println("Botón de avanzar clic derecho presionado");
                    mostrarContenidoPila(navegacionManager.obtenerPilaAdelanteList(), false);
                }
            }
        });


        setVisible(true);
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
    private void mostrarContenidoPila(List<String> pila, boolean esRetroceso) {
        JPopupMenu menuPila = new JPopupMenu();
        for (String url : pila) {
            JMenuItem item = new JMenuItem(url);
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

    // carga una URL en el WebEngine
    private void cargarURL(String url) {
        Platform.runLater(() -> webEngine.load(url));
    }

    // refrescar la página actual
    private void refrescarPagina() {
        Platform.runLater(() -> webEngine.reload());
    }

    // visitar una nueva pagina
    private void visitarPagina() {
        String url = urlTextField.getText();

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

            String finalUrl = url;
            cargarURL(finalUrl);
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese una URL.");
        }
    }

    // retroceder pagina
    private void retrocederPagina() {
        String urlAnterior = navegacionManager.retroceder();
        if (urlAnterior != null) {
            navegacionUsuario = true;
            cargarURL(urlAnterior);
        }
    }

    // avanzar pagina
    private void avanzarPagina() {
        String urlSiguiente = navegacionManager.avanzar();
        if (urlSiguiente != null) {
            navegacionUsuario = true;
            cargarURL(urlSiguiente);
        }
    }

    // actualizar la URL en el campo de texto
    private void actualizarCampoUrl() {
        String urlActual = navegacionManager.obtenerUrlActual();
        if (urlActual != null) {
            urlTextField.setForeground(Color.BLACK);
            urlTextField.setText(urlActual);
        }
    }

    // actualiza el estado de los botones
    private void actualizarEstadoBotones() {
        retrocederBtn.setEnabled(navegacionManager.puedeRetroceder());
        avanzarBtn.setEnabled(navegacionManager.puedeAvanzar());
        favoritosBtn.setEnabled(!favoritoService.existeFavorito(navegacionManager.obtenerUrlActual()));
    }

    // muestra menu de opciones
    private void mostrarMenuEmergente(JButton menuButton) {

        // opciones del menu
        JMenuItem historialOpc = new JMenuItem("Historial", UiTool.cargarIcono(Constants.ICONS_PATH + "record.png", 25, 25));
        JMenuItem favoritosOpc = new JMenuItem(Constants.TITULO_FAVORITOS, UiTool.cargarIcono(Constants.ICONS_PATH + "favoritos.png", 25, 25));
        JMenuItem descargasOpc = new JMenuItem("Descargas", UiTool.cargarIcono(Constants.ICONS_PATH + "downloads.png", 25, 25));

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
        historialOpc.addActionListener(e -> historialController.mostrarHistorial(
            this,
            this::actualizarEstadoBotones,
            navegacionManager::restablecerNavegacion
        ));
        favoritosOpc.addActionListener(e -> mostrarFavoritos());
        
        descargasOpc.addActionListener(e -> descargaController.mostrarDescargas(this));
    }

    // crea y muestra ventana de favoritos
    private void mostrarFavoritos() {
        Hashtable<String, String> favoritosMap = favoritoService.obtenerTodo();

        if (favoritosMap.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay favoritos.");
        } else {

            JTable favoritosTable = UiTool.crearTabla(Constants.TITULO_FAVORITOS, new String[]{"NOMBRE", "SITIO"},
                    favoritosMap.keySet().stream().map(key -> new Object[]{key, favoritosMap.get(key)}).toList());

            DefaultTableModel tableModel = (DefaultTableModel) favoritosTable.getModel();

            JScrollPane scrollPane = new JScrollPane(favoritosTable);
            scrollPane.setPreferredSize(new Dimension(500, 300));

            // Ajustar el ancho de las columnas
            favoritosTable.getColumnModel().getColumn(0).setPreferredWidth(150);
            favoritosTable.getColumnModel().getColumn(1).setPreferredWidth(350);

            JButton eliminarTodoBtn = UiTool.crearBotonConIcono("Eliminar todos", Constants.ICONS_PATH + "trash.png", 20, 20, null);
            JButton eliminarBtn = UiTool.crearBotonConIcono("Eliminar", Constants.ICONS_PATH + "eliminar-uno.png", 20, 20, null);
            JButton visitarBtn = UiTool.crearBotonConIcono("Abrir", Constants.ICONS_PATH + "browse.png", 20, 20, null);
            JButton cerrarBtn = UiTool.crearBotonConIcono("Cerrar", Constants.ICONS_PATH + "close.png", 20, 20, null);

            eliminarTodoBtn.addActionListener(e -> {
                if (JOptionPane.showConfirmDialog(null, "¿Estás seguro de eliminar todos los favoritos?", "Confirmación", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    UiTool.cerrarVentana(cerrarBtn);
                    favoritoService.eliminarTodo();
                    actualizarEstadoBotones();
                    JOptionPane.showMessageDialog(null, "Favoritos eliminados.");
                }
            });

            eliminarBtn.addActionListener(e -> {
                int selectedRow = favoritosTable.getSelectedRow();
                if (selectedRow != -1) {
                    if (JOptionPane.showConfirmDialog(null, "¿Estás seguro de eliminar todos los favoritos?", "Confirmación", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        String nombreFavorito = (String) tableModel.getValueAt(selectedRow, 0);
                        String urlFavorito = (String) tableModel.getValueAt(selectedRow, 1);
                        favoritoService.eliminarElemento(new Favorito(nombreFavorito, urlFavorito));
                        tableModel.removeRow(selectedRow);
                        actualizarEstadoBotones();
                        JOptionPane.showMessageDialog(null, "Favorito eliminado.");
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Por favor, selecciona un favorito.");
                }
            });

            visitarBtn.addActionListener(e -> {
                int selectedRow = favoritosTable.getSelectedRow();
                if (selectedRow != -1) {
                    String urlFavorito = (String) tableModel.getValueAt(selectedRow, 1);
                    cargarURL(urlFavorito);
                    UiTool.cerrarVentana(cerrarBtn);
                } else {
                    mostrarDialogoError("Por favor, selecciona un favorito.");
                }
            });

            cerrarBtn.addActionListener(e -> UiTool.cerrarVentana(cerrarBtn));

            // Mostrar favoritos y botones
            Object[] options = {eliminarTodoBtn, eliminarBtn, visitarBtn, cerrarBtn};
            JOptionPane.showOptionDialog(null, scrollPane, Constants.TITULO_FAVORITOS,
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                    options, null);
        }
    }

    // agrega un favorito
    private void agregarFavorito() {
        String url = urlTextField.getText();
        if (url.equals(Constants.PLACEHOLDER_URL) || url.isEmpty()) {
            mostrarDialogoInformacion("No hay URL para agregar a favoritos.");
        } else {
            String nombre = JOptionPane.showInputDialog(this, "Ingresa un nombre para el favorito:");
            if (nombre != null) {
                if (!nombre.isBlank()) {
                    if (favoritoService.existeFavorito(url)) {
                        mostrarDialogoInformacion("La URL ya está en favoritos.");
                    } else {
                        favoritoService.agregarElemento(new Favorito(nombre, url));
                        mostrarDialogoExito("Favorito agregado.");
                    }
                } else {
                    mostrarDialogoInformacion("Por favor, ingresa un nombre válido.");
                }
            }
            actualizarEstadoBotones();
        }
    }

    private void mostrarDialogoExito(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarDialogoError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void mostrarDialogoInformacion(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }
}
