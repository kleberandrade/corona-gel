#!/bin/sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
DEST=${1:-"$HOME/.local/share/corona-gel"}
[ -f "$ROOT/target/corona-gel.jar" ] || { echo "Build first: ./mvnw clean verify" >&2; exit 1; }
mkdir -p "$DEST/lib" "$DEST/data/figures/slides" "$DEST/data/figures/sensors"
cp "$ROOT/target/corona-gel.jar" "$DEST/corona-gel.jar"
cp "$ROOT"/target/lib/*.jar "$DEST/lib/"
cat > "$DEST/run.sh" <<'LAUNCHER'
#!/bin/sh
set -eu
APP_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
exec java "-Dcoronagel.data=$APP_DIR/data" "$@" -jar "$APP_DIR/corona-gel.jar"
LAUNCHER
chmod +x "$DEST/run.sh"
printf 'Installed to %s\nLaunch simulation: %s/run.sh\n' "$DEST" "$DEST"
