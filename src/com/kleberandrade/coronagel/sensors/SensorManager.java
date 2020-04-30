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

    private static final int RELE_TIME = 1500;
    private static final int RELE_DELAY = 4000;

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
            LogHelper.write("Opening alcohol gel");
            outPin.high();
            Thread.sleep(RELE_TIME);
            outPin.low();
            LogHelper.write("Close alcohol gel");
            Thread.sleep(RELE_DELAY);
            outOpened = false;
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
