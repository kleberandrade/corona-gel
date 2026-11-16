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

- Clean Maven `verify`: **20 tests**, zero failures/errors/skips; package and JaCoCo
  report produced. Test cases use virtual times or fake I/O; no actuator is driven.
- Policy cases: startup held low, stable release/detection, noise, exact pulse and
  cooldown boundaries, held sensor, delayed polling, and rearming.
- Runtime cases: timeout reaches the output, close during pulse, idempotent close,
  latched read/write errors, display-listener failure, idempotent scheduler start and
  rejected restart after close.
- Media cases: absent folders, non-images, directories masquerading as images,
  invalid files, filename sorting, empty kiosk, activation before paint, slide/sensor
  colors, mid-transition position, resume and successive transitions. Swing rendering
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

The pure policy's 13 mutation sites were all killed after adding missing debounce
and cooldown boundary tests. Broader mutation analysis also exercises covered Swing
and runtime code, with remaining timing equivalents, a real-cadence test gap and host coverage gaps
reported in the [final quality snapshot](QUALITY.txt): 47 killed, 3 survived and 26
uncovered sites. A surviving mutant is not automatically an
equivalent one: real scheduler cadence is not established by virtual-time tests.
Do not infer full-project mutation adequacy from the policy's result.

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
