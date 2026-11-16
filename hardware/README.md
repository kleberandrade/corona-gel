# Hardware reference assembly

The original project runs on a Raspberry Pi 3B; it is not microcontroller firmware.
The compilable Java application in `src/main/java` is the controller. The historical
Fritzing drawing names a generic IR proximity sensor and relay but does not identify
an exact sensor model, actuator current, or a complete driver. Those files remain
unchanged in `legacy/` for provenance. Do not treat them as a validated build plan.

This **unbuilt reference assembly** uses the same active-low sensor / active-high
output contract as the software, with a specified low-voltage pump driver. It does
not claim compatibility with an unidentified relay module or its polarity.

## Proximity sensor and relay option

For the original sensor-plus-relay style of operation, see the separately documented
[Pololu #5552 / Adafruit #4409 relay assembly](relay/README.md). It includes its own
net list and unloaded test procedure. The MOSFET assembly below remains an alternative;
do not combine their output wiring or assume their component lists are interchangeable.

## Bill of materials

- Raspberry Pi 3B with 64-bit Raspberry Pi OS, microSD, HDMI display and a dedicated
  regulated Pi supply meeting the manufacturer's requirements.
- One Adafruit 2168 IR break-beam transmitter/receiver pair. Power both at 3.3 V;
  interrupt the beam with a hand. This is a reference replacement for the unidentified
  reflective sensor, not an assertion about the historical hardware.
- Q1: Infineon IRLML2502, SOT-23 adapter/PCB, rated for gate drive at 2.5 V.
- D1: Vishay SS14 flyback diode, 1 A / 40 V, SMA adapter/PCB.
- R1: 10 kΩ pull-up, R2: 1 kΩ gate resistor, R3: 100 kΩ gate-to-source pull-down;
  standard 0.25 W resistors are sufficient here.
- One 5 V DC pump selected for the dispensed fluid, maximum 0.5 A **stall** current
  within the operating envelope of this reference. The repository does not identify
  a purchasable pump or certify fluid/solvent/material compatibility.
- Separate regulated, current-limited 5 V / 1 A pump supply; F1 1 A fuse with holder;
  C1 100 µF / at least 10 V electrolytic and C2 100 nF ceramic across the fused pump rail.
- Properly rated insulated wiring, PCB, connectors, enclosed low-voltage assembly,
  and a physical pump power disconnect. Keep liquids away from the electronics.

## Connections and power

Use [the editable SVG schematic](wiring.svg) and [connection list](connections.csv).
All pin numbers in Java are **BCM**, not physical or WiringPi numbers:

- Sensor signal → BCM17, physical pin 11 (old WiringPi GPIO0).
- Driver signal → BCM27, physical pin 13 (old WiringPi GPIO2).
- Sensor supply and R1 pull-up → 3.3 V, physical pin 1.
- Common logic/driver ground → physical pin 6 and external pump supply negative.
- BCM27 → R2 → Q1 gate (pin 1); R3 joins gate to source (pin 2, ground).
- Q1 drain (pin 3) → pump negative; fused external +5 V → pump positive.
- D1 is across the pump: **band/cathode to fused +5 V**, anode to drain/pump negative.
- C1/C2 are across fused +5 V and ground; observe C1 polarity.

Do not power the pump or a relay coil from a GPIO. Do not connect the external pump
+5 V to the Pi power pins or the sensor signal. The Pi GPIO is 3.3 V only and is not
5 V tolerant. R2 limits the brief gate charging current; R3 keeps the driver off
while the Pi pin is unconfigured. This design uses an active-high MOSFET output;
active-low relay boards require a separately reviewed interface.

At 0.5 A, the MOSFET's specified maximum 0.08 Ω resistance at 2.5 V gate drive
implies approximately 0.02 W conduction dissipation; this estimate does not validate
transients, PCB layout, stall heating, component authenticity, or pump suitability.
Check those against the actual parts before energizing. The diode and fuse must
also be suitable for actual startup/stall behavior.

## Behavior and commissioning boundary

The input is pulled high; a stable low starts a pulse only after an initial stable
release. Low held indefinitely cannot cause repeated pulses. A disconnected input
usually reads high and cannot be distinguished from an idle hand sensor; wiring
that intermittently alternates valid levels is not diagnosed. Sunlight can interfere
with the break-beam sensor. No distance measurement or clinical function is implemented.

Software requests off at 1.5 s, on shutdown, and on caught I/O errors. Linux scheduling,
a hung native call/JVM, SIGKILL, output hardware failure or a welded contact can defeat
software shutdown. An independent hardware timeout/power disconnect is required where
unbounded activation would be hazardous. No hard real-time maximum is guaranteed.

Before any physical use, verify pinout, polarity, ground, disconnected-load output,
boot/shutdown states, current, flyback orientation and sensor behavior using suitable
equipment. These physical checks have **not** been performed in this modernization.
No mains switching is covered. The project makes no disease-prevention, sanitation,
dosage, material compatibility, or medical efficacy claim.

## Primary sources

- [Raspberry Pi hardware and GPIO specifications](https://www.raspberrypi.com/documentation/computers/raspberry-pi.html).
- [Adafruit break-beam wiring, polarity and ambient-light limits](https://learn.adafruit.com/ir-breakbeam-sensors/arduino).
- [Infineon IRLML2502 datasheet, pinout and 2.5 V resistance rating](https://www.infineon.com/assets/row/public/documents/24/49/infineon-irlml2502-datasheet-en.pdf).
- [Vishay SS12–SS16 datasheet, SS14 ratings and cathode marking](https://www.vishay.com/docs/88746/ss12.pdf).
- [Pi4J GpioD provider and chip selection](https://www.pi4j.com/documentation/providers/gpiod/).
