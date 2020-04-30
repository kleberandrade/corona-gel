package com.kleberandrade.coronagel.sensors;

import com.kleberandrade.coronagel.helpers.LogHelper;
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
 * @author Kleber de Oliveira Andrade
 */
public class SensorManager implements GpioPinListenerDigital {

    private static final int RELE_TIME = 2000;

    private final GpioController gpio;
    private final GpioPinDigitalOutput outPin;
    private final GpioPinDigitalInput inPin;
    private boolean outOpened = false;

    public SensorManager() {
        LogHelper.write("Configuring sensors");
        gpio = GpioFactory.getInstance();

        inPin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_16);
        gpio.addListener(this, inPin);

        outPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_15, "RELE", PinState.LOW);
    }

    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
        if (event.getState() == PinState.HIGH) {
            openRele();
        }
    }

    public void openRele() {
        if (outOpened) {
            return;
        }

        try {
            outOpened = true;
            LogHelper.write("Open alcohol outlet in gel");
            outPin.high();
            Thread.sleep(RELE_TIME);
            outPin.low();
            outOpened = false;
            LogHelper.write("Close alcohol gel exit");
        } catch (InterruptedException ex) {
            LogHelper.write(ex.getMessage());
        }
    }

    public void dispose() {
        if (gpio != null) {
            gpio.shutdown();
        }
    }
}
