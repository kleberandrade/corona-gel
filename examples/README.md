# Runnable examples

Build with `./mvnw clean verify` from the repository root.

## Deterministic console simulation

```sh
java -cp target/corona-gel.jar com.kleberandrade.coronagel.views.MainConsole
```

Expected output:

```text
0 ms: output=false
50 ms: output=false
60 ms: output=false
110 ms: output=true
1609 ms: output=true
1610 ms: output=false
5610 ms: output=false
5660 ms: output=false
```

This example calls only the pure policy and uses virtual milliseconds; no sleeps,
display server, sensor, native library or actuator is involved.

## Generate media and run the kiosk

```sh
java -Djava.awt.headless=true examples/GenerateImages.java data
java -jar target/corona-gel.jar
```

The generator uses only Java2D/ImageIO and creates two original PNG slides and a
sensor image. It overwrites those three demo filenames, so choose an empty directory
if preserving existing files. Its optional argument is the content root:

```sh
java -Djava.awt.headless=true examples/GenerateImages.java /tmp/corona-demo
java -Dcoronagel.data=/tmp/corona-demo -jar target/corona-gel.jar
```

On a desktop, hold Space for at least 50 ms to trigger simulation, then release.
After cooldown, release must remain stable before another pulse can occur.
No GPIO library is initialized unless `-Dcoronagel.hardware=true` is provided.
