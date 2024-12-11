package browser.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Clase que representa un elemento del navegador (historial o favoritos).
 */
@AllArgsConstructor
@Getter
@Setter
public class ElementoNavegador {

    protected String nombre;
    protected String url;
}
