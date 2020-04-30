/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kleberandrade.coronagel.helpers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pdjkl
 */
public class LogHelper {

    public static final String LOG_FILE = "/home/pi/Desktop/CoronaGel/log.txt";

    public static void write(String text) {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(LOG_FILE, true));
            writer.append(getCurrentLocalDateTimeStamp())
                    .append(" ")
                    .append(text)
                    .append("\n");
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(LogHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String getCurrentLocalDateTimeStamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
