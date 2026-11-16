# Corona Gel Totem

[![Java 21](https://img.shields.io/badge/Java-21-007396)](https://www.pi4j.com/about/info-v3/)
[![Pi4J 3.0.4](https://img.shields.io/badge/Pi4J-3.0.4-blue)](https://www.pi4j.com/about/release-notes/)
[![License: MIT](https://img.shields.io/badge/License-MIT-green)](LICENSE)
[![Java CI](https://github.com/kleberandrade/corona-gel/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/kleberandrade/corona-gel/actions/workflows/ci.yml)

A Java/Swing image kiosk with timed dispenser control, originally created by
**Kléber de Oliveira Andrade** for a Raspberry Pi 3B. It cycles through local images,
displays a sensor image while dispensing, and controls a low-voltage driver from a
digital hand-detection input. The project name is historical: the software makes no
sanitation, dosage, disease-prevention, or medical efficacy claim.

## Requirements

- **JDK 21**, including Swing/Desktop for the kiosk; headless mode works for tests
  and the console example. Java 21 is the validated target.
- Maven **3.10.1**, downloaded automatically by the included Maven Wrapper, plus
  network access on the first build. No IDE or locally copied JARs are required.
- Real GPIO: Raspberry Pi 3B with 64-bit Raspberry Pi OS, a JDK 21 for that system,
  access to `/dev/gpiochip0`, and a reviewed driver/sensor assembly. See
  [hardware requirements and wiring](hardware/README.md).

Pi4J **3.0.4** is the latest 3.x release selected for Java 21. The
[Pi4J release notes](https://www.pi4j.com/about/release-notes/) specify Java 25 for
4.x; changing to that series is a separate runtime migration. The hardware adapter
uses GpioD, BCM17 for input and BCM27 for active-high output, on gpiochip0.
It does not use the old WiringPi backend or a pigpio daemon.

## Quick start: no hardware

```sh
git clone https://github.com/kleberandrade/corona-gel.git
cd corona-gel
./mvnw --batch-mode --no-transfer-progress clean verify
java -cp target/corona-gel.jar com.kleberandrade.coronagel.views.MainConsole
java -Djava.awt.headless=true examples/GenerateImages.java data
java -jar target/corona-gel.jar
```

The desktop must have a display server. Simulation is the **default** and never
initializes GPIO. Hold/release **Space** to simulate a detected hand; allow an initial
release before the first pulse. Close with the window manager (for example Alt+F4).
The console example prints a deterministic sequence without opening a window or
actuating hardware. [Example details](examples/README.md).

## Images and configuration

By default the application reads `data/` relative to its working directory:

- `data/figures/slides/`: supported, readable images in filename order.
- `data/figures/sensors/`: the first supported image in filename order, displayed
  during the output pulse; optional.

Use PNG or JPEG for predictable results. Missing folders show a placeholder;
unreadable/non-image files are ignored. Images are loaded at startup, stretched to
the display area, and slides animate every 7 seconds with a 500 ms vertical transition.
Restart to reload media. Use appropriately sized images; there is no image-size or
collection-size memory limit enforced by the application.

```sh
java -Dcoronagel.data=/absolute/path/to/content -jar target/corona-gel.jar
```

Keep `target/lib/` beside the JAR: the manifest points to those versioned dependencies.
Logging goes to standard error through Java logging/SLF4J; redirect it if needed.

## GPIO operation

Read [the BOM, connection list and reference circuit](hardware/README.md) before
choosing real components. The historical sensor and relay were not fully specified;
the documented MOSFET assembly is an **unbuilt reference**, not a validated retrofit.
GPIOs must never directly supply a pump or relay coil.

After electrical review, hardware is explicitly enabled with a JVM property:

```sh
java -Dcoronagel.hardware=true -Dcoronagel.data=/absolute/path/to/content -jar target/corona-gel.jar
```

Grant the runtime user only the required GPIO device permissions according to the OS
configuration. Do not run the kiosk as root to work around a configuration error.
Native GPIO is not exercised by the host tests. Other Pi models/OS architectures
need separate validation, including chip selection.

The controller samples every 10 ms using monotonic time. Its pure policy requires
50 ms of released input before arming, then 50 ms of detection. It requests one
**1,500 ms pulse**, then **4,000 ms cooldown**, then another fresh stable release.
A hand held in place or an input stuck low cannot continuously retrigger the pump.
Read/write exceptions latch the controller off and close the GPIO context; inspect
and restart the process to recover. Normal shutdown also requests off.

This is software timing on Linux, not a hard real-time guarantee. A blocked native
call, crashed/hung process, SIGKILL or failed driver can defeat shutdown. Where a
long activation is hazardous, use an independent hardware cutoff. No physical,
fluid compatibility, dosage, or clinical validation has been performed.

## Installation and updates

```sh
./install.sh /absolute/path/to/corona-app
/absolute/path/to/corona-app/run.sh
# Explicit hardware opt-in, after reviewing the circuit:
/absolute/path/to/corona-app/run.sh -Dcoronagel.hardware=true
```

The installer copies a previously built JAR/dependencies, creates media directories
and a launcher. Default destination: `~/.local/share/corona-gel`. Repeating installation
overwrites application files and preserves media. Choose a destination outside the
repository; it must not be the source `target/` directory. It installs no OS packages,
changes no permissions/system display settings, and adds no autostart entries.

Review/fetch source updates yourself, then run `./update.sh [destination]`: it builds,
tests and installs the current checkout without doing a hidden `git pull`.
For a kiosk session, configure autostart using your current desktop environment after
a manual simulation/hardware check; see [migration](docs/MIGRATION.md).

## Development and tests

```sh
./mvnw clean verify
./mvnw test
```

JUnit **6.1.3** tests cover pulse/cooldown boundaries, noise, stuck input, delayed polls,
I/O errors, output shutdown, image filtering and headless Swing rendering/transitions.
JaCoCo **0.8.16** writes a report to `target/site/jacoco/index.html`. See
[validation evidence and gaps](docs/VALIDATION.md); no coverage or passing-test badge
is asserted. CI builds on Java 21 for pushes to `main` and pull requests. The badge
links to the live GitHub Actions result; recorded validation is in the report above.

Source responsibilities:

- `src/main/java/.../control`: GPIO-independent timing policy.
- `src/main/java/.../sensors`: GPIO ports, simulation and serialized lifecycle.
- `src/main/java/.../components`, `views`, `models`, `helpers`, `interfaces`: Swing
  presentation and supporting code, preserving the original packages/entry points.
- `src/test/java`: host-side behavior tests.
- `hardware`: reference assembly, net list and preserved historical artifacts.
- `examples`, `scripts`, `.github/workflows`, `docs`: runnable media example,
  installation, CI and maintenance notes.

## License and migration

Original project code and new documentation are [MIT licensed](LICENSE).
[Third-party notices](THIRD_PARTY.md) remain applicable to dependencies and legacy
part designs. See [migration from Java 8/NetBeans](docs/MIGRATION.md) and the
[maintenance log](docs/MAINTENANCE.md) for the changes and their rationale.
