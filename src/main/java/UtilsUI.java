import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class UtilsUI {

    public static Image redimensionarImagen(String path, int width, int height) {
        try {
            BufferedImage originalImage = ImageIO.read(new File(path));
            return originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        } catch (
                IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
