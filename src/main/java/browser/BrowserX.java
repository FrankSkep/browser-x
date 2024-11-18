package browser;

import browser.dao.DescargasDAO;
import browser.database.Db_Connection;
import browser.model.Descarga;
import browser.service.FavoritoService;
import browser.ui.UI_Utils;
import browser.data_structures.LinkedList;
import browser.service.HistorialService;
import browser.ui.Validations;
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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class BrowserX extends JFrame {
    private final JTextField urlTextField;
    private final HistorialService historial;
    private final FavoritoService favoritos;
    private WebView webView;
    private WebEngine webEngine;

    // bandera para saber si la navegación fue natural o por avanzar/retroceder
    private boolean navegacionUsuario = false;

    public BrowserX() {
        Db_Connection.initializeDatabase();
        historial = new HistorialService();
        favoritos = new FavoritoService();

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
        JButton retrocederBtn = UI_Utils.crearBotonConIcono(null, "src/main/resources/icons/previous-page.png", 25, 25, 3);
        JButton avanzarBtn = UI_Utils.crearBotonConIcono(null, "src/main/resources/icons/next-page.png", 25, 25, 3);
        JButton inicioBtn = UI_Utils.crearBotonConIcono(null, "src/main/resources/icons/home.png", 25, 25, 3);
        JButton refrescarBtn = UI_Utils.crearBotonConIcono(null, "src/main/resources/icons/refresh.png", 25, 25, 3);
        panelBotones.add(retrocederBtn);
        panelBotones.add(avanzarBtn);
        panelBotones.add(inicioBtn);
        panelBotones.add(refrescarBtn);

        // Panel para los botones de visitar y menu
        JPanel panelMenu = new JPanel();
        panelMenu.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JButton visitarBtn = UI_Utils.crearBotonConIcono(null, "src/main/resources/icons/browse.png", 25, 25, 3);
        JButton favoritosBtn = UI_Utils.crearBotonConIcono(null, "src/main/resources/icons/estrella.png", 25, 25, 3);
        JButton showMenuBtn = UI_Utils.crearBotonConIcono(null, "src/main/resources/icons/menu.png", 25, 25, 3);
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

            // ChangeListener a la propiedad location del WebEngine
            webEngine.locationProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
//                    List<String> extensionesPermitidas = Arrays.asList(".pdf", ".zip", ".exe", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".jpg", ".png", ".gif", ".mp3", ".mp4");
//                    boolean esArchivoPermitido = extensionesPermitidas.stream().anyMatch(newValue::endsWith);
                    if (Validations.isValidFile(newValue)) {
                        descargarArchivo(newValue);
                        Platform.runLater(() -> webEngine.getHistory().go(-1)); // Volver a la página anterior
                    }
                }
            });

            // carga pagina inicial (Google)
            cargarURL("https://www.google.com");
        });

        // Listeners para los botones
        retrocederBtn.addActionListener(e -> retrocederPagina());
        avanzarBtn.addActionListener(e -> avanzarPagina());
        inicioBtn.addActionListener(e -> cargarURL("https://www.google.com"));
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

    // Método para descargar el archivo
    private void descargarArchivo(String url) {
        try (InputStream in = new URL(url).openStream()) {
            String fileName = url.substring(url.lastIndexOf('/') + 1);
            String userHome = System.getProperty("user.home");
            String downloadDir = Paths.get(userHome, "Downloads").toString();
            Files.createDirectories(Paths.get(downloadDir)); // Crear la carpeta si no existe
            Files.copy(in, Paths.get(downloadDir, fileName), StandardCopyOption.REPLACE_EXISTING);
            JOptionPane.showMessageDialog(this, "Archivo descargado: " + fileName);

            DescargasDAO.getInstance().guardar(new Descarga(fileName, url, LocalDateTime.now()));
        } catch (
                IOException e) {
            JOptionPane.showMessageDialog(this, "Error al descargar el archivo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(); // Imprimir el stack trace para más detalles
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

    // visitar una pagina nueva
    private void visitarPagina() {
        String url = urlTextField.getText();
        if (url.equals("Ingrese una URL")) {
            url = "";
        }
        if (!url.isEmpty() || !url.isBlank()) {
            if (Validations.isValidURL(url)) {
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "http://" + url;
                }
            } else {
                url = "https://www.google.com/search?q=" + url.replace(" ", "+");
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
        ImageIcon descargasIcon = UI_Utils.cargarIcono("src/main/resources/icons/downloads.png", 25, 25);

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
        LinkedList<String> historialCompleto = historial.obtenerHistorialList();

        if (historialCompleto.isEmpty()) {
            JOptionPane.showMessageDialog(null, "El historial está vacío.");
        } else {
            // Crear un modelo de tabla para mostrar el historial
            String[] columnNames = {"URL"};
            DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            JTable historialTable = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(historialTable);
            scrollPane.setPreferredSize(new Dimension(500, 300));

            // Agregar historial al modelo de la tabla
            for (String url : historialCompleto) {
                tableModel.addRow(new Object[]{url});
            }

            JButton eliminarTodoBtn = UI_Utils.crearBotonConIcono("Eliminar todo", "src/main/resources/icons/trash.png", 20, 20, null);
            JButton eliminarBtn = UI_Utils.crearBotonConIcono("Eliminar", "src/main/resources/icons/eliminar-uno.png", 20, 20, null);
            JButton visitarBtn = UI_Utils.crearBotonConIcono("Abrir", "src/main/resources/icons/browse.png", 20, 20, null);
            JButton cerrarBtn = UI_Utils.crearBotonConIcono("Cerrar", "src/main/resources/icons/close.png", 20, 20, null);

            eliminarTodoBtn.addActionListener(e -> {
                if (JOptionPane.showConfirmDialog(null, "¿Estás seguro de eliminar todo el historial?", "Confirmación", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    UI_Utils.cerrarVentana(cerrarBtn);
                    historial.deleteHistory();
                    JOptionPane.showMessageDialog(null, "Historial eliminado.");
                }
            });

            eliminarBtn.addActionListener(e -> {
                int selectedRow = historialTable.getSelectedRow();
                if (selectedRow != -1) {
                    String urlSeleccionada = (String) tableModel.getValueAt(selectedRow, 0);
                    historialCompleto.remove(urlSeleccionada);
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
                    UI_Utils.cerrarVentana(cerrarBtn);
                } else {
                    JOptionPane.showMessageDialog(null, "Por favor, selecciona una URL.");
                }
            });

            cerrarBtn.addActionListener(e -> UI_Utils.cerrarVentana(cerrarBtn));

            // Mostrar historial y botones
            Object[] options = {eliminarTodoBtn, eliminarBtn, visitarBtn, cerrarBtn};
            JOptionPane.showOptionDialog(null, scrollPane, "Historial de Navegación",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                    options, null);
        }
    }

    // agrega una URL a favoritos
    private void agregarFavorito() {
        String url = urlTextField.getText();
        if (url.equals("Ingrese una URL") || url.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay URL para agregar a favoritos.");
        } else {
            String nombre = JOptionPane.showInputDialog(this, "Ingresa un nombre para el favorito:");
            if (nombre != null) {
                if (!nombre.isEmpty() && !nombre.isBlank()) {
                    if (favoritos.existeFavorito(url)) {
                        JOptionPane.showMessageDialog(this, "La URL ya está en favoritos.");
                    } else {
                        favoritos.insertarFavorito(nombre, url);
                        JOptionPane.showMessageDialog(this, "Favorito agregado.");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Por favor, ingresa un nombre válido.");
                }
            }
        }
    }

    // crea y muestra ventana de favoritos
    private void mostrarVentanaFavoritos() {
        HashMap<String, String> favoritosMap = favoritos.obtenerFavoritos();

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
            favoritosTable.getColumnModel().getColumn(0).setPreferredWidth(150); // Columna "Nombre" más corta
            favoritosTable.getColumnModel().getColumn(1).setPreferredWidth(350); // Columna "URL" más ancha

            // Agregar favoritos al modelo de la tabla
            for (String nombreFavorito : favoritosMap.keySet()) {
                String urlFavorito = favoritosMap.get(nombreFavorito);
                tableModel.addRow(new Object[]{nombreFavorito, urlFavorito});
            }

            JButton eliminarTodoBtn = UI_Utils.crearBotonConIcono("Eliminar todos", "src/main/resources/icons/trash.png", 20, 20, null);
            JButton eliminarBtn = UI_Utils.crearBotonConIcono("Eliminar", "src/main/resources/icons/eliminar-uno.png", 20, 20, null);
            JButton visitarBtn = UI_Utils.crearBotonConIcono("Abrir", "src/main/resources/icons/browse.png", 20, 20, null);
            JButton cerrarBtn = UI_Utils.crearBotonConIcono("Cerrar", "src/main/resources/icons/close.png", 20, 20, null);


            eliminarTodoBtn.addActionListener(e -> {
                if (JOptionPane.showConfirmDialog(null, "¿Estás seguro de eliminar todos los favoritos?", "Confirmación", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    UI_Utils.cerrarVentana(cerrarBtn);
                    favoritos.eliminarFavoritos();
                    JOptionPane.showMessageDialog(null, "Favoritos eliminados.");
                }
            });

            eliminarBtn.addActionListener(e -> {
                int selectedRow = favoritosTable.getSelectedRow();
                if (selectedRow != -1) {
                    if (JOptionPane.showConfirmDialog(null, "¿Estás seguro de eliminar todos los favoritos?", "Confirmación", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        String nombreFavorito = (String) tableModel.getValueAt(selectedRow, 0);
                        favoritos.eliminarFavorito(nombreFavorito);
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
                    UI_Utils.cerrarVentana(cerrarBtn);
                } else {
                    JOptionPane.showMessageDialog(null, "Por favor, selecciona un favorito.");
                }
            });

            cerrarBtn.addActionListener(e -> UI_Utils.cerrarVentana(cerrarBtn));

            // Mostrar favoritos y botones
            Object[] options = {eliminarTodoBtn, eliminarBtn, visitarBtn, cerrarBtn};
            JOptionPane.showOptionDialog(null, scrollPane, "Favoritos",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                    options, null);
        }
    }

    // crea y muestra ventana de historial de navegación
    private void mostrarVentanaDescargas() {
        List<Descarga> historialDescargas = DescargasDAO.getInstance().obtenerTodo();

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

            // Agregar descargas al modelo de la tabla
            for (Descarga descarga : historialDescargas) {
                tableModel.addRow(new Object[]{descarga.getNombre(), descarga.getUrl(), descarga.getFecha().toString()});
            }

            JButton eliminarTodoBtn = UI_Utils.crearBotonConIcono("Eliminar todas", "src/main/resources/icons/trash.png", 20, 20, null);
            JButton eliminarBtn = UI_Utils.crearBotonConIcono("Eliminar", "src/main/resources/icons/eliminar-uno.png", 20, 20, null);
            JButton cerrarBtn = UI_Utils.crearBotonConIcono("Cerrar", "src/main/resources/icons/close.png", 20, 20, null);

            eliminarTodoBtn.addActionListener(e -> {
                if (JOptionPane.showConfirmDialog(null, "¿Estás seguro de eliminar todas las descargas?", "Confirmación", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    UI_Utils.cerrarVentana(cerrarBtn);
                    DescargasDAO.getInstance().eliminarTodo();
                    JOptionPane.showMessageDialog(null, "Descargas eliminadas.");
                }
            });

            eliminarBtn.addActionListener(e -> {
                int selectedRow = descargasTable.getSelectedRow();
                if (selectedRow != -1) {
                    String nombreDescarga = (String) tableModel.getValueAt(selectedRow, 0);
                    DescargasDAO.getInstance().eliminar(nombreDescarga);
                    tableModel.removeRow(selectedRow);
                    JOptionPane.showMessageDialog(null, "Descarga eliminada.");
                } else {
                    JOptionPane.showMessageDialog(null, "Por favor, selecciona una descarga.");
                }
            });

            cerrarBtn.addActionListener(e -> UI_Utils.cerrarVentana(cerrarBtn));

            // Mostrar descargas y botones
            Object[] options = {eliminarTodoBtn, eliminarBtn, cerrarBtn};
            JOptionPane.showOptionDialog(null, scrollPane, "Historial de Descargas",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                    options, null);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new JFXPanel(); // Inicializacion JavaFX
            new BrowserX(); // Creacion ventana principal
        });

        // shutdown hook para asegurarse de que todas las tareas de JavaFX se completen antes de cerrar
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Platform.runLater(Platform::exit); // Cerrar la aplicación JavaFX
        }));
    }
}
