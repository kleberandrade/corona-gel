package com.kleberandrade.coronagel.sensors;
import com.kleberandrade.coronagel.interfaces.HandDetectionListener;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class SensorManagerTest {
    static class Fake implements SensorManager.Ports {
        boolean detected, active, closed, failRead, failWrite;
        final java.util.concurrent.CountDownLatch sampled = new java.util.concurrent.CountDownLatch(1);
        public boolean detected() { sampled.countDown(); if (failRead) throw new IllegalStateException("sensor disconnected"); return detected; }
        public void setActive(boolean value) { active = value; if (value && failWrite) throw new IllegalStateException("driver failed"); }
        public void close() { closed = true; }
    }
    static class Listener implements HandDetectionListener {
        boolean active;
        public void turnOn() { active = true; }
        public void turnOff() { active = false; }
    }
    private final Fake ports = new Fake();
    private final Listener listener = new Listener();
    private final SensorManager manager = new SensorManager(ports, listener);
    private void activate() {
        manager.poll(0); manager.poll(50); ports.detected = true; manager.poll(60); manager.poll(110);
    }
    @Test void activationTimeoutAndCloseReachTheOutput() {
        activate(); assertTrue(ports.active); assertTrue(listener.active);
        manager.poll(1610); assertFalse(ports.active); assertFalse(listener.active);
        manager.close(); manager.close(); assertTrue(ports.closed);
    }
    @Test void closeDuringPulseTurnsOffAndPreventsFurtherPolling() {
        activate(); manager.close(); assertFalse(ports.active);
        manager.poll(111); assertFalse(ports.active); assertFalse(listener.active);
    }
    @Test void sensorErrorStopsAndLatchesOff() {
        activate(); ports.failRead = true; manager.poll(111);
        assertFalse(ports.active); assertTrue(ports.closed);
        ports.failRead = false; manager.poll(112); assertFalse(ports.active);
    }
    @Test void outputErrorAttemptsOffAndCloses() {
        ports.failWrite = true; activate(); assertFalse(ports.active); assertTrue(ports.closed);
    }
    @Test void listenerErrorCannotLeaveTheOutputOn() {
        var broken = new SensorManager(ports, new HandDetectionListener() {
            public void turnOn() { throw new IllegalStateException("display failed"); }
            public void turnOff() { }
        });
        broken.poll(0); broken.poll(50); ports.detected = true; broken.poll(60); broken.poll(110);
        assertFalse(ports.active); assertTrue(ports.closed);
    }
    @Test void schedulerStartIsIdempotentAndCannotRestartAfterClose() throws Exception {
        try {
            manager.start(); manager.start();
            assertTrue(ports.sampled.await(2, java.util.concurrent.TimeUnit.SECONDS));
        } finally { manager.close(); }
        assertFalse(ports.active);
        assertThrows(IllegalStateException.class, manager::start);
    }
}
