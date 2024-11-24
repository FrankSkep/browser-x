package browser;

import browser.data_structure.Hashtable;
import browser.database.Db_Connection;
import browser.model.Descarga;
import browser.model.EntradaHistorial;
import browser.model.Favorito;
import browser.service.DescargaService;
import browser.service.FavoritoService;
import browser.utils.UiTools;
import browser.data_structure.LinkedList;
import browser.service.HistorialService;
import browser.utils.ValidationTools;
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

public class BrowserX extends JFrame {
    private final HistorialService historialService;
    private final FavoritoService favoritoService;
    private final DescargaService descargaService;
    private WebView webView;
    private WebEngine webEngine;

    private final JTextField urlTextField;
    private final JButton retrocederBtn;
    private final JButton avanzarBtn;
    private final JButton favoritosBtn;

    private final String googleUrl = "https://www.google.com/";

    // bandera para saber si la navegación fue natural o por avanzar/retroceder
    private boolean navegacionUsuario = false;

    public BrowserX() {
        Db_Connection.initializeDatabase();
        historialService = new HistorialService();
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
        ImageIcon iconoBase = new ImageIcon("src/main/resources/icons/browser-icon.png");
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
        retrocederBtn = UiTools.crearBotonConIcono(null, "src/main/resources/icons/previous-page.png", 25, 25, 3);
        avanzarBtn = UiTools.crearBotonConIcono(null, "src/main/resources/icons/next-page.png", 25, 25, 3);
        JButton inicioBtn = UiTools.crearBotonConIcono(null, "src/main/resources/icons/home.png", 25, 25, 3);
        JButton refrescarBtn = UiTools.crearBotonConIcono(null, "src/main/resources/icons/refresh.png", 25, 25, 3);
        panelBotones.add(retrocederBtn);
        panelBotones.add(avanzarBtn);
        panelBotones.add(inicioBtn);
        panelBotones.add(refrescarBtn);

        // Panel para los botones de visitar y menu
        JPanel panelMenu = new JPanel();
        panelMenu.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JButton visitarBtn = UiTools.crearBotonConIcono(null, "src/main/resources/icons/browse.png", 25, 25, 3);
        favoritosBtn = UiTools.crearBotonConIcono(null, "src/main/resources/icons/estrella.png", 25, 25, 3);
        JButton showMenuBtn = UiTools.crearBotonConIcono(null, "src/main/resources/icons/menu.png", 25, 25, 3);
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
                        historialService.agregarUrlNavegacion(finalUrl);

                        if (!finalUrl.equals(googleUrl) && !finalUrl.equals("about:blank")) {
                            historialService.agregarEntradaHistorial(finalUrl);
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
                if (ValidationTools.isValidFile(newValue) || ValidationTools.esTipoDescargable(ValidationTools.getContentType(newValue))) {
                    descargarArchivo(newValue);
                }
            });

            // carga pagina inicial (Google)
            cargarURL(googleUrl);
        });

        // Listeners para los botones
        retrocederBtn.addActionListener(e -> retrocederPagina());
        avanzarBtn.addActionListener(e -> avanzarPagina());
        inicioBtn.addActionListener(e -> cargarURL(googleUrl));
        refrescarBtn.addActionListener(e -> refrescarPagina());
        visitarBtn.addActionListener(e -> visitarPagina());
        favoritosBtn.addActionListener(e -> agregarFavorito());
        showMenuBtn.addActionListener(e -> mostrarMenuEmergente(showMenuBtn));

        // Listeners para el campo de texto
        urlTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (urlTextField.getText().equals("Ingrese una URL")) {
                    urlTextField.setText("");
                    urlTextField.setForeground(Color.BLACK);
                } else { // seleccionar todo el texto al obtener el foco
                    urlTextField.selectAll();
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

        // Listener para la tecla Enter en el campo de texto
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
        new Thread(() -> {
            try {
                URL url = new URL(fileUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    // Intentar obtener el nombre del archivo desde Content-Disposition
                    String fileName = null;
                    String contentDisposition = connection.getHeaderField("Content-Disposition");
                    if (contentDisposition != null && contentDisposition.contains("filename=")) {
                        fileName = contentDisposition.split("filename=")[1].replace("\"", "");
                    }

                    // si no se encuentra en Content-Disposition, usar el ultimo segmento de la URL
                    if (fileName == null || fileName.isBlank()) {
                        System.out.println("Se obtiene el nombre del archivo desde la URL");
                        fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
                    }

                    // si el nombre sigue vacio, se agrega nombre generico
                    if (fileName.isBlank()) {
                        fileName = "archivo_descargado";
                    }

                    Path downloadPath = Paths.get(ValidationTools.getDownloadFolder(), fileName);

                    try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                         FileOutputStream fileOut = new FileOutputStream(downloadPath.toString())) {

                        byte[] buffer = new byte[1024];
                        int bytesRead;

                        while ((bytesRead = in.read(buffer, 0, 1024)) != -1) {
                            fileOut.write(buffer, 0, bytesRead);
                        }

                        Platform.runLater(() -> webEngine.getHistory().go(-1));
                        JOptionPane.showMessageDialog(this, "Archivo descargado: " + fileName);
                        descargaService.guardarDescarga(new Descarga(fileName, fileUrl, ValidationTools.dateFormat(LocalDateTime.now())));
                    }
                } else {
                    System.err.println("Error al descargar el archivo: " + connection.getResponseMessage());
                }
            } catch (
                    Exception e) {
                System.out.println("Error al descargar el archivo: " + e.getMessage());
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
        if (!url.isEmpty() || !url.isBlank()) {
            if (ValidationTools.isValidUrl(url)) {
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "http://" + url;
                }
            } else {
                url = googleUrl + "/search?q=" + url.replace(" ", "+");
            }

            String finalUrl = url;
            cargarURL(finalUrl);
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese una URL.");
        }
    }

    // retroceder en el historial
    private void retrocederPagina() {
        String urlAnterior = historialService.retroceder();
        if (urlAnterior != null) {
            navegacionUsuario = true;
            cargarURL(urlAnterior);
        }
    }

    // avanzar en el historial
    private void avanzarPagina() {
        String urlSiguiente = historialService.avanzar();
        if (urlSiguiente != null) {
            navegacionUsuario = true;
            cargarURL(urlSiguiente);
        }
    }

    // actualizar la URL en el campo de texto
    private void actualizarCampoUrl() {
        String urlActual = historialService.obtenerURLActual();
        if (urlActual != null) {
            urlTextField.setForeground(Color.BLACK);
            urlTextField.setText(urlActual);
        }
    }

    // muestra menu de opciones
    private void mostrarMenuEmergente(JButton menuButton) {
        // iconos para las opciones del menu
        ImageIcon historialIcon = UiTools.cargarIcono("src/main/resources/icons/record.png", 25, 25);
        ImageIcon favoritosIcon = UiTools.cargarIcono("src/main/resources/icons/favoritos.png", 25, 25);
        ImageIcon descargasIcon = UiTools.cargarIcono("src/main/resources/icons/downloads.png", 25, 25);

        // botones del menu
        JMenuItem historialOpc = new JMenuItem("Historial", historialIcon);
        JMenuItem favoritosOpc = new JMenuItem("Favoritos", favoritosIcon);
        JMenuItem descargasOpc = new JMenuItem("Descargas", descargasIcon);
        historialOpc.setIconTextGap(20);
        favoritosOpc.setIconTextGap(20);
        descargasOpc.setIconTextGap(20);
        Font font = new Font("Arial", Font.PLAIN, 16);
        historialOpc.setFont(font);
        favoritosOpc.setFont(font);
        descargasOpc.setFont(font);

        JPopupMenu menuEmergente = new JPopupMenu();
        menuEmergente.add(historialOpc);
        menuEmergente.add(favoritosOpc);
        menuEmergente.add(descargasOpc);
        menuEmergente.setPreferredSize(new Dimension(200, 150));

        Point location = SwingUtilities.convertPoint(menuButton, 0, 0, this);
        int x = location.x;
        int y = location.y + menuButton.getHeight();

        if (x + menuEmergente.getPreferredSize().width > getWidth()) {
            x = getWidth() - menuEmergente.getPreferredSize().width;
        }
        menuEmergente.show(this, x, y);

        historialOpc.addActionListener(e -> mostrarVentanaHistorial());
        favoritosOpc.addActionListener(e -> mostrarVentanaFavoritos());
        descargasOpc.addActionListener(e -> mostrarVentanaDescargas());
    }

    // crea y muestra ventana de historial de navegación
    private void mostrarVentanaHistorial() {
        LinkedList<EntradaHistorial> historialCompleto = historialService.obtener();

        if (historialCompleto.isEmpty()) {
            JOptionPane.showMessageDialog(null, "El historial está vacío.");
        } else {
            // Crear un modelo de tabla para mostrar el historial
            String[] columnNames = {"URL", "FECHA"};
            DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            JTable historialTable = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(historialTable);
            scrollPane.setPreferredSize(new Dimension(500, 300));

            historialTable.getColumnModel().getColumn(0).setPreferredWidth(350);

            // Agregar historial al modelo de la tabla
            for (EntradaHistorial entrada : historialCompleto) {
                tableModel.addRow(new Object[]{entrada.getUrl(), entrada.getFecha()});
            }

            JButton eliminarTodoBtn = UiTools.crearBotonConIcono("Eliminar todo", "src/main/resources/icons/trash.png", 20, 20, null);
            JButton eliminarBtn = UiTools.crearBotonConIcono("Eliminar", "src/main/resources/icons/eliminar-uno.png", 20, 20, null);
            JButton visitarBtn = UiTools.crearBotonConIcono("Abrir", "src/main/resources/icons/browse.png", 20, 20, null);
            JButton cerrarBtn = UiTools.crearBotonConIcono("Cerrar", "src/main/resources/icons/close.png", 20, 20, null);

            eliminarTodoBtn.addActionListener(e -> {
                if (JOptionPane.showConfirmDialog(null, "¿Estás seguro de eliminar todo el historial?", "Confirmación", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    actualizarEstadoBotones();
                    UiTools.cerrarVentana(cerrarBtn);
                    historialService.eliminar();
                    JOptionPane.showMessageDialog(null, "Historial eliminado.");
                }
            });

            eliminarBtn.addActionListener(e -> {
                int selectedRow = historialTable.getSelectedRow();
                if (selectedRow != -1) {
                    EntradaHistorial entradaSeleccionada = new EntradaHistorial((String) tableModel.getValueAt(selectedRow, 0),
                            (String) tableModel.getValueAt(selectedRow, 1));

                    historialService.eliminarEntrada(entradaSeleccionada);
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
                    UiTools.cerrarVentana(cerrarBtn);
                } else {
                    JOptionPane.showMessageDialog(null, "Por favor, selecciona una URL.");
                }
            });

            cerrarBtn.addActionListener(e -> UiTools.cerrarVentana(cerrarBtn));

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
                if (!nombre.isEmpty() && !nombre.isBlank()) {
                    if (favoritoService.existe(url)) {
                        JOptionPane.showMessageDialog(this, "La URL ya está en favoritos.");
                    } else {
                        favoritoService.guardar(new Favorito(nombre, url));
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
        Hashtable<String, String> favoritosMap = favoritoService.obtener();

        if (favoritosMap.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay favoritos.");
        } else {
            // Crear un modelo de tabla para mostrar los favoritos
            String[] columnNames = {"Nombre", "URL"};
            DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            JTable favoritosTable = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(favoritosTable);
            scrollPane.setPreferredSize(new Dimension(500, 300));

            // Ajustar el ancho de las columnas
            favoritosTable.getColumnModel().getColumn(0).setPreferredWidth(150);
            favoritosTable.getColumnModel().getColumn(1).setPreferredWidth(350);

            // Agregar favoritos al modelo de la tabla
            for (String nombreFavorito : favoritosMap.keySet()) {
                String urlFavorito = favoritosMap.get(nombreFavorito);
                tableModel.addRow(new Object[]{nombreFavorito, urlFavorito});
            }

            JButton eliminarTodoBtn = UiTools.crearBotonConIcono("Eliminar todos", "src/main/resources/icons/trash.png", 20, 20, null);
            JButton eliminarBtn = UiTools.crearBotonConIcono("Eliminar", "src/main/resources/icons/eliminar-uno.png", 20, 20, null);
            JButton visitarBtn = UiTools.crearBotonConIcono("Abrir", "src/main/resources/icons/browse.png", 20, 20, null);
            JButton cerrarBtn = UiTools.crearBotonConIcono("Cerrar", "src/main/resources/icons/close.png", 20, 20, null);


            eliminarTodoBtn.addActionListener(e -> {
                if (JOptionPane.showConfirmDialog(null, "¿Estás seguro de eliminar todos los favoritos?", "Confirmación", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    UiTools.cerrarVentana(cerrarBtn);
                    favoritoService.eliminarTodo();
                    JOptionPane.showMessageDialog(null, "Favoritos eliminados.");
                }
            });

            eliminarBtn.addActionListener(e -> {
                int selectedRow = favoritosTable.getSelectedRow();
                if (selectedRow != -1) {
                    if (JOptionPane.showConfirmDialog(null, "¿Estás seguro de eliminar todos los favoritos?", "Confirmación", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        String nombreFavorito = (String) tableModel.getValueAt(selectedRow, 0);
                        favoritoService.eliminar(nombreFavorito);
                        tableModel.removeRow(selectedRow);
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
                    UiTools.cerrarVentana(cerrarBtn);
                } else {
                    JOptionPane.showMessageDialog(null, "Por favor, selecciona un favorito.");
                }
            });

            cerrarBtn.addActionListener(e -> UiTools.cerrarVentana(cerrarBtn));

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
            String[] columnNames = {"Nombre", "URL", "Fecha"};
            DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            JTable descargasTable = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(descargasTable);
            scrollPane.setPreferredSize(new Dimension(500, 300));

            // Ajustar el ancho de las columnas
            descargasTable.getColumnModel().getColumn(0).setPreferredWidth(200);
            descargasTable.getColumnModel().getColumn(1).setPreferredWidth(150);
            descargasTable.getColumnModel().getColumn(2).setPreferredWidth(110);

            // Agregar descargas al modelo de la tabla
            for (Descarga descarga : historialDescargas) {
                tableModel.addRow(new Object[]{descarga.getNombre(), descarga.getUrl(), descarga.getFecha()});
            }

            JButton eliminarTodoBtn = UiTools.crearBotonConIcono("Eliminar todas", "src/main/resources/icons/trash.png", 20, 20, null);
            JButton eliminarBtn = UiTools.crearBotonConIcono("Eliminar", "src/main/resources/icons/eliminar-uno.png", 20, 20, null);
            JButton cerrarBtn = UiTools.crearBotonConIcono("Cerrar", "src/main/resources/icons/close.png", 20, 20, null);

            eliminarTodoBtn.addActionListener(e -> {
                if (JOptionPane.showConfirmDialog(null, "¿Estás seguro de eliminar todas las descargas?", "Confirmación", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    UiTools.cerrarVentana(cerrarBtn);
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
                    descargaService.eliminar(new Descarga(nombre, url, fecha));
                    tableModel.removeRow(selectedRow);
                    JOptionPane.showMessageDialog(null, "Descarga eliminada.");
                } else {
                    JOptionPane.showMessageDialog(null, "Por favor, selecciona una descarga.");
                }
            });

            cerrarBtn.addActionListener(e -> UiTools.cerrarVentana(cerrarBtn));

            // Mostrar descargas y botones
            Object[] options = {eliminarTodoBtn, eliminarBtn, cerrarBtn};
            JOptionPane.showOptionDialog(null, scrollPane, "Historial de Descargas",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                    options, null);
        }
    }

    // actualiza el estado de los botones de navegación
    private void actualizarEstadoBotones() {
        retrocederBtn.setEnabled(historialService.puedeRetroceder());
        avanzarBtn.setEnabled(historialService.puedeAvanzar());
        favoritosBtn.setEnabled(!favoritoService.existe(historialService.obtenerURLActual()));
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
