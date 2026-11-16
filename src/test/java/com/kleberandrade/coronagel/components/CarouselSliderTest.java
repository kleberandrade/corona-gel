package com.kleberandrade.coronagel.components;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import static org.junit.jupiter.api.Assertions.*;
class CarouselSliderTest {
    @TempDir Path data;
    private void image(String name, Color color) throws Exception {
        Path path = data.resolve("figures/" + name); Files.createDirectories(path.getParent());
        var image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        var graphics = image.createGraphics(); graphics.setColor(color); graphics.fillRect(0, 0, 10, 10); graphics.dispose();
        ImageIO.write(image, "png", path.toFile());
    }
    private int paint(CarouselSlider slider) {
        slider.setSize(100, 100);
        var frame = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        var graphics = frame.createGraphics(); slider.paint(graphics); graphics.dispose();
        return frame.getRGB(50, 0);
    }
    @Test void emptyFoldersAndDetectionBeforeFirstPaintAreSafe() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var slider = new CarouselSlider(7000, 500, data);
            try {
                paint(slider);
                slider.actionPerformed(new ActionEvent(this, 0, "tick", 100, 0));
                slider.turnOn(); paint(slider); slider.turnOff(); paint(slider);
            }
            finally { slider.stop(); }
        });
    }
    @Test void rendersSlidesTransitionsAndSensorImageHeadlessly() throws Exception {
        image("slides/1.png", Color.RED); image("slides/2.png", Color.BLUE); image("sensors/1.png", Color.GREEN);
        SwingUtilities.invokeAndWait(() -> {
            var slider = new CarouselSlider(7000, 500, data);
            try {
                assertEquals(Color.RED.getRGB(), paint(slider));
                slider.actionPerformed(new ActionEvent(this, 0, "tick", 100, 0));
                slider.actionPerformed(new ActionEvent(this, 0, "tick", 600, 0));
                assertEquals(Color.BLUE.getRGB(), paint(slider));
                slider.turnOn(); assertEquals(Color.GREEN.getRGB(), paint(slider));
                slider.turnOff(); assertEquals(Color.BLUE.getRGB(), paint(slider));
            } finally { slider.stop(); }
        });
    }
    @Test void animationMovesBothImagesAndResumesAtTopAfterDetection() throws Exception {
        image("slides/1.png", Color.RED); image("slides/2.png", Color.BLUE);
        SwingUtilities.invokeAndWait(() -> {
            var slider = new CarouselSlider(7000, 500, data);
            try {
                paint(slider);
                slider.actionPerformed(new ActionEvent(this, 0, "tick", 100, 0));
                slider.actionPerformed(new ActionEvent(this, 0, "tick", 225, 0));
                var frame = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
                var graphics = frame.createGraphics(); slider.paint(graphics); graphics.dispose();
                assertEquals(Color.BLUE.getRGB(), frame.getRGB(50, 10));
                assertEquals(Color.RED.getRGB(), frame.getRGB(50, 90));
                slider.turnOn(); slider.turnOff();
                assertEquals(Color.RED.getRGB(), paint(slider));
                slider.actionPerformed(new ActionEvent(this, 0, "tick", 1000, 0));
                assertEquals(Color.RED.getRGB(), paint(slider));
                slider.actionPerformed(new ActionEvent(this, 0, "tick", 1500, 0));
                assertEquals(Color.BLUE.getRGB(), paint(slider));
                slider.actionPerformed(new ActionEvent(this, 0, "tick", 2000, 0));
                assertEquals(Color.BLUE.getRGB(), paint(slider));
                slider.actionPerformed(new ActionEvent(this, 0, "tick", 2500, 0));
                assertEquals(Color.RED.getRGB(), paint(slider));
            } finally { slider.stop(); }
        });
    }
}
