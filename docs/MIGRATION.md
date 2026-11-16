# Migration from the 2020 distribution

1. Back up your existing images and installation. The old fixed media root was
   `/home/pi/Desktop/CoronaGel/figures/{slides,sensors}`. Copy those image files into
   the new installation's `data/figures/{slides,sensors}` directories, or set
   `-Dcoronagel.data` to the old `CoronaGel` directory.
2. Install a supported JDK 21 for the OS architecture, then build with `./mvnw clean
   verify`. Java 8/NetBeans Ant metadata and machine-specific Windows library paths
   were replaced by Maven's standard `src/main/java` and `src/test/java` layout.
   Java package names and both original main-class names are retained.
3. Use the new `target/corona-gel.jar` together with its `target/lib/` directory.
   Old committed `dist/` JARs were removed; never mix old Pi4J 1.x JARs with 3.x.
4. Run simulation and inspect your images before opting into hardware. `MainConsole`
   now demonstrates the same timing policy without actuating GPIO. Only the Swing
   entry point with the explicit hardware property starts real GPIO operation.
5. Old Pi4J/WiringPi GPIO0 and GPIO2 correspond to BCM17 / physical 11 and BCM27 /
   physical 13. The output remains active-high. Review the **actual driver and supply**;
   the old Fritzing file is preserved in `hardware/legacy`, not endorsed as a safe
   direct-GPIO relay wiring plan. Reference assembly parts are documented separately.
6. The old pulse and cooldown durations remain 1.5 s and 4 s. New behavior adds input
   debounce, an initial release requirement, fresh release after cooldown, serialized
   polling and off/close attempts after errors. The sensor image appears only during
   the output pulse. Stuck low input no longer retriggers without a valid release.
7. Remove old autostart entries manually after reviewing them: the 2020 installer
   appended a launcher to `~/.bashrc`, wrote `~/coronagel.sh`, and replaced LXDE
   autostart configuration. Do not delete unrelated entries. The new installer does
   not undo unknown system configuration or modify display-manager files.
8. After manual validation, optionally enable the new [Labwc autostart helper](AUTOSTART.md)
   for hardware startup in a desktop session. Disable it with the documented helper
   command; desktop auto-login remains an explicit OS setting. Configure screen blanking through your current desktop settings.
   The old unsafe `chmod 777`/LightDM editing is removed. Review and restore previous
   LightDM permissions/configuration using OS administration procedures if necessary.

There is no automatic Git pull, privileged package installation, shell-startup
modification, service deployment or physical actuation in the migration scripts.
Logs now use standard error instead of a fixed file in `/home/pi/Desktop`.
