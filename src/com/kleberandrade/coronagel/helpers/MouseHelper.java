/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kleberandrade.coronagel.helpers;

import java.awt.Cursor;
import java.awt.Point;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

/**
 *
 * @author Kleber de Oliveira Andrade
 */
public class MouseHelper {

    public static void hideCursor(JFrame frame) {
        ImageIcon emptyIcon = new ImageIcon(new byte[0]);
        Cursor invisibleCursor = frame.getToolkit().createCustomCursor(emptyIcon.getImage(), new Point(0, 0), "Invisible");
        frame.setCursor(invisibleCursor);
    }
}
