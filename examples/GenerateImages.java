import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import javax.imageio.ImageIO;

/** Generate original demo PNGs with only the JDK, without downloading media. */
class GenerateImages {
    public static void main(String[] args) throws Exception {
        Path data = Path.of(args.length == 0 ? "data" : args[0]);
        Dimension size = dimensions(args);
        int width = size.width;
        int height = size.height;
        write(data.resolve("figures/slides/01.png"), "Corona Gel Totem", new Color(20, 60, 90), width, height);
        write(data.resolve("figures/slides/02.png"), "Place your hand at the sensor", new Color(35, 95, 80), width, height);
        write(data.resolve("figures/sensors/01.png"), "Dispensing", new Color(75, 45, 100), width, height);
    }
    private static Dimension dimensions(String[] args) {
        String format = args.length > 1 ? args[1] : "";
        return java.util.Map.of("--portrait", new Dimension(720, 1280))
                .getOrDefault(format, new Dimension(1280, 720));
    }
    private static void write(Path path, String label, Color background, int width, int height) throws Exception {
        Files.createDirectories(path.getParent());
        var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        var graphics = image.createGraphics();
        graphics.setColor(background); graphics.fillRect(0, 0, width, height);
        graphics.setColor(Color.WHITE); graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.min(48, width / 25)));
        graphics.drawString(label, 80, height / 2); graphics.dispose();
        ImageIO.write(image, "png", path.toFile());
        System.out.println(path);
    }
}
