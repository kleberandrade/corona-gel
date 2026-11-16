# Raspberry Pi 3B compatibility and setup

The physical target remains the original Raspberry Pi 3B. This is a Java/Swing
application controlling the Pi GPIO, not a browser-only demo or microcontroller
firmware. Simulation is the safe desktop default; the hardware property enables the
real Pi4J GpioD adapter in the same application.

## Preserved hardware contract

- Original WiringPi GPIO0 → BCM17 / physical pin 11: active-low digital sensor.
- Original WiringPi GPIO2 → BCM27 / physical pin 13: active-high driver output.
- Idle, initial and shutdown output state: LOW.
- Pulse: 1,500 ms; cooldown: 4,000 ms. Debounce and fresh-release requirements are
  intentional changes described in [migration](MIGRATION.md).
- Swing fullscreen image slides and sensor display still run on the Pi itself.

An existing relay installation can use this output contract if its interface is
3.3 V safe, active-high, correctly driven and separately powered as required by its
actual parts. No specific unidentified relay module is certified by this project.
The MOSFET reference in [hardware](../hardware/README.md) is optional, not a demand
to replace a working compatible relay interface. Never connect a bare coil to GPIO.

## Runtime baseline

Use a 64-bit Raspberry Pi OS desktop image based on Bookworm or newer, with a
**Linux ARM64 JDK 21 including java.desktop**, a display session and a user permitted
to access `/dev/gpiochip0`. This replaces the old Java 8/WiringPi environment; an
unchanged 2020 Raspbian image is not a supported runtime for the updated build.

The Pi4J GpioD release notes identify Bookworm as the required OS baseline. Its
3.0.4 runtime JAR includes both aarch64 and armhf native libraries; this project
selects ARM64 as its documented/CI-tested CPU target, rather than claiming all
32-bit Java/OS combinations. The native libraries remain embedded in their JARs.
Pi4J 3.x uses Java 21; Pi4J 4.x/Java 25 is not used here.

Install a vendor-supported Linux ARM64 JDK 21 from its official distribution, such
as [Temurin 21 releases](https://adoptium.net/temurin/releases/?version=21&os=linux&arch=aarch64).
Keep the Java major version and CPU architecture consistent. Bookworm's default
Java package may differ; do not assume a generic package install selects Java 21.

Read-only checks on the Pi:

```sh
uname -m                         # aarch64 for the documented target
getconf LONG_BIT                 # 64
java -version                    # Java 21
java --list-modules              # includes java.desktop
ls -l /dev/gpiochip0
id -nG                          # verify OS-configured device access
```

If GPIO access fails, inspect device ownership/OS group configuration. Do not bypass
it by running the entire graphical kiosk as root. Do not enable GPIO on a different
chip or change pin numbering to suppress an error.

## Build and run on the Pi

From the checkout, inside a desktop session:

```sh
./mvnw --batch-mode --no-transfer-progress clean verify
java -Djava.awt.headless=true examples/GenerateImages.java data
java -jar target/corona-gel.jar
```

The first launch is simulation. Review real wiring with pump power disconnected
before enabling hardware; verify boot/shutdown and output polarity on the actual
assembly. Then opt in explicitly:

```sh
java -Dcoronagel.hardware=true -Dcoronagel.data=/absolute/path/to/content -jar target/corona-gel.jar
```

Retain `lib/` beside the installed JAR. The [installer](../README.md#installation-and-updates)
creates the launcher without modifying your desktop autostart or display settings.

## What CI establishes

CI runs the Java 21 build, behavior tests, headless examples and installation checks
on Linux **x64 and ARM64**. An ARM64 Ubuntu runner checks the CPU/runtime path, not
Raspberry Pi GPIO, Raspberry Pi OS, a graphical Pi desktop, or the connected pump.
The physical Pi3B/relay/sensor validation remains a separate, unperformed check.

Primary documentation:

- [Pi4J GpioD provider and default gpiochip0](https://www.pi4j.com/documentation/providers/gpiod/).
- [Pi4J release notes and GpioD OS baseline](https://www.pi4j.com/about/release-notes/).
- [Pi4J Java on ARM](https://www.pi4j.com/documentation/java-for-arm/).
- [Raspberry Pi GPIO electrical specifications](https://www.raspberrypi.com/documentation/computers/raspberry-pi.html).
- [GitHub standard ARM64 hosted runners](https://docs.github.com/en/actions/reference/runners/github-hosted-runners).
