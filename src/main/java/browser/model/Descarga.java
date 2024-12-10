package browser.model;

import lombok.Getter;

import java.util.Objects;

/**
 * La clase Descarga representa una descarga realizada por el navegador.
 * Contiene el nombre, la URL y la fecha de la descarga.
 */
@Getter
public class Descarga extends ElementoNavegador {
    private String fecha;

    public Descarga(String nombre, String url, String fecha) {
        super(nombre, url);
        this.fecha = fecha;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        Descarga descarga = (Descarga) o;
        return Objects.equals(nombre, descarga.nombre) &&
                Objects.equals(url, descarga.url) &&
                Objects.equals(fecha, descarga.fecha);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre, url, fecha);
    }
}