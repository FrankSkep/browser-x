package browser.util;

import javax.swing.*;
import java.awt.*;

public class DownloadProgressDialog extends JDialog {
    private final JProgressBar progressBar;

    public DownloadProgressDialog(JFrame parent) {
        super(parent, "Descargando...", true);
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        setLayout(new BorderLayout());
        add(progressBar, BorderLayout.CENTER);
        setSize(300, 75);
        setLocationRelativeTo(parent);
    }

    public void updateProgress(int progress) {
        progressBar.setValue(progress);
    }
}
