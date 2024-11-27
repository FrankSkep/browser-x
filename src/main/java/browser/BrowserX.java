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
import browser.util.DownloadProgressDialog;
import browser.data_structure.LinkedList;
import browser.service.NavegacionService;
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
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

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
                        navegacionService.agregarUrlNavegacion(finalUrl);

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
                        ValidationUtil.esTipoDescargable(ValidationUtil.getContentType(newValue))) {
                    descargarArchivo(newValue);
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

    // Método para descargar el archivo
    private void descargarArchivo(String fileUrl) {
        DownloadProgressDialog progressDialog = new DownloadProgressDialog(this);

        // Mostrar el diálogo en un hilo separado
        SwingUtilities.invokeLater(() -> progressDialog.setVisible(true));

        new Thread(() -> {
            try {
                URL url = new URL(fileUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    String fileName = null;
                    String contentDisposition = connection.getHeaderField("Content-Disposition");
                    if (contentDisposition != null && contentDisposition.contains("filename=")) {
                        fileName = contentDisposition.split("filename=")[1].replace("\"", "");
                    }
                    if (fileName == null || fileName.isBlank()) {
                        fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
                    }
                    if (fileName.isBlank()) {
                        fileName = "archivo_descargado";
                    }

                    Path downloadPath = Paths.get(ValidationUtil.getDownloadFolder(), fileName);
                    int fileSize = connection.getContentLength();
                    try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                         FileOutputStream fileOut = new FileOutputStream(downloadPath.toString())) {

                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        int totalBytesRead = 0;

                        while ((bytesRead = in.read(buffer, 0, 1024)) != -1) {
                            fileOut.write(buffer, 0, bytesRead);
                            totalBytesRead += bytesRead;
                            int progress = (int) ((totalBytesRead / (double) fileSize) * 100);
                            SwingUtilities.invokeLater(() -> progressDialog.updateProgress(progress));
                        }

                        Platform.runLater(() -> webEngine.getHistory().go(-1));
                        JOptionPane.showMessageDialog(this, "Archivo descargado: " + fileName);
                        descargaService.guardarDescarga(new Descarga(fileName, fileUrl, ValidationUtil.dateFormat(LocalDateTime.now())));
                    }
                } else {
                    System.err.println("Error al descargar el archivo: " + connection.getResponseMessage());
                }
            } catch (
                    Exception e) {
                System.out.println("Error al descargar el archivo: " + e.getMessage());
            } finally {
                SwingUtilities.invokeLater(progressDialog::dispose);
            }
        }).start();
    }

    // carga una URL en el WebEngine
    private void cargarURL(String url) {
        Platform.runLater(() -> webEngine.load(url));
    }

    // refrescar la página actual
    private void refrescarPagina() {
        Platform.runLater(() -> webEngine.reload());
    }

    // visitar una pagina nueva
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

    // retroceder en el historial
    private void retrocederPagina() {
        String urlAnterior = navegacionService.retroceder();
        if (urlAnterior != null) {
            navegacionUsuario = true;
            cargarURL(urlAnterior);
        }
    }

    // avanzar en el historial
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

    // muestra menu de opciones
    private void mostrarMenuEmergente(JButton menuButton) {
        // iconos para las opciones del menu
        ImageIcon historialIcon = UiTool.cargarIcono(ICONS_PATH + "record.png", 25, 25);
        ImageIcon favoritosIcon = UiTool.cargarIcono(ICONS_PATH + "favoritos.png", 25, 25);
        ImageIcon descargasIcon = UiTool.cargarIcono(ICONS_PATH + "downloads.png", 25, 25);

        // opciones del menu
        JMenuItem historialOpc = new JMenuItem("Historial", historialIcon);
        JMenuItem favoritosOpc = new JMenuItem("Favoritos", favoritosIcon);
        JMenuItem descargasOpc = new JMenuItem("Descargas", descargasIcon);

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
        historialOpc.addActionListener(e -> mostrarVentanaHistorial());
        favoritosOpc.addActionListener(e -> mostrarVentanaFavoritos());
        descargasOpc.addActionListener(e -> mostrarVentanaDescargas());
    }

    // crea y muestra ventana de historial de navegación
    private void mostrarVentanaHistorial() {
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

    // crea y muestra ventana de favoritos
    private void mostrarVentanaFavoritos() {
        Hashtable<String, String> favoritosMap = favoritoService.obtenerTodo();

        if (favoritosMap.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay favoritos.");
        } else {

            List<Object[]> datos = favoritosMap.keySet().stream()
                    .map(key -> new Object[]{key, favoritosMap.get(key)}).toList();
            JTable favoritosTable = UiTool.crearTabla("Favoritos", new String[]{"NOMBRE", "SITIO"}, datos);
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
    private void mostrarVentanaDescargas() {
        LinkedList<Descarga> historialDescargas = descargaService.obtenerTodo();

        if (historialDescargas.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay descargas.");
        } else {
            String[] columnNames = {"NOMBRE", "SITIO", "FECHA"};
            DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            JTable descargasTable = UiTool.crearTabla("Historial de Descargas", new String[]{"NOMBRE", "SITIO", "FECHA"}, historialDescargas.stream()
                    .map(descarga -> new Object[]{descarga.getNombre(), descarga.getUrl(), descarga.getFecha()}).toList());

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

    // actualiza el estado de los botones de navegación
    private void actualizarEstadoBotones() {
        retrocederBtn.setEnabled(navegacionService.puedeRetroceder());
        avanzarBtn.setEnabled(navegacionService.puedeAvanzar());
        favoritosBtn.setEnabled(!favoritoService.existeFavorito(navegacionService.obtenerURLActual()));
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
