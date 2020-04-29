package com.kleberandrade.coronagel.controllers;

import com.kleberandrade.coronagel.models.CarouselSliderImage;
import com.kleberandrade.coronagel.helpers.FileHelper;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.Timer;

public class CarouselSlider extends JPanel implements ActionListener {

    private final String[] images;
    private final int changeImageTime;
    private final CarouselSliderImage currentImage;
    private final CarouselSliderImage nextImage;

    private int index = 0;
    private boolean firstTime = true;
    private long startTime;
    private int fadeOutTime;
    private Timer timer;

    public CarouselSlider(int changeImageTime, int fadeOutTime) {
        this.changeImageTime = changeImageTime;
        this.fadeOutTime = fadeOutTime;
        this.images = takeImages();

        currentImage = new CarouselSliderImage(this);
        nextImage = new CarouselSliderImage(this);

        setTimer();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        currentImage.setImage(getCurrentImage());
        nextImage.setImage(getNextImage());

        if (firstTime) {
            currentImage.setWidth(graphics.getClipBounds().width);
            currentImage.setHeight(graphics.getClipBounds().height);
            nextImage.setWidth(graphics.getClipBounds().width);
            nextImage.setHeight(graphics.getClipBounds().height);
            nextImage.setX(currentImage.getWidth());
            firstTime = false;
        }

        nextImage.draw(graphics);
        currentImage.draw(graphics);
    }

    private void setTimer() {
        this.timer = new Timer(20, this);
        timer.setInitialDelay(changeImageTime - fadeOutTime);
        timer.start();
    }

    private Image getCurrentImage() {
        return Toolkit.getDefaultToolkit().getImage(FileHelper.IMAGES_FILE + images[index]);
    }

    private Image getNextImage() {
        return Toolkit.getDefaultToolkit().getImage(FileHelper.IMAGES_FILE + images[(index + 1) % images.length]);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        
        if (startTime == 0){
            startTime = event.getWhen();
        }
        
        long elapsedTime = event.getWhen() - startTime;
        long displacement = currentImage.getWidth() / fadeOutTime * elapsedTime;
        
        currentImage.setX((int)-displacement);
        nextImage.setX(nextImage.getWidth() - (int)displacement);
        
        if (nextImage.getX() <= 0){
            startTime = 0;
            timer.stop();
            setTimer();
            
            currentImage.setX(0);
            nextImage.setX(currentImage.getWidth());
            index = ++index % images.length;
        }
        
        repaint();

    }

    private String[] takeImages() {
        FileHelper helper = new FileHelper();
        return helper.getFolderFiles(FileHelper.IMAGES_FILE);
    }
}
