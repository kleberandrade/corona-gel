package com.kleberandrade.coronagel.sensors;

import com.kleberandrade.coronagel.control.Dispenser;
import com.kleberandrade.coronagel.interfaces.HandDetectionListener;
import java.util.concurrent.*;
import java.util.logging.*;

/** Serializes sensor sampling, output writes and shutdown. Never sleeps in a GPIO callback. */
public final class SensorManager implements AutoCloseable {
    public interface Ports extends AutoCloseable {
        boolean detected();
        void setActive(boolean active);
        @Override void close();
    }
    private final Ports ports;
    private final HandDetectionListener listener;
    private final Dispenser dispenser = new Dispenser();
    private final ScheduledExecutorService worker;
    private final java.util.function.LongSupplier nanoTime;
    private boolean active;
    private boolean closed;
    private ScheduledFuture<?> task;

    public SensorManager(Ports ports, HandDetectionListener listener) {
        this(ports, listener, Executors.newSingleThreadScheduledExecutor(), System::nanoTime);
    }

    SensorManager(Ports ports, HandDetectionListener listener, ScheduledExecutorService worker,
                  java.util.function.LongSupplier nanoTime) {
        this.ports = ports;
        this.listener = listener;
        this.worker = worker;
        this.nanoTime = nanoTime;
    }

    public synchronized void start() {
        if (closed) throw new IllegalStateException("Controller is closed");
        if (task != null) return;
        long origin = nanoTime.getAsLong();
        task = worker.scheduleAtFixedRate(() -> poll(TimeUnit.NANOSECONDS.toMillis(nanoTime.getAsLong() - origin)), 0, 10, TimeUnit.MILLISECONDS);
    }

    public synchronized void poll(long now) {
        if (closed) return;
        try {
            boolean next = dispenser.update(now, ports.detected());
            ports.setActive(next);
            notifyChange(next);
        } catch (RuntimeException failure) {
            Logger.getLogger(SensorManager.class.getName()).log(Level.SEVERE, "Controller stopped; restart after inspection", failure);
            close();
        }
    }

    private void notifyChange(boolean next) {
        if (active == next) return;
        active = next;
        if (next) listener.turnOn();
        else listener.turnOff();
    }

    @Override public synchronized void close() {
        if (closed) return;
        closed = true;
        worker.shutdownNow();
        try { ports.setActive(false); }
        finally {
            try { ports.close(); }
            finally { notifyChange(false); }
        }
    }

    /** Compatibility with the original window lifecycle. */
    public void dispose() { close(); }
}
