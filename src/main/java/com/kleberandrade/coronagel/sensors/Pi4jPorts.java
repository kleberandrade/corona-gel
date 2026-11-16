package com.kleberandrade.coronagel.sensors;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.*;

/** BCM numbering: original WiringPi 0/2 become BCM 17/27. Active-low input, active-high driver. */
public final class Pi4jPorts implements SensorManager.Ports {
    private final Context context;
    private final DigitalInput input;
    private final DigitalOutput output;

    public Pi4jPorts() {
        context = Pi4J.newAutoContext();
        try {
            output = context.create(DigitalOutput.newConfigBuilder(context).id("dispenser")
                    .address(27).provider("gpiod-digital-output")
                    .initial(DigitalState.LOW).shutdown(DigitalState.LOW).build());
            input = context.create(DigitalInput.newConfigBuilder(context).id("hand")
                    .address(17).provider("gpiod-digital-input").pull(PullResistance.PULL_UP).build());
        } catch (RuntimeException | Error failure) {
            context.shutdown();
            throw failure;
        }
    }
    @Override public boolean detected() { return input.isLow(); }
    @Override public void setActive(boolean active) { output.state(active ? DigitalState.HIGH : DigitalState.LOW); }
    @Override public void close() { context.shutdown(); }
}
