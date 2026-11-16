# Maintenance log — 2026-11-16

Scope: `kleberandrade/corona-gel` only. Starting revision: `07c67a9`, branch `master`.
The clone was clean and all historical commits identify Kléber de Oliveira Andrade;
no project-specific AGENTS.md or existing license was present. Applied `ub-mode`
and `test-master`; no delegated agents were used.

1. Cloned the previously absent repository. The sandbox could not resolve GitHub;
   authorized network access allowed cloning. Inspected source, Fritzing XML,
   distribution contents, Git history and installation scripts before choosing scope.
2. Identified a Java/Swing Raspberry Pi application, not an Arduino project. Kept
   its Java packages, entry-point names, slide animation and original authorship.
3. Replaced Java 8/NetBeans Ant and absolute Windows library paths with Java 21,
   Maven Wrapper 3.10.1 and pinned stable build/test dependencies. Selected Pi4J
   3.0.4 because the 4.x series requires Java 25. Consulted upstream release notes
   and Maven Central metadata; excluded alpha/beta/milestone releases. Pinned the
   wrapper archive SHA-256 after checking the upstream SHA-512.
4. Moved code to standard main/test roots. Removed committed application binaries,
   obsolete snapshot/example JARs and IDE-generated build metadata. Kept historical
   circuit files unchanged under `hardware/legacy` with third-party scope noted.
5. Wrote timing-policy tests first; observed the missing-class failure separately
   from the expected old Pi4J API migration errors. Implemented the pure controller
   and observed green. Added behavioral boundary cases after mutation survivors.
6. Replaced duplicated blocking relay loops with serialized monotonic-time polling,
   debounced release/detection, bounded software pulse, cooldown, fresh release and
   failure-latched off/close attempts. Added fake-port failure/shutdown tests.
7. Reproduced missing-directory and invalid-image failures, then fixed media
   discovery. Added headless Swing rendering/transition tests; kept updates on the
   EDT, cached slides, reset interrupted animation, fixed integer scaling and removed
   redundant first-paint state/dead position assignments. The console entry point
   now provides a deterministic, GPIO-free example.
8. Added simulation by default and explicit real-hardware opt-in, preserving the
   BCM equivalent of the original WiringPi pins. Startup GPIO failures clean up the
   display timer; GPIO-context initialization failures shut down the context.
9. Replaced privileged, destructive OS/autostart edits and hidden pulls with local
   installation/update scripts. Verified paths containing spaces and preservation
   of existing media. Added original executable Java2D demo media generation.
10. Consulted Raspberry Pi, Pi4J, Adafruit, Infineon and Vishay primary documentation.
    Specified an unbuilt 3.3 V sensor / externally powered 5 V MOSFET driver reference,
    BOM, CSV net list and SVG circuit. Rendered and inspected the SVG, corrected
    overlap, and retained the historical drawing separately. No equipment was driven.
11. Added English README, migration, examples, actual-version/license badges, CI
    targeting the existing `master` branch, MIT for original work and third-party
    notices. CI workflow publication/execution remains pending a later push.
12. Applied and re-read the GitHub About description using the existing authorized
    credential, without printing credentials: “Java Swing kiosk for Raspberry Pi
    with image slides, digital hand detection, and timed dispenser control.”
13. Ran clean build/tests, console/media/install examples, local documentation links,
    shell syntax, XML checks, CRAP, duplication and mutation analysis. See
    [validation evidence](VALIDATION.md) for measured scope, gaps and exact results.
14. Normalized wrapper text using Git attributes (LF in Git, CRLF for the Windows
    launcher checkout), preserving upstream license headers.
15. Prepared local commits only. No push, deployment, package publication, system
    autostart changes or physical/clinical validation was authorized or performed.

## Terminology

- **Detected:** digital input is low; it is not a measured distance.
- **Pulse:** software requests active output for 1,500 ms.
- **Cooldown:** 4,000 ms after off is requested; input cannot start another pulse.
- **Release:** stable high input, required initially and again after cooldown.
- **Ports:** hardware/simulation boundary; it reads detection and writes output.

## Remaining boundary

A running JVM and functioning native GPIO backend are required to enforce timing.
Disconnected-high input cannot be distinguished from idle. Hardware, pump/fluids,
operating-system permissions, real kiosk display and electrical behavior need checks
on the actual assembly. These are documented validation limits, not claimed results.
