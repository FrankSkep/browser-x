package browser;

import browser.data_structure.Hashtable;
import browser.database.Db_Connection;
import browser.model.Descarga;
import browser.model.EntradaHistorial;
import browser.model.Favorito;
import browser.service.DescargaService;
import browser.service.FavoritoService;
import browser.util.UiTool;
import browser.util.ValidationUtil;
import browser.data_structure.LinkedList;
import browser.service.NavegacionService;
import com.formdev.flatlaf.FlatLightLaf;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class BrowserX extends JFrame {
    private final NavegacionService navegacionService;
    private final FavoritoService favoritoService;
    private final DescargaService descargaService;
    private WebView webView;
    private WebEngine webEngine;

    private final JTextField urlTextField;
    private final JButton retrocederBtn;
    private final JButton avanzarBtn;
    private final JButton favoritosBtn;

    private final String GOOGLE_URL = "https://www.google.com/";
    private final String ICONS_PATH = "src/main/resources/icons/";

    // bandera para saber si la navegación fue por avanzar/retroceder
    private boolean navegacionUsuario = false;

    public BrowserX() {
        Db_Connection.initializeDatabase();
        navegacionService = new NavegacionService();
        favoritoService = new FavoritoService();
        descargaService = new DescargaService();

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            // UIManager.getSystemLookAndFeelClassName() // Establece el aspecto del sistema
            // UIManager.setLookAndFeel(new NimbusLookAndFeel()); // Establece el aspecto Nimbus
        } catch (
                Exception e) {
            System.err.println("Ocurrio un error: " + e.getMessage());
        }
        // icono ventana
        ImageIcon iconoBase = new ImageIcon(ICONS_PATH + "browser-icon.png");
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
        retrocederBtn = UiTool.crearBotonConIcono(null, ICONS_PATH + "previous-page.png", 25, 25, 3);
        avanzarBtn = UiTool.crearBotonConIcono(null, ICONS_PATH + "next-page.png", 25, 25, 3);
        retrocederBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getButton() == MouseEvent.BUTTON3 && retrocederBtn.isEnabled()) {
                    System.out.println("Botón de retroceder clic derecho presionado");
                    mostrarContenidoPila(navegacionService.obtenerPilaAtras(), true);
                }
            }
        });
        
        avanzarBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getButton() == MouseEvent.BUTTON3 && avanzarBtn.isEnabled()) {
                    System.out.println("Botón de avanzar clic derecho presionado");
                    mostrarContenidoPila(navegacionService.obtenerPilaAdelante(), false);
                }
            }
        });

        JButton inicioBtn = UiTool.crearBotonConIcono(null, ICONS_PATH + "home.png", 25, 25, 3);
        JButton refrescarBtn = UiTool.crearBotonConIcono(null, ICONS_PATH + "refresh.png", 25, 25, 3);
        panelBotones.add(retrocederBtn);
        panelBotones.add(avanzarBtn);
        panelBotones.add(inicioBtn);
        panelBotones.add(refrescarBtn);

        // Panel para los botones de visitar y menu
        JPanel panelMenu = new JPanel();
        panelMenu.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JButton visitarBtn = UiTool.crearBotonConIcono(null, ICONS_PATH + "browse.png", 25, 25, 3);
        favoritosBtn = UiTool.crearBotonConIcono(null, ICONS_PATH + "estrella.png", 25, 25, 3);
        JButton showMenuBtn = UiTool.crearBotonConIcono(null, ICONS_PATH + "menu.png", 25, 25, 3);
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
                            navegacionService.agregarUrlNavegacion(finalUrl);
                        }
                        if (!finalUrl.equals(GOOGLE_URL) && !finalUrl.equals("about:blank")) {
                            navegacionService.guardarEnHistorial(finalUrl);
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
                if (ValidationUtil.isValidFile(newValue) ||
                        ValidationUtil.isValidMimeType(ValidationUtil.getContentType(newValue))) {
                    descargaService.descargarArchivo(newValue, this);
                }
            });

            // carga pagina inicial (Google)
            cargarURL(GOOGLE_URL);
        });

        // Listeners para los botones
        retrocederBtn.addActionListener(e -> retrocederPagina());
        avanzarBtn.addActionListener(e -> avanzarPagina());
        inicioBtn.addActionListener(e -> cargarURL(GOOGLE_URL));
        refrescarBtn.addActionListener(e -> refrescarPagina());
        visitarBtn.addActionListener(e -> visitarPagina());
        favoritosBtn.addActionListener(e -> agregarFavorito());
        showMenuBtn.addActionListener(e -> mostrarMenuEmergente(showMenuBtn));

        // Listener para el campo de texto de la URL
        urlTextField.addFocusListener(new java.awt.event.FocusAdapter() {

            // Eliminar placeholder al obtener el foco
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (urlTextField.getText().equals("Ingrese una URL")) {
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
                    urlTextField.setText("Ingrese una URL");
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
        setVisible(true);
    }

    private void mostrarContenidoPila(List<String> pila, boolean esRetroceso) {
        JPopupMenu menuPila = new JPopupMenu();
        for (String url : pila) {
            JMenuItem item = new JMenuItem(url);
            item.addActionListener(e -> {
                if (esRetroceso) {
                    navegacionService.irAtrasHasta(url);
                } else {
                    navegacionService.irAdelanteHasta(url);
                }
                actualizarEstadoBotones();
                actualizarCampoUrl();
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

        if (url.equals("Ingrese una URL")) {
            url = "";
        }

        if (!url.isBlank()) {
            if (ValidationUtil.isValidUrl(url)) {
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "http://" + url;
                }
            } else {
                url = GOOGLE_URL + "/search?q=" + url.replace(" ", "+");
            }

            String finalUrl = url;
            cargarURL(finalUrl);
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese una URL.");
        }
    }

    // retroceder pagina
    private void retrocederPagina() {
        String urlAnterior = navegacionService.retroceder();
        if (urlAnterior != null) {
            navegacionUsuario = true;
            cargarURL(urlAnterior);
        }
    }

    // avanzar pagina
    private void avanzarPagina() {
        String urlSiguiente = navegacionService.avanzar();
        if (urlSiguiente != null) {
            navegacionUsuario = true;
            cargarURL(urlSiguiente);
        }
    }

    // actualizar la URL en el campo de texto
    private void actualizarCampoUrl() {
        String urlActual = navegacionService.obtenerURLActual();
        if (urlActual != null) {
            urlTextField.setForeground(Color.BLACK);
            urlTextField.setText(urlActual);
        }
    }

    // actualiza el estado de los botones
    private void actualizarEstadoBotones() {
        retrocederBtn.setEnabled(navegacionService.puedeRetroceder());
        avanzarBtn.setEnabled(navegacionService.puedeAvanzar());
        favoritosBtn.setEnabled(!favoritoService.existeFavorito(navegacionService.obtenerURLActual()));
    }


    // muestra menu de opciones
    private void mostrarMenuEmergente(JButton menuButton) {

        // opciones del menu
        JMenuItem historialOpc = new JMenuItem("Historial", UiTool.cargarIcono(ICONS_PATH + "record.png", 25, 25));
        JMenuItem favoritosOpc = new JMenuItem("Favoritos", UiTool.cargarIcono(ICONS_PATH + "favoritos.png", 25, 25));
        JMenuItem descargasOpc = new JMenuItem("Descargas", UiTool.cargarIcono(ICONS_PATH + "downloads.png", 25, 25));

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
        historialOpc.addActionListener(e -> mostrarHistorial());
        favoritosOpc.addActionListener(e -> mostrarFavoritos());
        descargasOpc.addActionListener(e -> mostrarDescargas());
    }

    // crea y muestra ventana de historial de navegación
    private void mostrarHistorial() {
        LinkedList<EntradaHistorial> historialCompleto = navegacionService.obtenerHistorial();

        if (historialCompleto.isEmpty()) {
            JOptionPane.showMessageDialog(null, "El historial está vacío.");
        } else {

            JTable historialTable = UiTool.crearTabla("Historial de Navegación", new String[]{"SITIO", "FECHA"}, historialCompleto.stream()
                    .map(entrada -> new Object[]{entrada.getUrl(), entrada.getFecha()}).toList());
            DefaultTableModel tableModel = (DefaultTableModel) historialTable.getModel();
            JScrollPane scrollPane = new JScrollPane(historialTable);

            scrollPane.setPreferredSize(new Dimension(500, 300));
            historialTable.getColumnModel().getColumn(0).setPreferredWidth(350);

            JButton eliminarTodoBtn = UiTool.crearBotonConIcono("Eliminar todo", ICONS_PATH + "trash.png", 20, 20, null);
            JButton eliminarBtn = UiTool.crearBotonConIcono("Eliminar", ICONS_PATH + "eliminar-uno.png", 20, 20, null);
            JButton visitarBtn = UiTool.crearBotonConIcono("Abrir", ICONS_PATH + "browse.png", 20, 20, null);
            JButton cerrarBtn = UiTool.crearBotonConIcono("Cerrar", ICONS_PATH + "close.png", 20, 20, null);

            eliminarTodoBtn.addActionListener(e -> {
                if (JOptionPane.showConfirmDialog(null, "¿Estás seguro de eliminar todo el historial?", "Confirmación", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    UiTool.cerrarVentana(cerrarBtn);
                    navegacionService.eliminarHistorial();
                    actualizarEstadoBotones();
                    JOptionPane.showMessageDialog(null, "Historial eliminado.");
                }
            });

            eliminarBtn.addActionListener(e -> {
                int selectedRow = historialTable.getSelectedRow();
                if (selectedRow != -1) {
                    EntradaHistorial entradaSeleccionada = new EntradaHistorial((String) tableModel.getValueAt(selectedRow, 0),
                            (String) tableModel.getValueAt(selectedRow, 1));

                    navegacionService.eliminarEntradaHistorial(entradaSeleccionada);
                    tableModel.removeRow(selectedRow);
                    JOptionPane.showMessageDialog(null, "Entrada de historial eliminada.");
                } else {
                    JOptionPane.showMessageDialog(null, "Por favor, selecciona una entrada.");
                }
            });

            visitarBtn.addActionListener(e -> {
                int selectedRow = historialTable.getSelectedRow();
                if (selectedRow != -1) {
                    String urlSeleccionada = (String) tableModel.getValueAt(selectedRow, 0);
                    cargarURL(urlSeleccionada);
                    UiTool.cerrarVentana(cerrarBtn);
                } else {
                    JOptionPane.showMessageDialog(null, "Por favor, selecciona una URL.");
                }
            });

            cerrarBtn.addActionListener(e -> UiTool.cerrarVentana(cerrarBtn));

            // Mostrar historial y botones
            Object[] options = {eliminarTodoBtn, eliminarBtn, visitarBtn, cerrarBtn};
            JOptionPane.showOptionDialog(null, scrollPane, "Historial de Navegación",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                    options, null);
        }
    }

    // crea y muestra ventana de favoritos
    private void mostrarFavoritos() {
        Hashtable<String, String> favoritosMap = favoritoService.obtenerTodo();

        if (favoritosMap.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay favoritos.");
        } else {

            JTable favoritosTable = UiTool.crearTabla("Favoritos", new String[]{"NOMBRE", "SITIO"},
                    favoritosMap.keySet().stream().map(key -> new Object[]{key, favoritosMap.get(key)}).toList());

            DefaultTableModel tableModel = (DefaultTableModel) favoritosTable.getModel();

            JScrollPane scrollPane = new JScrollPane(favoritosTable);
            scrollPane.setPreferredSize(new Dimension(500, 300));

            // Ajustar el ancho de las columnas
            favoritosTable.getColumnModel().getColumn(0).setPreferredWidth(150);
            favoritosTable.getColumnModel().getColumn(1).setPreferredWidth(350);

            JButton eliminarTodoBtn = UiTool.crearBotonConIcono("Eliminar todos", ICONS_PATH + "trash.png", 20, 20, null);
            JButton eliminarBtn = UiTool.crearBotonConIcono("Eliminar", ICONS_PATH + "eliminar-uno.png", 20, 20, null);
            JButton visitarBtn = UiTool.crearBotonConIcono("Abrir", ICONS_PATH + "browse.png", 20, 20, null);
            JButton cerrarBtn = UiTool.crearBotonConIcono("Cerrar", ICONS_PATH + "close.png", 20, 20, null);

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
                        favoritoService.eliminarFavorito(nombreFavorito);
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
                    JOptionPane.showMessageDialog(null, "Por favor, selecciona un favorito.");
                }
            });

            cerrarBtn.addActionListener(e -> UiTool.cerrarVentana(cerrarBtn));

            // Mostrar favoritos y botones
            Object[] options = {eliminarTodoBtn, eliminarBtn, visitarBtn, cerrarBtn};
            JOptionPane.showOptionDialog(null, scrollPane, "Favoritos",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                    options, null);
        }
    }

    // crea y muestra ventana de historial de navegación
    private void mostrarDescargas() {
        LinkedList<Descarga> historialDescargas = descargaService.obtenerTodo();

        if (historialDescargas.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay descargas.");
        } else {
            JTable descargasTable = UiTool.crearTabla("Historial de Descargas", new String[]{"NOMBRE", "SITIO", "FECHA"},
                    historialDescargas.stream()
                            .map(descarga -> new Object[]{descarga.getNombre(), descarga.getUrl(), descarga.getFecha()}).toList());

            DefaultTableModel tableModel = (DefaultTableModel) descargasTable.getModel();

            JScrollPane scrollPane = new JScrollPane(descargasTable);
            scrollPane.setPreferredSize(new Dimension(500, 300));

            // Ajustar el ancho de las columnas
            descargasTable.getColumnModel().getColumn(0).setPreferredWidth(200);
            descargasTable.getColumnModel().getColumn(1).setPreferredWidth(150);
            descargasTable.getColumnModel().getColumn(2).setPreferredWidth(110);

            JButton eliminarTodoBtn = UiTool.crearBotonConIcono("Eliminar todas", ICONS_PATH + "trash.png", 20, 20, null);
            JButton eliminarBtn = UiTool.crearBotonConIcono("Eliminar", ICONS_PATH + "eliminar-uno.png", 20, 20, null);
            JButton cerrarBtn = UiTool.crearBotonConIcono("Cerrar", ICONS_PATH + "close.png", 20, 20, null);

            eliminarTodoBtn.addActionListener(e -> {
                if (JOptionPane.showConfirmDialog(null, "¿Estás seguro de eliminar todas las descargas?", "Confirmación", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    UiTool.cerrarVentana(cerrarBtn);
                    descargaService.eliminarTodo();
                    JOptionPane.showMessageDialog(null, "Descargas eliminadas.");
                }
            });

            eliminarBtn.addActionListener(e -> {
                int selectedRow = descargasTable.getSelectedRow();
                if (selectedRow != -1) {
                    String nombre = (String) tableModel.getValueAt(selectedRow, 0);
                    String url = (String) tableModel.getValueAt(selectedRow, 1);
                    String fecha = (String) tableModel.getValueAt(selectedRow, 2);
                    descargaService.eliminarDescarga(new Descarga(nombre, url, fecha));
                    tableModel.removeRow(selectedRow);
                    JOptionPane.showMessageDialog(null, "Descarga eliminada.");
                } else {
                    JOptionPane.showMessageDialog(null, "Por favor, selecciona una descarga.");
                }
            });

            cerrarBtn.addActionListener(e -> UiTool.cerrarVentana(cerrarBtn));

            // Mostrar descargas y botones
            Object[] options = {eliminarTodoBtn, eliminarBtn, cerrarBtn};
            JOptionPane.showOptionDialog(null, scrollPane, "Historial de Descargas",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                    options, null);
        }
    }

    // agrega un favorito
    private void agregarFavorito() {
        String url = urlTextField.getText();
        if (url.equals("Ingrese una URL") || url.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay URL para agregar a favoritos.");
        } else {
            String nombre = JOptionPane.showInputDialog(this, "Ingresa un nombre para el favorito:");
            if (nombre != null) {
                if (!nombre.isBlank()) {
                    if (favoritoService.existeFavorito(url)) {
                        JOptionPane.showMessageDialog(this, "La URL ya está en favoritos.");
                    } else {
                        favoritoService.agregarFavorito(new Favorito(nombre, url));
                        JOptionPane.showMessageDialog(this, "Favorito agregado.");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Por favor, ingresa un nombre válido.");
                }
            }
            actualizarEstadoBotones();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new JFXPanel(); // Inicializacion JavaFX
            new BrowserX(); // Creacion ventana principal
        });

        // shutdown hook para asegurar que todas las tareas de JavaFX se completen antes de cerrar
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Platform.runLater(Platform::exit); // cerrar app JavaFX
        }));
    }
}
