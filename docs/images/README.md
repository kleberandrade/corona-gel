# Totem concept image

`corona-gel-totem.png` was created with the built-in image generation tool on
2026-11-16 for this repository. It is a product concept, not a photograph, fabrication
plan, image of tested hardware or claim about a built original totem. No third-party
photograph or logo was used. The editable electronic schematic is separately
maintained in `hardware/wiring.svg`; generated imagery is not used for wiring.

## Final prompt

> Use case: product-mockup. Asset type: GitHub README project illustration. Create a polished realistic 3D product concept of a freestanding automatic gel dispenser totem with an integrated portrait LCD screen for a Raspberry Pi Java/Swing image kiosk. Entire totem visible in three-quarter view: stable weighted floor base, slim upright white enclosure, portrait screen above a recessed dispenser opening with nozzle, drip tray, and small unobtrusive hand sensor. Neutral studio background, soft natural studio light, restrained professional industrial design. Screen shows a simple abstract slide with geometric shapes, no medical symbols and no disease-prevention claims. No people, no invented certifications, no logos or watermark. Do not render any text. This is an illustrative concept, not a photo of a built device. Wide landscape composition suitable for a README, visually clear at small width.

## Actual application content on the concept screen

- `application-preview.png`: direct 720×1280 PNG render from the actual production
  `CarouselSlider`, executed on Swing's EDT with portrait demo media. No image AI
  produced this source screenshot. It is a **headless component render**, not a
  desktop capture or proof of operation on a Raspberry Pi.
- `corona-gel-totem-with-app.png`: built-in image-tool edit of the original concept,
  inserting that render into the LCD in perspective. No physical hardware photo is
  claimed. The unedited source render and original concept remain available.

Reproduce the screen source from the repository root after building:

```sh
java -Djava.awt.headless=true examples/GenerateImages.java /tmp/corona-preview --portrait
java -Djava.awt.headless=true -cp target/corona-gel.jar examples/CapturePreview.java /tmp/corona-preview docs/images/application-preview.png
```

The carousel displays supplied images; the simple title slide is the actual demo
content, not an invented administrative interface.

### Edit prompt

> Use case: compositing. Image 1 is the edit target: the existing freestanding gel-dispenser totem product concept. Image 2 is supporting insert content: an actual headless render of the project's Java Swing CarouselSlider component showing its supplied demo slide, a dark blue background with the exact white text 'Corona Gel Totem'. Change ONLY the active LCD screen content of Image 1 by inserting Image 2, scaled and perspective-matched to the screen's visible interior. Preserve the screenshot's exact text, colors and simple layout: do not invent buttons, dashboards, logos, medical claims, or add new UI. Preserve the totem, black screen bezel, enclosure, nozzle, sensor, drip tray, base, original background, camera angle, shadows, materials and composition unchanged. The physical totem remains an illustrative product concept; do not add any claim that it is built or tested.
