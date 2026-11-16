package com.kleberandrade.coronagel.views;

import com.kleberandrade.coronagel.control.Dispenser;

/** Deterministic headless example; never initializes GPIO. */
public final class MainConsole {
    public static void main(String[] args) {
        var dispenser = new Dispenser();
        long[] times = {0, 50, 60, 110, 1609, 1610, 5610, 5660};
        boolean[] detected = {false, false, true, true, true, true, false, false};
        for (int i = 0; i < times.length; i++) {
            System.out.printf("%d ms: output=%s%n", times[i], dispenser.update(times[i], detected[i]));
        }
    }
}
