/**
 * <h1>Documentación de BrowserX</h1>
 * <p>
 * Este proyecto es un navegador web desarrollado con Java, Swing y JavaFX WebEngine.
 * </p>
 *
 * <h3>
 *     Para ver la documentación completa, de click: <a href="{@docRoot}/overview-tree.html">aquí</a>
 * </h3>
 */
module BrowserX {
    requires javafx.graphics;
    requires javafx.swing;
    requires javafx.web;
    requires com.formdev.flatlaf;
    requires java.sql;
    requires static lombok;
}
