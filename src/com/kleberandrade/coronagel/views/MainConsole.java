/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kleberandrade.coronagel.views;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pdjkl
 */
public class MainConsole {

    public static final int RELE_TIME = 1500;
    public static final int RELE_DELAY = 4000;
    public static boolean opened = false;

    public static void main(String[] args) throws InterruptedException {

        System.out.println("Configuring sensors");
        final GpioController gpio = GpioFactory.getInstance();
        final GpioPinDigitalOutput outPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "RELE", PinState.LOW);
        final GpioPinDigitalInput inPin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_00);
        boolean outOpened = false;

        GpioPinListenerDigital listener = new GpioPinListenerDigital() {

            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                //System.out.println(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = " + event.getState());
                if (!opened && event.getState().isLow()) {
                    try {
                        opened = true;
                        System.out.println("Opening alcohol gel");
                        outPin.high();

                        Thread.sleep(RELE_TIME);

                        outPin.low();
                        System.out.println("Close alcohol gel");
                        Thread.sleep(RELE_DELAY);
                        opened = false;
                        System.out.println("Released for use");
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MainConsole.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };

        gpio.addListener(listener, inPin);

        System.out.println(" ... PRESS <CTRL-C> TO STOP THE PROGRAM.");
        while (true) {
            Thread.sleep(500);
        }
    }
}
