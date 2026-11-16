package com.kleberandrade.coronagel.components;

import com.kleberandrade.coronagel.models.SlideImage;
import com.kleberandrade.coronagel.helpers.FileHelper;
import com.kleberandrade.coronagel.interfaces.HandDetectionListener;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.Timer;

public class CarouselSlider extends JPanel implements ActionListener, HandDetectionListener {

    private final java.nio.file.Path slides;
    private final java.nio.file.Path sensors;

    private final String[] slideImagesFilename;
    private final String sensorImageFilename;
    private final Image[] loadedSlides;
    private final int changeImageTime;
    private final SlideImage currentImage;
    private final SlideImage nextImage;
    private final SlideImage sensorImage;

    private int index = 0;
    private long startTime;
    private int fadeOutTime;
    private Timer timer;
    private final java.util.function.BiFunction<Integer, ActionListener, Timer> timerFactory;
    private boolean handDetected = false;

    public CarouselSlider(int changeImageTime, int fadeOutTime) {
        this(changeImageTime, fadeOutTime, java.nio.file.Path.of(System.getProperty("coronagel.data", "data")));
    }

    public CarouselSlider(int changeImageTime, int fadeOutTime, java.nio.file.Path data) {
        this(changeImageTime, fadeOutTime, data, Timer::new);
    }

    CarouselSlider(int changeImageTime, int fadeOutTime, java.nio.file.Path data,
                   java.util.function.BiFunction<Integer, ActionListener, Timer> timerFactory) {
        this.timerFactory = timerFactory;
        this.slides = data.resolve("figures/slides");
        this.sensors = data.resolve("figures/sensors");
        this.changeImageTime = changeImageTime;
        this.fadeOutTime = fadeOutTime;

        final FileHelper helper = new FileHelper();
        this.slideImagesFilename = helper.getFolderFiles(slides.toString());
        String[] sensorFiles = helper.getFolderFiles(sensors.toString());
        this.sensorImageFilename = sensorFiles.length == 0 ? "" : sensorFiles[0];
        this.loadedSlides = java.util.Arrays.stream(slideImagesFilename)
                .map(name -> new javax.swing.ImageIcon(slides.resolve(name).toString()).getImage())
                .toArray(Image[]::new);

        currentImage = new SlideImage(this);
        nextImage = new SlideImage(this);
        sensorImage = new SlideImage(getSensorImage(), this);

        setTimer();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        if (slideImagesFilename.length == 0 && !handDetected) {
            g2d.drawString("Add images to " + slides, 30, 50);
            return;
        }
        sensorImage.setWidth(getWidth());
        sensorImage.setHeight(getHeight());
        if (handDetected) {
            g2d.drawString("Dispensing", 30, 50);
            sensorImage.draw(g2d);
        } else {
            drawSlides(g2d);
        }
    }

    private void drawSlides(Graphics2D g2d) {
            currentImage.setImage(getCurrentImage());
            nextImage.setImage(getNextImage());

            currentImage.setWidth(getWidth());
            currentImage.setHeight(getHeight());
            nextImage.setWidth(getWidth());
            nextImage.setHeight(getHeight());

            sensorImage.draw(g2d);
            nextImage.draw(g2d);
            currentImage.draw(g2d);
    }

    private void setTimer() {
        this.timer = timerFactory.apply(20, this);
        timer.setInitialDelay(changeImageTime - fadeOutTime);
        timer.start();
    }

    private Image getCurrentImage() {
        return loadedSlides[index];
    }

    private Image getNextImage() {
        return loadedSlides[(index + 1) % loadedSlides.length];
    }

    private Image getSensorImage() {
        return new javax.swing.ImageIcon(sensors.resolve(sensorImageFilename).toString()).getImage();
    }

    @Override
    public void actionPerformed(ActionEvent event) {

        if (slideImagesFilename.length == 0) return;
        if (startTime == 0) {
            startTime = event.getWhen();
        }

        long elapsedTime = event.getWhen() - startTime;
        long displacement = (long) currentImage.getHeight() * elapsedTime / fadeOutTime;

        currentImage.setY((int)displacement);
        nextImage.setY(-nextImage.getHeight() + (int) displacement);

        finishTransition();
        repaint();
    }

    private void finishTransition() {
        if (nextImage.getY() >= 0) {
            startTime = 0;
            timer.stop();
            setTimer();

            currentImage.setY(0);
            index = (index + 1) % slideImagesFilename.length;
        }
    }

    @Override
    public void turnOn() {
        onEdt(() -> setDetected(true));
    }

    private void setDetected(boolean detected) {
        this.handDetected = detected;
        this.startTime = 0;
        currentImage.setY(0);
        if (detected) timer.stop();
        else timer.restart();
        repaint();
    }

    @Override
    public void turnOff() { onEdt(() -> setDetected(false)); }

    private void onEdt(Runnable action) {
        if (javax.swing.SwingUtilities.isEventDispatchThread()) action.run();
        else javax.swing.SwingUtilities.invokeLater(action);
    }

    public void stop() { timer.stop(); }
}
