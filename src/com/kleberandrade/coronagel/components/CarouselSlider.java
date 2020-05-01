package com.kleberandrade.coronagel.components;

import com.kleberandrade.coronagel.models.SlideImage;
import com.kleberandrade.coronagel.helpers.FileHelper;
import com.kleberandrade.coronagel.interfaces.HandDetectionListener;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.Timer;

public class CarouselSlider extends JPanel implements ActionListener, HandDetectionListener {

    public static String SLIDE_IMAGES = "/home/pi/Desktop/CoronaGel/figures/slides/";
    public static String SENSOR_IMAGES = "/home/pi/Desktop/CoronaGel/figures/sensors/";

    private final String[] slideImagesFilename;
    private final String sensorImageFilename;
    private final int changeImageTime;
    private final SlideImage currentImage;
    private final SlideImage nextImage;
    private final SlideImage sensorImage;

    private int index = 0;
    private boolean firstTime = true;
    private long startTime;
    private int fadeOutTime;
    private Timer timer;
    private boolean handDetected = false;

    public CarouselSlider(int changeImageTime, int fadeOutTime) {
        this.changeImageTime = changeImageTime;
        this.fadeOutTime = fadeOutTime;

        final FileHelper helper = new FileHelper();
        this.slideImagesFilename = helper.getFolderFiles(SLIDE_IMAGES);
        this.sensorImageFilename = helper.getFolderFiles(SENSOR_IMAGES)[0];

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

        if (handDetected) {
            sensorImage.draw(g2d);
        } else {
            currentImage.setImage(getCurrentImage());
            nextImage.setImage(getNextImage());

            if (firstTime) {
                sensorImage.setWidth(graphics.getClipBounds().width);
                sensorImage.setHeight(graphics.getClipBounds().height);
                currentImage.setWidth(graphics.getClipBounds().width);
                currentImage.setHeight(graphics.getClipBounds().height);
                nextImage.setWidth(graphics.getClipBounds().width);
                nextImage.setHeight(graphics.getClipBounds().height);
                nextImage.setY(currentImage.getHeight());
                firstTime = false;
            }

            sensorImage.draw(g2d);
            nextImage.draw(g2d);
            currentImage.draw(g2d);
        }
    }

    private void setTimer() {
        this.timer = new Timer(20, this);
        timer.setInitialDelay(changeImageTime - fadeOutTime);
        timer.start();
    }

    private Image getCurrentImage() {
        return Toolkit.getDefaultToolkit().getImage(SLIDE_IMAGES + slideImagesFilename[index]);
    }

    private Image getNextImage() {
        return Toolkit.getDefaultToolkit().getImage(SLIDE_IMAGES + slideImagesFilename[(index + 1) % slideImagesFilename.length]);
    }

    private Image getSensorImage() {
        return Toolkit.getDefaultToolkit().getImage(SENSOR_IMAGES + sensorImageFilename);
    }

    @Override
    public void actionPerformed(ActionEvent event) {

        if (startTime == 0) {
            startTime = event.getWhen();
        }

        long elapsedTime = event.getWhen() - startTime;
        long displacement = currentImage.getHeight()/ fadeOutTime * elapsedTime;

        currentImage.setY((int) -displacement);
        nextImage.setY(nextImage.getHeight()- (int) displacement);

        if (nextImage.getY() <= 0) {
            startTime = 0;
            timer.stop();
            setTimer();

            currentImage.setY(0);
            nextImage.setY(currentImage.getHeight());
            index = ++index % slideImagesFilename.length;
        }

        repaint();
    }

    @Override
    public void turnOn() {
        this.handDetected = true;
        repaint();
        this.timer.stop();
    }

    @Override
    public void turnOff() {
        this.handDetected = false;
        this.firstTime = true;
        repaint();
        this.timer.start();
    }
}
