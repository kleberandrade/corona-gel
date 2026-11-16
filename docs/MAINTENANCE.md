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

## Publication and default-branch migration — 2026-11-16

The user subsequently authorized pushing the three modernization commits, changing
`master` to `main`, and following GitHub Actions through a successful run, including
fixes if needed. The original no-push boundary above describes the initial phase.
The CI branch filter and README badge/usage references were changed to `main`;
historical baseline references remain unchanged. The GitHub branch rename preserves
repository history and updates the default branch through the GitHub API.

Remote synchronization confirmed no intervening commits before publication. GitHub
confirmed `main` as the default branch, and stale `origin/master` tracking was
pruned. [Java CI run 43800805976](https://github.com/kleberandrade/corona-gel/actions/runs/43800805976) passed at
`6cc2fc1` on its first execution; no application/CI correction was required. README
and validation notes were updated to distinguish this remote result from the
initial local-only report. The final documentation commit is published and monitored
as well, leaving no authorized publication step pending.

## Survivor review and README illustrations — 2026-11-16

- Re-read all three survivors. Corrected the prior clock-origin equivalence claim:
  the zero-initialized release baseline makes the addition mutant observable.
- Wrote the new behavior tests first and observed missing-constructor failures.
  Added package-private timer/clock/executor injection while preserving public
  defaults: Swing Timer, System.nanoTime and the real Raspberry Pi GPIO adapter.
- Verified complete scheduled carousel cycles, pause/release restart, short initial
  release and negative/nonzero nanoTime origins. Mutator now kills both real gaps;
  the remaining initial-delay phase change is documented, without hiding it.
- Clean verification: 24 tests. CRAP maximum 6; dryer found no duplicates at 0.8.
  Full-source mutation: 49 killed, 1 contract-equivalent survivor, 26 uncovered.
- Generated a product-concept PNG with the built-in image tool, inspected the image,
  and saved it under docs/images with its exact prompt and provenance. Embedded both
  the concept and the editable electronic schematic directly in the English README.
- Kept Raspberry Pi 3B, Java/Swing, original pin mapping and output polarity. Added
  Pi setup/runtime requirements and clarified that the MOSFET reference is optional
  for a correctly driven compatible relay installation. Checked primary Pi4J/GitHub
  documentation and the aarch64/armhf libraries embedded in Pi4J's runtime JAR.
- Expanded CI to Linux x64 and ARM64. This validates CPU/runtime paths without
  claiming Raspberry Pi OS, physical GPIO, pump or interactive-desktop validation.
- User authorized push to main and monitoring CI to completion for this change.

## Pi installation procedure and actual screen content — 2026-11-16

- Inspected the preserved Fritzing image: component models, coil/module electrical
  characteristics and pinouts cannot be reliably identified from it. Asked for
  model information; the user authorized choosing recommended compatible parts.
- Chose the active/current Pololu digital distance sensor v2 #5552 (3.3 V supply,
  active-low OUT) and Adafruit STEMMA non-latching mini relay #4409 (3.3 V signal,
  active-high, integrated driver). Consulted manufacturer pins/specifications and
  avoided the discontinued Sharp carrier as a new-design recommendation.
- Added a separate relay assembly, net list and editable connection diagram, keeping
  the original GPIO17/27 contract. Retained the prior MOSFET alternative and legacy
  drawings without mixing their BOMs. Documented unloaded testing before pump wiring.
- Added Trixie 64-bit desktop / Java 21 installation commands, GPIO-access checks,
  expected sensor/relay behavior, troubleshooting and explicit physical-test limits.
- Added portrait demo generation and a reproducible headless renderer of the actual
  CarouselSlider. Captured its real demo slide on the EDT, then used the built-in
  image edit tool to insert that source into a sibling totem concept image. Saved
  original render, exact edit prompt and provenance; no Pi/physical screen capture
  or invented UI is claimed. Added both example modes and preview capture to CI.

## Explicit Labwc hardware autostart and illustrated demo — 2026-11-16

- Read the official Raspberry Pi Labwc kiosk and boot/auto-login documentation.
  Chose session startup so Swing inherits the real display session; no root service,
  DISPLAY guessing or automatic crash/restart loop is introduced.
- Wrote failing CLI tests before implementing a Python standard-library helper.
  Enable validates the installed application and Java 21 before writing a private
  launcher and one managed Labwc block. Disable removes only that block; status,
  idempotence, symlink/malformed-file checks and existing commands are covered.
- Pinned the selected Java executable, enabled real GPIO explicitly, redirected logs
  to the user's state directory and used Linux flock for duplicate automatic starts.
  Configuration never launches equipment. Installer copies the helper but leaves
  activation explicit; OS desktop auto-login remains an OS configuration step.
- Ran 13 autostart tests on macOS: 11 pass, 2 Linux-only launcher/lock cases skipped.
  Added the complete suite to both Linux CI jobs. Used temporary coverage tooling
  to measure CLI subprocesses; corrected report placement for crapper's LCOV reader.
  Python manager statement coverage is 100%, combined branch coverage 99%.
- Ran mutator for the new manager. Three survivors revealed weak diagnostic checks;
  asserted the actual user-facing missing-install/unmanaged-launcher messages and
  absence of tracebacks. The final manager result is 45 killed / 0 survived.
- Replaced plain demo titles with original illustrated Java2D slides (hand, droplet,
  hierarchy and three steps), preserving default landscape and optional portrait.
  Captured the actual Swing component and used the built-in image editor only to
  insert that render into the retained totem enclosure. Saved a sibling concept PNG,
  source preview and exact prompt. No physical Pi boot or GPIO test was performed.

- Dryer found identical portrait/landscape instruction-layout functions. Consolidated
  the layout data, then proved all six generated PNGs byte-identical. Final dryer
  result has no candidates at 0.8; maximum reported CRAP is 6 with measured coverage.

## v2.0.0 release preparation — 2026-11-16

- Inspected GitHub releases/tags: only v1.0-alpha existed; kept the declared Maven
  2.0.0 version for the new major release, preserving the old release/tag.
- Wrote failing archive/checksum tests, then added a standard-library packager that
  stages the verified installer output and portrait demo media with licenses and a
  binary-specific README. Required runtime JARs/notices remain unchanged.
- Normalized archive ownership/modes/timestamps and gzip headers. Tested complete
  contents, executable launcher, reproducibility after input mtime changes, actual
  checksum bytes and link rejection. This is archive reproducibility for identical
  input files, not a cross-OS font-rendering equivalence claim.
- Added main-CI packaging smoke checks on x64/ARM64 and a tag release workflow on
  ARM64. It checks version/notes, builds/tests, verifies checksum/extracted console
  execution, uploads assets to a draft, then publishes the complete GitHub release.
- Wrote English release notes and installation/migration instructions with explicit
  Java 21 requirements, safe simulation default and unperformed physical-test limits.

- Local release checks passed: 24 Java tests; 20 Python tests with the two Linux-only
  flock cases skipped on macOS. The packaged console runs after extraction, launcher
  syntax and checksum verify, and every runtime dependency is byte-identical to the
  verified build. Added full staging/idempotence and failure-propagation cases after
  mutation analysis exposed gaps; final packaging scope is 26 killed / 0 survived /
  2 uncovered CLI-error-handler sites. Maximum CRAP remained 6 and dryer found no
  candidates at 0.8 before the expanded tests. Physical validation remains unclaimed.
