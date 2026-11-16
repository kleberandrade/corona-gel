import com.kleberandrade.coronagel.components.CarouselSlider;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

/** Render the real kiosk component with local media; no GPIO or desktop capture. */
class CapturePreview {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("Usage: CapturePreview <data-root> <output.png>");
        Path data = Path.of(args[0]);
        Path output = Path.of(args[1]).toAbsolutePath();
        Files.createDirectories(output.getParent());
        SwingUtilities.invokeAndWait(() -> render(data, output));
        System.out.println(output);
    }
    private static void render(Path data, Path output) {
        var slider = new CarouselSlider(7000, 500, data);
        try {
            slider.setSize(720, 1280);
            var image = new BufferedImage(720, 1280, BufferedImage.TYPE_INT_RGB);
            var graphics = image.createGraphics();
            try { slider.paint(graphics); }
            finally { graphics.dispose(); }
            ImageIO.write(image, "png", output.toFile());
        } catch (java.io.IOException failure) {
            throw new java.io.UncheckedIOException(failure);
        } finally { slider.stop(); }
    }
}
