package com.kleberandrade.coronagel.sensors;

import com.kleberandrade.coronagel.interfaces.HandDetectionListener;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SensorManagerClockTest {
    private static final class ManualWorker extends ScheduledThreadPoolExecutor {
        Runnable callback;
        ManualWorker() { super(1); }
        @Override public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long delay, long period, TimeUnit unit) {
            callback = command;
            return new DormantTask();
        }
    }
    private static final class DormantTask extends FutureTask<Void> implements ScheduledFuture<Void> {
        DormantTask() { super(() -> null); }
        public long getDelay(TimeUnit unit) { return 0; }
        public int compareTo(Delayed other) { return 0; }
    }
    private static final HandDetectionListener LISTENER = new HandDetectionListener() {
        public void turnOn() { }
        public void turnOff() { }
    };

    @Test void aShortInitialReleaseCannotArmAtANonzeroClockOrigin() {
        var ports = new SensorManagerTest.Fake();
        var worker = new ManualWorker();
        var clock = new AtomicLong(5_000_000_000L);
        try (var manager = new SensorManager(ports, LISTENER, worker, clock::get)) {
            manager.start(); worker.callback.run();
            clock.addAndGet(TimeUnit.MILLISECONDS.toNanos(40)); ports.detected = true; worker.callback.run();
            clock.addAndGet(TimeUnit.MILLISECONDS.toNanos(50)); worker.callback.run();
            assertFalse(ports.active, "release lasted only 40 ms, so detection must not dispense");
        }
    }

    @Test void startupReleaseAndPulseUseElapsedTimeEvenWithANegativeNanoTimeOrigin() {
        var ports = new SensorManagerTest.Fake();
        var worker = new ManualWorker();
        var clock = new AtomicLong(-5_000_000_000L);
        try (var manager = new SensorManager(ports, LISTENER, worker, clock::get)) {
            manager.start(); worker.callback.run();
            clock.addAndGet(TimeUnit.MILLISECONDS.toNanos(50)); worker.callback.run();
            clock.addAndGet(TimeUnit.MILLISECONDS.toNanos(10)); ports.detected = true; worker.callback.run();
            clock.addAndGet(TimeUnit.MILLISECONDS.toNanos(50)); worker.callback.run();
            assertTrue(ports.active);
            clock.addAndGet(TimeUnit.MILLISECONDS.toNanos(1499)); worker.callback.run(); assertTrue(ports.active);
            clock.addAndGet(TimeUnit.MILLISECONDS.toNanos(1)); worker.callback.run(); assertFalse(ports.active);
        }
    }
}
