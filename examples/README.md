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

The generator uses only Java2D/ImageIO and creates two illustrated PNG slides and a
sensor image, including an original hand/droplet illustration and three-step guidance. It overwrites those three demo filenames, so choose an empty directory
if preserving existing files. Its optional argument is the content root:

```sh
java -Djava.awt.headless=true examples/GenerateImages.java /tmp/corona-demo
java -Dcoronagel.data=/tmp/corona-demo -jar target/corona-gel.jar
```

On a desktop, hold Space for at least 50 ms to trigger simulation, then release.
After cooldown, release must remain stable before another pulse can occur.
No GPIO library is initialized unless `-Dcoronagel.hardware=true` is provided.

## Portrait media and a reproducible application preview

```sh
java -Djava.awt.headless=true examples/GenerateImages.java /tmp/corona-portrait --portrait
java -Djava.awt.headless=true -cp target/corona-gel.jar examples/CapturePreview.java /tmp/corona-portrait /tmp/application-preview.png
```

The optional portrait format creates 720×1280 media. The default remains landscape
1280×720. CapturePreview runs the actual CarouselSlider component on the EDT and
renders it at 720×1280, then stops its timer. It does not initialize GPIO, open a
fullscreen window or capture a physical Pi screen. It shows the first supplied
slide, so its content is media rather than a made-up control dashboard.

The README's product concept uses this saved component render as its LCD content;
image compositing is illustrative and should not be treated as a photograph of
hardware or a physical Pi validation. The source PNG is included separately.
