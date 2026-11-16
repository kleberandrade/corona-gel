package com.kleberandrade.coronagel.control;

/** Pure, monotonic-time dispenser policy. All times are milliseconds. */
public final class Dispenser {
    private enum State { RELEASE, READY, DISPENSING, COOLDOWN }
    private State state = State.RELEASE;
    private boolean sample;
    private long changedAt;
    private long deadline;

    public boolean update(long now, boolean detected) {
        if (sample != detected) {
            sample = detected;
            changedAt = now;
        }
        switch (state) {
            case RELEASE -> armWhenReleased(now);
            case READY -> dispenseWhenStable(now);
            case DISPENSING -> stopAtDeadline(now);
            case COOLDOWN -> waitForRelease(now);
        }
        return state == State.DISPENSING;
    }

    private void armWhenReleased(long now) {
        if (!sample && now - changedAt >= 50) state = State.READY;
    }

    private void dispenseWhenStable(long now) {
        if (sample && now - changedAt >= 50) {
            state = State.DISPENSING;
            deadline = now + 1500;
        }
    }

    private void stopAtDeadline(long now) {
        if (now >= deadline) {
            state = State.COOLDOWN;
            deadline = now + 4000;
        }
    }

    private void waitForRelease(long now) {
        if (now >= deadline) {
            state = State.RELEASE;
            changedAt = now;
        }
    }
}
