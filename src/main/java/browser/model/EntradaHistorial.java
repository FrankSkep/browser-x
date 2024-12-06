package browser.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * La clase EntradaHistorial representa una entrada en el historial de navegación.
 * Contiene la URL y la fecha de la entrada.
 */
@AllArgsConstructor
@Getter
@Setter
public class EntradaHistorial {
    private String url;
    private String fecha;

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof EntradaHistorial entradaHistorial))
            return false;

        return url.equals(entradaHistorial.url) && fecha.equals(entradaHistorial.fecha);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, fecha);
    }
}