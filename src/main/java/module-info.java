module JavaFX {
    requires javafx.graphics;
    requires javafx.swing;
    requires javafx.web;
    requires com.formdev.flatlaf;
    exports browser;
    exports browser.ui;
    exports browser.service;
}
