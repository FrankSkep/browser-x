package browser.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Utilidad para la validación de URLs, archivos y tipos MIME.
 */
public class ValidationUtil {

    private static final List<String> ALLOWED_DOMAINS = List.of(
            ".com", ".org", ".net", ".edu", ".gov", ".info", ".io", ".biz", ".name", ".me", ".tech", ".online", ".dev", ".app", ".blog",
            ".us", ".uk", ".ca", ".es", ".mx", ".de", ".fr", ".it", ".br", ".in", ".cn", ".jp", ".au", ".nz", ".za", ".ru", ".ar", ".cl",
            ".co", ".se", ".no", ".fi", ".dk", ".pl", ".nl", ".be", ".pt", ".gr", ".cz", ".sk", ".hu", ".ro", ".tr", ".id", ".sg", ".kr", "la"
    );

    private static final List<String> ALLOWED_FILES = List.of(
            ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".txt", ".csv", ".xml", ".json", ".zip", ".rar", ".tar", ".gz",
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".svg", ".ico", ".mp3", ".mp4", ".avi", ".mkv", ".mov", ".flv", ".wmv", ".webm", ".ogg", "/cfg"
    );

    private static final List<String> ALLOWED_MIME_TYPES = List.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/xml",
            "application/json",
            "application/zip",
            "application/x-rar-compressed",
            "application/x-tar",
            "application/gzip",
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/bmp",
            "image/svg+xml",
            "image/x-icon",
            "audio/mpeg",
            "video/mp4",
            "text/plain",
            "text/csv"
    );

    /**
     * Verifica si una URL es válida.
     *
     * @param url La URL a verificar.
     * @return true si la URL es válida, false en caso contrario.
     */
    public static boolean isValidUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        if (url.contains(" ") || !url.contains(".")) {
            return false;
        }
        try {
            URL parsedUrl = new URL(url);
            return ALLOWED_DOMAINS.stream().anyMatch(parsedUrl.getHost()::endsWith);
        } catch (
                MalformedURLException e) {
            return false;
        }
    }

    /**
     * Verifica si un archivo es válido según su extensión.
     *
     * @param fileUrl La URL del archivo a verificar.
     * @return true si el archivo es válido, false en caso contrario.
     */
    public static boolean isValidFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return false;
        }
        return ALLOWED_FILES.stream().anyMatch(fileUrl::endsWith);
    }

    /**
     * Verifica si una URL es válida para descarga.
     *
     * @param url La URL a verificar.
     * @return true si la URL es válida para descarga, false en caso contrario.
     */
    public static boolean isDownloadUrl(String url) {
        return isValidFile(url) || isValidMimeType(getContentType(url));
    }

    /**
     * Verifica si un tipo MIME es válido.
     *
     * @param contentType El tipo MIME a verificar.
     * @return true si el tipo MIME es válido, false en caso contrario.
     */
    public static boolean isValidMimeType(String contentType) {
        if (contentType == null)
            return false;
        return ALLOWED_MIME_TYPES.contains(contentType);
    }

    /**
     * Obtiene el tipo de contenido de una URL.
     *
     * @param urlStr La URL de la cual obtener el tipo de contenido.
     * @return El tipo de contenido de la URL.
     */
    public static String getContentType(String urlStr) {
        String contentType = null;
        try {
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.connect();
            contentType = connection.getContentType();
        } catch (
                IOException e) {
            System.err.println("Error al obtener el tipo de contenido: " + e.getMessage());
        }
        return contentType;
    }

    /**
     * Formatea una fecha en el formato dd/MM/yyyy HH:mm.
     *
     * @param fecha La fecha a formatear.
     * @return La fecha formateada como cadena.
     */
    public static String dateFormat(LocalDateTime fecha) {
        String dia = fecha.getDayOfMonth() < 10 ? "0" + fecha.getDayOfMonth() : String.valueOf(fecha.getDayOfMonth());
        String mes = fecha.getMonthValue() < 10 ? "0" + fecha.getMonthValue() : String.valueOf(fecha.getMonthValue());
        String anio = String.valueOf(fecha.getYear());
        String hora = fecha.getHour() < 10 ? "0" + fecha.getHour() : String.valueOf(fecha.getHour());
        String minuto = fecha.getMinute() < 10 ? "0" + fecha.getMinute() : String.valueOf(fecha.getMinute());
        return dia + "/" + mes + "/" + anio + " " + hora + ":" + minuto;
    }

    /**
     * Obtiene la carpeta de descargas del usuario.
     *
     * @return La ruta de la carpeta de descargas.
     */
    public static String getDownloadFolder() {
        String userHome = System.getProperty("user.home");
        String folderName;
        if (Objects.equals(getOperatingSystem(), "Windows")) {
            folderName = "Downloads";
        } else {
            String language = Locale.getDefault().getDisplayLanguage();
            if (language.equals("español")) {
                folderName = "Descargas";
            } else {
                folderName = "Downloads";
            }
        }
        return Paths.get(userHome, folderName).toString();
    }

    /**
     * Obtiene el sistema operativo del usuario.
     *
     * @return El nombre del sistema operativo.
     */
    public static String getOperatingSystem() {
        String os = System.getProperty("os.name").toLowerCase();

        String osName = os.substring(0, 3);

        return switch (osName) {
            case "win" ->
                    "Windows";
            case "mac" ->
                    "MacOS";
            case "nix",
                 "nux",
                 "aix" ->
                    "Unix";
            case "sun" ->
                    "Solaris";
            default ->
                    null;
        };
    }
}