import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import javax.imageio.ImageIO;

/** Original illustrated kiosk media, rendered using only Java2D. */
class GenerateImages {
    private record Slide(String file, String eyebrow, String title, String description, Color accent) { }
    public static void main(String[] args) throws Exception {
        Path data = Path.of(args.length == 0 ? "data" : args[0]);
        generate(data, dimensions(args));
    }
    private static void generate(Path data, Dimension size) throws Exception {
        var slides = java.util.List.of(
            new Slide("slides/01.png", "TOUCH-FREE GEL STATION", "Place your\nhand below.", "A simple gesture. Automatic dispensing.", new Color(91, 232, 192)),
            new Slide("slides/02.png", "READY FOR YOUR NEXT VISIT", "Ready when\nyou are.", "Remove your hand after the flow stops.", new Color(115, 204, 255)),
            new Slide("sensors/01.png", "DISPENSING", "One moment.\nPlease wait.", "Remove your hand when the flow stops.", new Color(158, 171, 255)));
        for (var slide : slides) write(data.resolve("figures/" + slide.file()), slide, size);
    }
    private static Dimension dimensions(String[] args) {
        String format = args.length > 1 ? args[1] : "";
        return java.util.Map.of("--portrait", new Dimension(720, 1280))
                .getOrDefault(format, new Dimension(1280, 720));
    }
    private static void write(Path path, Slide slide, Dimension size) throws Exception {
        Files.createDirectories(path.getParent());
        var image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
        var g = image.createGraphics();
        try { render(g, slide, size); }
        finally { g.dispose(); }
        ImageIO.write(image, "png", path.toFile());
        System.out.println(path);
    }
    private static void render(Graphics2D g, Slide slide, Dimension size) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        boolean portrait = size.height > size.width;
        background(g, size);
        header(g, size.width, slide.accent());
        copy(g, slide);
        content(g, slide, portrait);
        text(g, "CORONA GEL  /  AUTOMATIC DISPENSER", 56, size.height - 38, 15, new Color(157, 181, 200), Font.PLAIN);
    }
    private static void background(Graphics2D g, Dimension size) {
        g.setPaint(new GradientPaint(0, 0, new Color(13, 30, 49), size.width, size.height, new Color(23, 65, 79)));
        g.fillRect(0, 0, size.width, size.height);
        g.setColor(new Color(91, 232, 192, 12));
        g.fillOval(size.width - 300, -150, 500, 500);
        g.fillOval(-180, size.height - 300, 400, 400);
    }
    private static void header(Graphics2D g, int width, Color accent) {
        g.setColor(accent); g.fillRoundRect(56, 47, 10, 28, 8, 8);
        text(g, "CORONA GEL", 82, 70, 23, Color.WHITE, Font.BOLD);
        g.setColor(new Color(255, 255, 255, 45)); g.drawLine(56, 103, width - 56, 103);
    }
    private static void copy(Graphics2D g, Slide slide) {
        text(g, slide.eyebrow(), 56, 155, 16, slide.accent(), Font.BOLD);
        int y = 230;
        for (String line : slide.title().split("\\n")) {
            text(g, line, 52, y, 64, Color.WHITE, Font.BOLD); y += 74;
        }
        text(g, slide.description(), 56, 350, 21, new Color(190, 210, 221), Font.PLAIN);
    }
    private record Layout(Rectangle hero, int firstStep, int gap) { }
    private static Layout layout(boolean portrait) {
        return portrait ? new Layout(new Rectangle(56, 410, 608, 420), 889, 103)
                        : new Layout(new Rectangle(725, 143, 499, 460), 390, 86);
    }
    private static void content(Graphics2D g, Slide slide, boolean portrait) {
        var layout = layout(portrait);
        var hero = layout.hero();
        illustration(g, hero.x, hero.y, hero.width, hero.height, slide.accent());
        int first = layout.firstStep();
        int gap = layout.gap();
        step(g, 56, first, 608, "01", "Move your hand", "Keep it under the dispenser.", slide.accent());
        step(g, 56, first + gap, 608, "02", "Wait a moment", "The dispenser stops automatically.", slide.accent());
        step(g, 56, first + 2 * gap, 608, "03", "Remove your hand", "Leave the sensing area clear.", slide.accent());
    }
    private static void step(Graphics2D g, int x, int y, int width, String number, String title, String detail, Color accent) {
        g.setColor(new Color(255, 255, 255, 10)); g.fillRoundRect(x, y, width, 78, 20, 20);
        g.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 25));
        g.fillRoundRect(x + 14, y + 14, 50, 50, 15, 15);
        text(g, number, x + 24, y + 46, 20, accent, Font.BOLD);
        text(g, title, x + 84, y + 32, 22, Color.WHITE, Font.BOLD);
        text(g, detail, x + 84, y + 59, 17, new Color(178, 203, 218), Font.PLAIN);
    }
    private static void illustration(Graphics2D g, int x, int y, int width, int height, Color accent) {
        g.setColor(new Color(255, 255, 255, 9)); g.fillRoundRect(x, y, width, height, 36, 36);
        var art = (Graphics2D) g.create();
        try {
            art.translate(x + width / 2.0 - 200, y + height / 2.0 - 175);
            art.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 16));
            art.fillOval(55, 5, 290, 290);
            droplet(art, accent);
            hand(art, accent);
        } finally { art.dispose(); }
    }
    private static void droplet(Graphics2D g, Color accent) {
        var shape = new Path2D.Double();
        shape.moveTo(200, 42); shape.curveTo(180, 76, 148, 100, 148, 130);
        shape.curveTo(148, 200, 252, 200, 252, 130); shape.curveTo(252, 100, 220, 76, 200, 42);
        g.setPaint(new GradientPaint(160, 70, Color.WHITE, 230, 175, accent)); g.fill(shape);
        g.setColor(new Color(255, 255, 255, 180));
        g.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Arc2D.Double(165, 115, 45, 42, 180, 65, Arc2D.OPEN));
        g.setColor(accent); g.drawLine(200, 198, 200, 210);
    }
    private static void hand(Graphics2D g, Color accent) {
        var shape = new Path2D.Double();
        shape.moveTo(52, 287); shape.lineTo(111, 239);
        shape.curveTo(129, 225, 142, 227, 161, 236); shape.lineTo(199, 252);
        shape.lineTo(248, 252); shape.curveTo(275, 252, 275, 225, 248, 225);
        shape.lineTo(211, 225); shape.lineTo(179, 206);
        shape.curveTo(166, 195, 180, 179, 194, 186); shape.lineTo(244, 207);
        shape.lineTo(293, 207); shape.curveTo(313, 207, 328, 217, 344, 231);
        shape.lineTo(377, 258); shape.lineTo(324, 305);
        shape.curveTo(311, 317, 297, 321, 277, 321); shape.lineTo(157, 321);
        shape.lineTo(119, 344); shape.closePath();
        g.setPaint(new GradientPaint(80, 225, new Color(209, 245, 239), 350, 330, accent)); g.fill(shape);
    }
    private static void text(Graphics2D g, String text, int x, int y, int size, Color color, int style) {
        g.setColor(color); g.setFont(new Font(Font.SANS_SERIF, style, size)); g.drawString(text, x, y);
    }
}
