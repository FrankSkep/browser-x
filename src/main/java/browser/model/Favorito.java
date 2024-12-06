package browser.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * La clase Favorito representa un favorito del navegador.
 * Contiene el nombre y la URL del favorito.
 */
@AllArgsConstructor
@Getter
@Setter
public class Favorito {
    private String nombre;
    private String url;
}