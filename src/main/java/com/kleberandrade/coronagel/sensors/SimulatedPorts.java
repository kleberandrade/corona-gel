package com.kleberandrade.coronagel.sensors;

/** No GPIO context or native library is created. */
public final class SimulatedPorts implements SensorManager.Ports {
    private volatile boolean detected;
    public void detect(boolean value) { detected = value; }
    @Override public boolean detected() { return detected; }
    @Override public void setActive(boolean active) { }
    @Override public void close() { }
}
