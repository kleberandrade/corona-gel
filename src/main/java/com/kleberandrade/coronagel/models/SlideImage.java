/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kleberandrade.coronagel.models;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.ImageObserver;

/**
 *
 * @author pdjkl
 */
public class SlideImage {

    private Image image;
    private ImageObserver imageObserver;
    private int x;
    private int y;
    private int width;
    private int height;

    public SlideImage(Image image, ImageObserver imageObserver) {
        this.image = image;
        this.imageObserver = imageObserver;
    }

    public SlideImage(ImageObserver imageObserver) {
        this.imageObserver = imageObserver;
    }

    public void draw(Graphics2D graphics) {
        graphics.drawImage(image, x, y, width, height, imageObserver);
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

}
