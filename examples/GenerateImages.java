import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import javax.imageio.ImageIO;

/** Generate original demo PNGs with only the JDK, without downloading media. */
class GenerateImages {
    public static void main(String[] args) throws Exception {
        Path data = Path.of(args.length == 0 ? "data" : args[0]);
        write(data.resolve("figures/slides/01.png"), "Corona Gel Totem", new Color(20, 60, 90));
        write(data.resolve("figures/slides/02.png"), "Place your hand at the sensor", new Color(35, 95, 80));
        write(data.resolve("figures/sensors/01.png"), "Dispensing", new Color(75, 45, 100));
    }
    private static void write(Path path, String label, Color background) throws Exception {
        Files.createDirectories(path.getParent());
        var image = new BufferedImage(1280, 720, BufferedImage.TYPE_INT_RGB);
        var graphics = image.createGraphics();
        graphics.setColor(background); graphics.fillRect(0, 0, 1280, 720);
        graphics.setColor(Color.WHITE); graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 48));
        graphics.drawString(label, 80, 360); graphics.dispose();
        ImageIO.write(image, "png", path.toFile());
        System.out.println(path);
    }
}
