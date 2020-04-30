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

/**
 *
 * @author pdjkl
 */
public class ConsoleMain {

    public static void main(String args[]) throws InterruptedException {

        System.out.println("<--Pi4J--> GPIO Listen Example ... started.");

        // create GPIO controller
        final GpioController gpio = GpioFactory.getInstance();
        final GpioPinDigitalOutput out = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_15, "RELE", PinState.LOW);

        // create GPIO listener
        GpioPinListenerDigital listener = new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                // display pin state on console
                System.out.println(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = " + event.getState());
                if (event.getState() == PinState.HIGH) {
                    out.high();
                } else {
                    out.low();
                }
            }
        };

        // provision gpio input pins with its internal pull down resistor enabled
        GpioPinDigitalInput pot = gpio.provisionDigitalInputPin(RaspiPin.GPIO_16);

        // create and register gpio pin listener
        gpio.addListener(listener, pot);

        System.out.println(" ... complete the GPIO circuit and see the listener feedback here in the console.");

        // keep program running until user aborts (CTRL-C)
        while (true) {
            Thread.sleep(500);
        }

        // stop all GPIO activity/threads by shutting down the GPIO controller
        // (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
        // gpio.shutdown();   <--- implement this method call if you wish to terminate the Pi4J GPIO controller
    }
}
