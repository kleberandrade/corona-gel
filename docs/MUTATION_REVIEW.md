# Review of the three surviving mutants

Reviewed on 2026-11-16 against the initial snapshot at `3095416`. The updated
production constructors still create Swing timers, a single scheduled executor,
`System.nanoTime`, and the same real Pi4J adapter. Package-private injection points
allow tests to drive those scheduling boundaries without wall-clock sleeps or GPIO.

## 1. Carousel initial delay: subtraction replaced with addition

Expression: `timer.setInitialDelay(changeImageTime - fadeOutTime)`.

**Real test gap, now killed.** For a 7,000 ms period and 500 ms transition, the first
animation callback must be scheduled after 6,500 ms, followed by the 500 ms movement.
The mutant waits 7,500 ms and finishes at 8,000 ms. Old tests called animation
callbacks directly, bypassing the configured initial delay.

`scheduledCarouselCompletesEachTransitionAtTheConfiguredPeriod` advances a virtual
Swing timer queue through two complete cycles and checks the rendered slide at
7,000 and 14,000 ms. `detectionPausesScheduledSlidesAndReleaseRestartsACompletePeriod`
checks the sensor image while paused and a full 7,000 ms cycle after release. The
fake timers honor the real Timer initial-delay/repeat settings, and call the real
ActionListeners on the EDT. They do not sleep or assert private fields. The real
Swing Timer remains the default outside tests.

## 2. Monotonic origin: subtraction replaced with addition

Expression: `TimeUnit.NANOSECONDS.toMillis(nanoTime.getAsLong() - origin)`.

**Real test gap, now killed. The earlier equivalence classification was wrong.**
A constant shift is not harmless here: the controller starts with a release baseline
at zero, so a positive shifted first timestamp can arm before a genuine 50 ms initial
release. A negative shifted timestamp can prevent initial arming. `nanoTime` has an
arbitrary origin, so neither a zero nor a positive absolute value may be assumed.

`aShortInitialReleaseCannotArmAtANonzeroClockOrigin` starts at +5,000,000,000 ns and
presents detection after only 40 ms of release. After stable detection the output
must remain off; the addition mutant enables it.

`startupReleaseAndPulseUseElapsedTimeEvenWithANegativeNanoTimeOrigin` starts at
−5,000,000,000 ns, releases for 50 ms and then detects for 50 ms. It checks that the
pulse starts and ends at its elapsed deadline; the addition mutant fails to arm.
The scheduler callback is exercised with an injected monotonic clock, not by calling
the pure policy directly. Production still uses `System.nanoTime`.

## 3. First scheduling delay: zero replaced with one millisecond

Expression: the initial `0` argument of `scheduleAtFixedRate(..., 0, 10, MILLISECONDS)`.

**Equivalent under this application's timing contract; intentionally retained in
the raw survivor count.** Startup is asynchronous, and no exact first-poll instant
or scheduler phase is promised. Requesting the first sample after 0 ms or 1 ms
changes only the requested startup phase. It leaves the 10 ms sampling period,
50 ms stability rules, relative-time pulse/cooldown policy and shutdown behavior
intact. Linux/JVM scheduling can delay either callback; neither version provides
hard real-time response. A test requiring the first read at an exact wall-clock
instant would strengthen the public contract merely to kill this mutant.

This is **contract equivalence**, not an assertion that event timestamps are
bit-for-bit equal or that every timing consumer would consider the change harmless.
The complete existing and new behavior suite passes for this mutant. The actual
production delay remains 0 ms; no suppression or source rewrite hides the survivor.
Revisit this classification if the product gains an exact first-sample deadline.

## Measured result and remaining boundaries

Clean build: **24 tests**, zero failures/errors/skips. Full-source mutation result:
**49 killed, 1 survived, 26 uncovered**, 76 sites total; tool score 98.0% of executed
sites. The pure policy remains 13 killed / 0 survived / 0 uncovered. Both real gaps
above are killed in the recorded mutator run. [Raw quality snapshot](QUALITY.txt).

The 26 uncovered sites remain visible. They mostly belong to the GPIO adapter,
fullscreen window/cursor and other paths not exercised on this host. Virtual timers
validate schedule configuration and observable animation behavior, not Swing event
latency on a loaded Pi. Real Raspberry Pi GPIO, relay/pump, electrical behavior and
interactive display checks have not been performed here.

## Primary API references

- [Java 21 System.nanoTime: arbitrary origin and elapsed differences](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/System.html#nanoTime()).
- [Java 21 ScheduledExecutorService: relative initial delays and periods](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ScheduledExecutorService.html).

The equivalence decision in case 3 is a project-contract judgment, not a claim that
the Java API defines zero and one millisecond as identical.
