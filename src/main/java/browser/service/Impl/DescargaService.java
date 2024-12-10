package browser.service.Impl;

import browser.dao.Impl.DescargaDAOImpl;
import browser.data_structure.LinkedList;
import browser.model.Descarga;
import browser.service.IService;
import browser.util.DownloadProgressDialog;
import browser.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

/**
 * Servicio para gestionar las descargas del navegador.
 */
public class DescargaService  implements IService<LinkedList<Descarga>,String,Descarga> {

    private final LinkedList<Descarga> descargas;

    /**
     * Constructor que inicializa la lista enlazada de descargas y carga los datos desde el DAO.
     */
    public DescargaService() {
        descargas = new LinkedList<>();
        descargas.addAll(DescargaDAOImpl.getInstance().getAll());
    }

    /**
     * Guarda una descarga en la lista y en el DAO.
     *
     * @param descarga La descarga a guardar.
     */
    @Override
    public void agregarElemento(Descarga descarga) {
        descargas.add(descarga);
        DescargaDAOImpl.getInstance().save(descarga);
    }

    /**
     * Elimina una descarga de la lista y del DAO.
     *
     * @param descarga La descarga a eliminar.
     */
    @Override
    public void eliminarElemento(Descarga descarga) {
        descargas.remove(descarga);
        DescargaDAOImpl.getInstance().delete(descarga);
    }

    /**
     * Elimina todas las descargas de la lista y del DAO.
     */
    @Override
    public void eliminarTodo() {
        descargas.clear();
        DescargaDAOImpl.getInstance().deleteAll();
    }

    /**
     * Obtiene todas las descargas.
     *
     * @return Una lista enlazada con todas las descargas.
     */
    @Override
    public LinkedList<Descarga> obtenerTodo() {
        return descargas;
    }

    @Override
    public String obtenerElemento() {
        return "";
    }

    /**
     * Descarga un archivo desde una URL y muestra el progreso en un diálogo.
     *
     * @param fileUrl La URL del archivo a descargar.
     * @param parent  El componente padre para el diálogo de progreso.
     */
    public void descargarArchivo(String fileUrl, Component parent) {
        DownloadProgressDialog progressDialog = new DownloadProgressDialog((JFrame) parent);

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

                        JOptionPane.showMessageDialog(parent, "Archivo descargado: " + fileName);
                        agregarElemento(new Descarga(fileName, fileUrl, ValidationUtil.dateFormat(LocalDateTime.now())));
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
}