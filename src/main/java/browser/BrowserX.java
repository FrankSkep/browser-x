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
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

public class BrowserX extends JFrame {
    private final HistorialService historial;
    private final FavoritoService favoritos;
    private final DescargaService descargas;
    private WebView webView;
    private WebEngine webEngine;

    private final JTextField urlTextField;
    private final JButton retrocederBtn;
    private final JButton avanzarBtn;
    private final JButton favoritosBtn;

    private final String googleUrl = "https://www.google.com";

    // bandera para saber si la navegación fue natural o por avanzar/retroceder
    private boolean navegacionUsuario = false;

    public BrowserX() {
        Db_Connection.initializeDatabase();
        historial = new HistorialService();
        favoritos = new FavoritoService();
        descargas = new DescargaService();

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
                        historial.visitar(finalUrl);
                    }
                    navegacionUsuario = false;
                    actualizarInterfazSwing();
                    actualizarEstadoBotones();

                } else if (newState == Worker.State.FAILED) {
                    urlTextField.setText("");
                    JOptionPane.showMessageDialog(this, "No se pudo cargar la página, verifica la URL.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            // ChangeListener a la propiedad location del WebEngine
            webEngine.locationProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    if (ValidationTools.isValidFile(newValue)) {
                        descargarArchivo(newValue);
                        webEngine.getHistory().go(-1); // Volver a la página anterior
                    }
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

        // Listener para seleccionar todo el texto al hacer clic
        urlTextField.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                urlTextField.selectAll();
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
    private void descargarArchivo(String url) {
        try {
            // obtener el nombre del archivo de la URL
            String fileName = url.substring(url.lastIndexOf('/') + 1);

            // definir la ruta de descarga
            String downloadFolder = ValidationTools.getDownloadFolder();
            Path downloadPath = Paths.get(downloadFolder, fileName);

            // Descargar el archivo
            try (InputStream in = new URI(url).toURL().openStream()) {
                Files.copy(in, downloadPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (
                    URISyntaxException e) {
                JOptionPane.showMessageDialog(this, "Error al descargar el archivo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }

            // registrar la descarga en la base de datos
            String fecha = ValidationTools.dateFormat(LocalDateTime.now());
            descargas.agregarDescarga(new Descarga(fileName, url, fecha));

            JOptionPane.showMessageDialog(this, "Archivo descargado: " + fileName, "Descarga completada", JOptionPane.INFORMATION_MESSAGE);
        } catch (
                IOException e) {
            JOptionPane.showMessageDialog(this, "Error al descargar el archivo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
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
            if (ValidationTools.containsDomain(url)) {
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
        String urlAnterior = historial.retroceder();
        if (urlAnterior != null) {
            navegacionUsuario = true;
            cargarURL(urlAnterior);
        }
    }

    // avanzar en el historial
    private void avanzarPagina() {
        String urlSiguiente = historial.avanzar();
        if (urlSiguiente != null) {
            navegacionUsuario = true;
            cargarURL(urlSiguiente);
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
        LinkedList<EntradaHistorial> historialCompleto = historial.obtenerHistorial();

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
                    historial.eliminarHistorial();
                    JOptionPane.showMessageDialog(null, "Historial eliminado.");
                }
            });

            eliminarBtn.addActionListener(e -> {
                int selectedRow = historialTable.getSelectedRow();
                if (selectedRow != -1) {
                    EntradaHistorial entradaSeleccionada = new EntradaHistorial((String) tableModel.getValueAt(selectedRow, 0),
                            (String) tableModel.getValueAt(selectedRow, 1));

                    historial.eliminarEntradaHistorial(entradaSeleccionada);
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
                        favoritos.insertarFavorito(new Favorito(nombre, url));
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
        Hashtable<String, String> favoritosMap = favoritos.obtenerFavoritos();

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
        LinkedList<Descarga> historialDescargas = descargas.obtenerDescargas();

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
                    descargas.eliminarDescargas();
                    JOptionPane.showMessageDialog(null, "Descargas eliminadas.");
                }
            });

            eliminarBtn.addActionListener(e -> {
                int selectedRow = descargasTable.getSelectedRow();
                if (selectedRow != -1) {
                    String nombre = (String) tableModel.getValueAt(selectedRow, 0);
                    String url = (String) tableModel.getValueAt(selectedRow, 1);
                    String fecha = (String) tableModel.getValueAt(selectedRow, 2);
                    descargas.eliminarDescarga(new Descarga(nombre, url, fecha));
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

    private void actualizarEstadoBotones() {
        retrocederBtn.setEnabled(historial.puedeRetroceder());
        avanzarBtn.setEnabled(historial.puedeAvanzar());
        favoritosBtn.setEnabled(!favoritos.existeFavorito(historial.obtenerURLActual()));
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
