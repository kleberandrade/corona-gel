# Automatic startup on Raspberry Pi 3B

This opt-in setup starts Corona Gel with **the real sensor and relay enabled** when
the user's **Labwc graphical desktop session** starts. Raspberry Pi OS Trixie Desktop
uses Labwc by default. Automatic desktop login is required to reach that session
without a keyboard after powering on. The helper does not alter login/security or
system-wide desktop settings itself.

The GPIO contract stays unchanged: BCM17 active-low sensor, BCM27 active-high relay
interface, 1.5-second pulse and 4-second cooldown. Complete the
[unloaded relay tests first](RASPBERRY_PI.md#step-by-step-installation-and-relay-test).
The app must not run as root. The physical Pi boot/relay behavior has not been tested
by the agent; host/CI tests use fake Java executables and no GPIO.

## Install or update

Run these commands **on the Pi**, as the desktop user, after the normal Java 21 and
GPIO-permission setup. First disable any old 2020 launcher entries after reviewing
[migration](MIGRATION.md); two separate old/new startup mechanisms must not compete.

```sh
cd ~/corona-gel
git pull --ff-only
./mvnw --batch-mode --no-transfer-progress clean verify
java -Djava.awt.headless=true examples/GenerateImages.java data --portrait
./install.sh "$HOME/.local/share/corona-gel"
cp -R data/figures/. "$HOME/.local/share/corona-gel/data/figures/"
```

The demo generator overwrites its three demo filenames. Keep your own media or back
it up before regenerating. Omit `--portrait` for a landscape display. The installer
copies `autostart.py` but does not enable it automatically or start equipment.
Python 3 and util-linux `flock` must be installed (provided by the Pi OS baseline).

## Enable hardware startup

Choose the Java 21 executable explicitly, so a different Java selected by a future
shell/desktop environment does not unexpectedly change the runtime:

```sh
python3 "$HOME/.local/share/corona-gel/autostart.py" enable \
  --java /usr/lib/jvm/java-21-openjdk-arm64/bin/java
python3 "$HOME/.local/share/corona-gel/autostart.py" status
```

For a custom installation prefix, also pass `--app-dir /absolute/path/to/corona-app`.
The helper checks the installed JAR/launcher and runs only `java -version` during
configuration. **Enable does not immediately launch or energize GPIO.**

It creates a private `autostart.sh` inside the installation and manages one clearly
marked block in `${XDG_CONFIG_HOME:-~/.config}/labwc/autostart`. Other desktop
commands remain intact. Repeating enable updates that block without duplicating it.
Malformed marker blocks, symlinked configuration files and unrelated existing
launchers are rejected rather than overwritten. This is a Labwc setup; a custom
X11/other desktop needs its own session-start mechanism.

## Start automatically after power-on

Open the Pi's configuration tool:

```sh
sudo raspi-config
```

Select boot to the **desktop** and enable **desktop automatic login** for the same
user who enabled Corona Gel. Menu wording varies by OS version; the graphical
Control Centre also provides Desktop Auto Login. Then reboot:

```sh
sudo reboot
```

After the desktop appears, Corona Gel should open fullscreen with
`-Dcoronagel.hardware=true`. Start with the pump disconnected: confirm that the relay
is initially off, one stable hand detection produces one pulse, held detection does
not retrigger, and shutdown releases the relay. Record those actual Pi results.
The user/session must already have GPIO access; the launcher does not bypass it.

## Disable automatic startup

From a terminal or SSH session **as the same user**:

```sh
python3 "$HOME/.local/share/corona-gel/autostart.py" disable
python3 "$HOME/.local/share/corona-gel/autostart.py" status
```

Disable removes only Corona Gel's block. It preserves media, installation, logs and
other Labwc commands. It prevents future automatic starts; **it does not stop an
already running application**. Close the window with Alt+F4 to stop the current app,
or reboot after disabling. This helper does not turn desktop automatic login off;
use `raspi-config`/Control Centre if you also want the OS to require a password.

## Logs and recovery

```sh
tail -n 100 "${XDG_STATE_HOME:-$HOME/.local/state}/corona-gel/autostart.log"
```

The automatic launcher pins Java 21, selects the installation's `data/` directory,
enables hardware and uses a nonblocking process lock to prevent duplicate automatic
instances. The lock is released when Java exits; an existing lock file by itself is
not a stale-running-process error. Close the automatic instance before manually
running the JAR: direct/manual launches do not participate in this launcher lock.

There is no crash/restart loop: an I/O failure remains stopped until inspected and
manually restarted or a new desktop session begins. This prevents an error from
silently retriggering equipment through repeated automatic restarts. A hung JVM or
welded contact still requires an independent physical disconnect/cutoff.

If nothing opens, check desktop auto-login, the managed Labwc block, selected Java,
GPIO permissions, whether another automatic instance is holding the lock, and the
log above. A console-only boot or plain SSH session does not provide a GUI session.

## Validation and references

CLI tests cover enable/disable/status, preservation/idempotence, malformed/symlink
configuration, Java selection, missing installation, permissions and quoted paths.
Linux CI additionally executes the generated launcher with fake Java to check real
hardware flags/media arguments and single-instance behavior. It never starts GPIO.
No desktop autostart setting was enabled on the development Mac.

- [Official Raspberry Pi kiosk startup using Labwc](https://www.raspberrypi.com/tutorials/how-to-use-a-raspberry-pi-in-kiosk-mode/).
- [Official Raspberry Pi boot and desktop auto-login settings](https://www.raspberrypi.com/documentation/computers/configuration.html).
