# Validation evidence

Validated locally on macOS arm64 with OpenJDK 21. No Raspberry Pi or actuator was
connected. This report separates executed host checks from hardware requirements.

## Reproduce

```sh
./mvnw --batch-mode --no-transfer-progress clean verify
java -cp target/corona-gel.jar com.kleberandrade.coronagel.views.MainConsole
java -Djava.awt.headless=true examples/GenerateImages.java /tmp/corona-demo
./install.sh "/tmp/corona gel app"
sh -n install.sh update.sh scripts/install.sh "/tmp/corona gel app/run.sh"
```

The implementation environment used an explicit Java 21 JAVA_HOME, a temporary
Maven dependency cache and a temporary wrapper download cache; no developer's global
Java configuration was changed. The wrapper was exercised with Maven 3.10.1.

## Executed checks

- Clean Maven `verify`: **24 tests**, zero failures/errors/skips; package and JaCoCo
  report produced. Test cases use virtual times or fake I/O; no actuator is driven.
- Policy cases: startup held low, stable release/detection, noise, exact pulse and
  cooldown boundaries, held sensor, delayed polling, and rearming.
- Runtime cases: timeout reaches the output, close during pulse, idempotent close,
  latched read/write errors, display-listener failure, idempotent scheduler start and
  rejected restart after close, nonzero/negative monotonic origins and a too-short
  initial release through the scheduled callback.
- Media cases: absent folders, non-images, directories masquerading as images,
  invalid files, filename sorting, empty kiosk, activation before paint, slide/sensor
  colors, mid-transition position, resume, successive transitions and complete
  scheduled cycles with virtual timers. Swing rendering
  runs headlessly on the EDT; it is not an interactive desktop-session check.
- Console example output matched the documented eight timestamp/output lines.
- Java2D generator produced the three expected PNGs without external media downloads.
- Installer succeeded in a path containing spaces and preserved existing media on
  repeat installation; shell syntax and copied runtime dependency packaging checked. Launcher argument
  quoting/default simulation were checked with a fake Java executable, without
  opening a GUI. Workflow YAML was parsed locally.
- Local Markdown links resolve; POM/SVG parse as XML; `git diff --check` passed.
- SVG was rendered with macOS Quick Look and visually inspected for legibility and
  circuit/connection-list consistency. This is drawing review, not electrical testing.
- GitHub About PATCH was followed by a GET verifying the requested English description.

## Quality tools

`crapper`, `dryer` and `mutator` were available and executed, rather than inferred.
Commands below require separately installed tools and are not Maven dependencies:

```sh
crapper --use-existing-coverage --threshold 9.99
dryer --threshold 0.8
mutator --use-existing-coverage --test-command './mvnw -o -q test' src/main/java
```

CRAP uses JaCoCo instruction coverage; all reported functions scored below 10 after
refactoring/testing. This is a complexity indicator, not proof of complete behavior.
Dryer found no duplicate candidates at similarity 0.8.

The pure policy's 13 mutation sites are all killed. The survivor review corrected
an earlier false equivalence classification for the monotonic-clock origin and
added virtual-scheduler tests for both real gaps. The measured full-source result
is **49 killed, 1 survived, 26 uncovered**; the remaining 0-to-1 ms initial-delay
mutant is equivalent under the asynchronous-startup contract, not bit-for-bit timing.
See the [case-by-case review](MUTATION_REVIEW.md) and [raw quality snapshot](QUALITY.txt).
No survivor was suppressed. Hardware/fullscreen coverage gaps remain explicit.

## Not executed

- GPIO/native-provider initialization on Raspberry Pi, device permissions or real
  sensor/actuator I/O. `Pi4jPorts` is compiled, not physically integrated here.
- Boot state, voltage/current, stall, flyback, electrical transients, pump suitability,
  fluid compatibility, hard real-time cutoff or independent hardware watchdog checks.
- Interactive fullscreen window/input, desktop autostart, screen blanking or Pi GPU
  behavior. Headless pixel checks cover rendering paths only.
- Sanitation, clinical or disease-prevention validation.

JaCoCo's complete report is generated at `target/site/jacoco/index.html`. Hardware
adapter, fullscreen window and cursor paths are intentionally unexecuted on this
host. These gaps must remain visible when assessing deployment readiness.

## Remote CI after authorized publication — 2026-11-16

The default branch was renamed from `master` to `main` through GitHub's branch-rename
API. The three original modernization commits and the branch-reference adjustment
were pushed without rewriting their history. `origin/HEAD` and local tracking now
point to `main`; the old remote branch name no longer exists.

[Java CI run 43800805976](https://github.com/kleberandrade/corona-gel/actions/runs/43800805976) completed successfully on commit
`6cc2fc16184890f450704674380bf4a8d2496167`, using the Ubuntu runner and Temurin 21.
It ran the clean Maven verification, headless console example, demo image generation
and local-installation checks. No implementation fix was needed. The documentation
update recording this result is also published and its own CI run is checked before
handoff. Hardware and interactive-desktop validation limits above still apply.

## Optional Labwc startup and illustrated media — 2026-11-16

The new autostart helper is tested in isolated temporary configuration/state/app
paths. No development-host desktop setting was enabled. Python unittest covers
preservation/idempotence, enable/disable/status, errors, Java 21 selection and file
permissions. There are 13 tests: 11 pass on the development Mac; 2 require Linux
`flock` and are explicitly skipped locally, then executed by Linux x64/ARM64 CI with
fake Java, never GPIO. The Linux-specific cases verify hardware/media arguments and
that concurrent automatic starts cannot run two Java instances.

A temporary coverage.py 7.10.7 environment measured the Python manager's statements
and branches through its CLI subprocesses (not a runtime dependency). It reported
100% statement coverage and 99% combined statement/branch coverage; the remaining
branch is the non-CLI import path. Mutator initially exposed missing assertions for
error diagnostics; after adding those, all 45 Python manager mutation sites are
killed. This scope does not replace the separate Java mutation snapshot or establish
physical autostart behavior. Java CRAP/duplication checks and image generation were
also re-run for the changed example code.

The improved portrait/landscape media and the real Swing preview are rendered with
Java2D/ImageIO and inspected separately. The totem enclosure remains illustrative.
Reboot, desktop login, real GPIO/relay/pump state and Pi display behavior still need
physical validation on the installation.

[Autostart quality snapshot](AUTOSTART_QUALITY.txt) records the measured scope.
