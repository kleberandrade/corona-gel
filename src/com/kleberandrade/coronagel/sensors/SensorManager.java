package com.kleberandrade.coronagel.sensors;

import com.kleberandrade.coronagel.helpers.LogHelper;
import com.kleberandrade.coronagel.interfaces.HandDetectionListener;
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
    private HandDetectionListener detectionListener;

    public SensorManager(HandDetectionListener detectionListener) {
        LogHelper.write("Configuring sensors");
        gpio = GpioFactory.getInstance();

        inPin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_00);
        gpio.addListener(this, inPin);

        outPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "RELE", PinState.LOW);
        
        this.detectionListener = detectionListener;
    }

    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
        if (event.getState().isLow()) {
            openRele();
        }
    }

    public void openRele() {
        if (outOpened) {
            return;
        }

        try {
            detectionListener.turnOn();
            Thread.sleep(100);
            outOpened = true;
            outPin.high();            
            LogHelper.write("Opening alcohol gel");
            Thread.sleep(RELE_TIME);
            LogHelper.write("Close alcohol gel");
            
            outPin.low();
            Thread.sleep(RELE_DELAY);
            outOpened = false;
            detectionListener.turnOff();
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
