package com.kleberandrade.coronagel.control;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DispenserTest {
    private final Dispenser dispenser = new Dispenser();
    private void ready() { dispenser.update(0, false); dispenser.update(50, false); }
    private void trigger() { dispenser.update(60, true); dispenser.update(110, true); }

    @Test void startsOffAndRequiresReleaseBeforeArming() {
        assertFalse(dispenser.update(0, true));
        assertFalse(dispenser.update(6000, true));
        dispenser.update(6010, false);
        assertFalse(dispenser.update(6060, false));
        dispenser.update(6070, true);
        assertTrue(dispenser.update(6120, true));
    }
    @Test void rejectsShortSensorNoise() {
        ready();
        assertFalse(dispenser.update(60, true));
        assertFalse(dispenser.update(109, true));
        assertFalse(dispenser.update(110, false));
    }
    @Test void stopsAtDeadlineAndDoesNotExtendOnRepeatedDetection() {
        ready(); trigger();
        assertTrue(dispenser.update(1609, true));
        assertFalse(dispenser.update(1610, true));
        assertFalse(dispenser.update(5610, true));
        assertFalse(dispenser.update(10000, true));
    }
    @Test void requiresFreshStableReleaseAfterCooldown() {
        ready(); trigger();
        assertFalse(dispenser.update(1610, false));
        assertFalse(dispenser.update(5609, false));
        assertFalse(dispenser.update(5610, false));
        assertFalse(dispenser.update(5620, true));
        assertFalse(dispenser.update(5670, true));
        dispenser.update(5680, false); dispenser.update(5730, false);
        dispenser.update(5740, true);
        assertTrue(dispenser.update(5790, true));
    }
    @Test void delayedPollStopsWithoutAnotherPulse() {
        ready(); trigger();
        assertFalse(dispenser.update(50000, true));
        assertFalse(dispenser.update(60000, true));
    }
    @Test void heldSensorNeverArmsEvenAfterDebounce() {
        assertFalse(dispenser.update(0, true));
        assertFalse(dispenser.update(50, true));
        assertFalse(dispenser.update(100, true));
    }
    @Test void shortReleaseAtALaterTimestampCannotRearm() {
        dispenser.update(0, true); dispenser.update(1000, true);
        dispenser.update(1010, false); dispenser.update(1059, false);
        dispenser.update(1060, true);
        assertFalse(dispenser.update(1110, true));
    }
    @Test void cooldownIsNotShortenedByEarlyRelease() {
        ready(); trigger(); dispenser.update(1610, false);
        dispenser.update(1660, false); dispenser.update(1710, false);
        dispenser.update(1720, true);
        assertFalse(dispenser.update(1770, true));
    }
    @Test void cooldownExpiresAtTheExactBoundary() {
        ready(); trigger(); dispenser.update(1610, true);
        dispenser.update(5610, false); dispenser.update(5660, false);
        dispenser.update(5670, true);
        assertTrue(dispenser.update(5720, true));
    }
}
