package browser.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Descarga {
    private String nombre;
    private String url;
    private String fecha;

    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}

        if (o == null || getClass() != o.getClass()) {return false;}

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