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

## Step-by-step installation and relay test

The user authorized selecting recommended compatible parts. Use the
[Pololu #5552 + Adafruit #4409 relay assembly](../hardware/relay/README.md), which
preserves the original proximity-sensor/relay behavior. Its exact connections,
voltage levels and load limits are documented there. The old drawing does not
identify the original purchased components.

### 1. Prepare the Pi

Back up the microSD card before writing an OS. In Raspberry Pi Imager select the
Pi 3B and **Raspberry Pi OS Desktop, 64-bit, based on Trixie**. Use a desktop image,
not Lite, for the Swing window. Attach the screen, keyboard and a proper dedicated
Pi supply. The updated software does not run on an unchanged Java 8 Raspbian image.

In a terminal in the Pi desktop session:

```sh
uname -m                       # expected: aarch64
cat /etc/os-release             # this APT recipe targets Trixie
sudo apt update
sudo apt install -y git openjdk-21-jdk gpiod
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-arm64
export PATH="$JAVA_HOME/bin:$PATH"
java -version                  # expected: Java 21
java --list-modules             # should include java.desktop
```

The full JDK includes the graphical runtime. Do not mix Debian/Trixie repositories
into Bookworm to obtain Java: on an existing Bookworm installation use the official
ARM64 JDK distribution linked above, or reinstall a current 64-bit desktop image.
The environment exports affect this terminal; if another Java is selected in a new
session, select Java 21 there again before building or running the launcher.

### 2. Build and test without hardware

```sh
git clone --branch main https://github.com/kleberandrade/corona-gel.git
cd corona-gel
./mvnw --batch-mode --no-transfer-progress clean verify
java -cp target/corona-gel.jar com.kleberandrade.coronagel.views.MainConsole
java -Djava.awt.headless=true examples/GenerateImages.java data --portrait
java -jar target/corona-gel.jar
```

Omit `--portrait` for a landscape display. Use media matching the actual display
orientation: the current application stretches images to the available area.
The GUI should show demo slides; hold Space for more than 50 ms to simulate a hand.
Close with Alt+F4. This stage never initializes GPIO. The console prints the eight
expected virtual-time lines in [examples](../examples/README.md).

### 3. Wire the sensor and relay, with power off

Shut down and disconnect both supplies. Follow the
[relay connection list](../hardware/relay/connections.csv) and its
[diagram](../hardware/relay/wiring.svg). Essential connections are:

- Sensor VIN → Pi physical 1 (3.3 V); sensor GND → physical 6.
- Sensor OUT → physical 11 (BCM17). Leave EN at its default.
- Relay white Signal → physical 13 (BCM27), with 10 kΩ from Signal to GND.
- Relay red V+ → fused external +5 V; black Ground → common GND.
- External negative joins Pi GND. External positive does **not** join Pi power/GPIO.

Leave the relay's COM/NO/NC terminals without a pump or other load attached. The
module's transistor drives the coil; the GPIO only commands its signal input.
The recommended sensor is powered at 3.3 V because its HIGH output follows VIN.

### 4. Check access, then enable GPIO with no pump

Reconnect the Pi and relay supply and start the desktop. In the terminal:

```sh
ls -l /dev/gpiochip0
id -nG
gpiodetect
```

The runtime user needs the OS-configured GPIO device permissions. If the device's
group is `gpio` and your user is not a member, add only that group and log out/in:

```sh
getent group gpio
sudo usermod -aG gpio "$USER"
```

Recheck permissions after login. If the device is not assigned to that group,
inspect the OS device/udev configuration instead of applying broad chmod changes.
Do not run the graphical application as root. Do not run separate gpio readers or
writers while Pi4J owns the same lines.

Inside the checkout, in a terminal using Java 21:

```sh
java -Dcoronagel.hardware=true -Dcoronagel.data="$PWD/data" -jar target/corona-gel.jar
```

### 5. Expected unloaded behavior

1. Keep the sensing area clear during startup. The relay must be off.
2. Bring a hand within the sensor's nominal 10 cm range and hold it stable. The
   sensor indicator should show detection; the relay should activate for about
   1.5 seconds and the screen should show the sensor image.
3. Keep the hand in place. The relay must stop and must not repeat continuously.
4. Wait for the 4-second cooldown, then remove the hand fully and pause briefly
   before presenting it again. It must trigger one new pulse.
5. Close with Alt+F4 during a pulse and confirm the relay releases. Confirm that
   startup/reboot and loss of control power do not cause unintended activation.

Record the actual results on the Pi. App/CI tests cannot establish these electrical
or physical observations, and no such checks were performed by the agent.

### 6. Optional pump test and installation

Only after the unloaded tests and pump/load review pass, power everything off and
connect the reviewed low-voltage DC load through **COM and NO**. Fit the separate
pump flyback diode with its band at pump positive, as shown in the relay guide.
The board's coil diode is not pump protection. Keep a physical power disconnect
available; software cannot release a welded contact or guarantee timing after a
process/native failure. No mains switching is covered.

Install the built application to a separate directory and copy the media:

```sh
./install.sh "$HOME/.local/share/corona-gel"
cp -R data/figures/. "$HOME/.local/share/corona-gel/data/figures/"
"$HOME/.local/share/corona-gel/run.sh" -Dcoronagel.hardware=true
```

The installer does not add autostart. Validate a manual desktop-session run before
configuring it with your actual desktop environment.

### Troubleshooting

- No display / HeadlessException: launch in the Pi desktop session, not plain SSH.
- Relay always on: disconnect the load and check active-HIGH module, NO vs NC,
  common ground and startup state. This assembly does not use an active-LOW board.
- No detection: check sensor power at 3.3 V, OUT on BCM17, protective liner removal,
  target distance/reflectivity and ambient light. Check labels, not guessed pin order.
- GPIO access error: inspect gpiochip0 permissions and other processes using it.
- No second pulse: wait for cooldown, then fully remove the hand before retrying.
- Java/class version error: ensure this terminal and launcher select Java 21 ARM64.

APT package reference: [Debian Trixie OpenJDK 21 JDK, including ARM64](https://packages.debian.org/trixie/openjdk-21-jdk).
OS installation: [official Raspberry Pi OS documentation](https://www.raspberrypi.com/documentation/computers/os.html).

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
