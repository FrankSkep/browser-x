package browser.ui;

import java.time.LocalDateTime;
import java.util.List;

public class Validations {

    private static final List<String> ALLOWED_DOMAINS = List.of(
            ".com", ".org", ".net", ".edu", ".gov", ".info", ".io", ".biz", ".name", ".me", ".tech", ".online", ".dev", ".app", ".blog",
            ".us", ".uk", ".ca", ".es", ".mx", ".de", ".fr", ".it", ".br", ".in", ".cn", ".jp", ".au", ".nz", ".za", ".ru", ".ar", ".cl",
            ".co", ".se", ".no", ".fi", ".dk", ".pl", ".nl", ".be", ".pt", ".gr", ".cz", ".sk", ".hu", ".ro", ".tr", ".id", ".sg", ".kr"
    );

    private static final List<String> ALLOWED_FILES = List.of(
            ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".txt", ".csv", ".xml", ".json", ".zip", ".rar", ".tar", ".gz",
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".svg", ".ico", ".mp3", ".mp4", ".avi", ".mkv", ".mov", ".flv", ".wmv", ".webm"
    );

    public static boolean containsDomain(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        // recorrer los dominios permitidos para verificar
        return ALLOWED_DOMAINS.stream().anyMatch(url::endsWith);
    }

    public static boolean isValidFile(String file) {
        if (file == null || file.isBlank()) {
            return false;
        }
        // recorrer las extensiones permitidas para verificar
        return ALLOWED_FILES.stream().anyMatch(file::endsWith);
    }

    public static String dateFormat(LocalDateTime fecha) {
        String dia = fecha.getDayOfMonth() < 10 ? "0" + fecha.getDayOfMonth() : String.valueOf(fecha.getDayOfMonth());
        String mes = fecha.getMonthValue() < 10 ? "0" + fecha.getMonthValue() : String.valueOf(fecha.getMonthValue());
        String anio = String.valueOf(fecha.getYear());
        String hora = fecha.getHour() < 10 ? "0" + fecha.getHour() : String.valueOf(fecha.getHour());
        String minuto = fecha.getMinute() < 10 ? "0" + fecha.getMinute() : String.valueOf(fecha.getMinute());
        return dia + "/" + mes + "/" + anio + " " + hora + ":" + minuto;
    }
}
