# Third-party notices

The MIT license covers the original project source and new documentation. It does
not replace dependency licenses, manufacturer documentation terms, or third-party
Fritzing part artwork.

- Pi4J 3.0.4 (core, Raspberry Pi/GpioD plugins, native GpioD library): Apache-2.0.
  Each runtime JAR retains its LICENSE.txt and NOTICE.txt files. See
  [Pi4J license](https://www.pi4j.com/about/license/).
- SLF4J API and Simple: MIT. See [SLF4J licensing](https://www.slf4j.org/license.html).
- JUnit Jupiter and its test-only platform dependencies: EPL-2.0 / accompanying
  notices; [JUnit license](https://github.com/junit-team/junit5/blob/main/LICENSE.md).
- Maven Wrapper scripts: Apache-2.0 headers retained verbatim.
- `hardware/legacy/eletronic.fzz` and `.png` are unchanged historical project
  artifacts. The Fritzing file references bundled parts, including SparkFun parts;
  no new license is asserted over third-party part designs. They are not the
  recommended wiring guide for the updated driver.

Old Pi4J snapshot/example/AbsoluteLayout binaries were removed, not relicensed.
Runtime dependencies are copied intact into `target/lib`; include their notices
when redistributing an installation. Manufacturer datasheets are linked, not copied.
