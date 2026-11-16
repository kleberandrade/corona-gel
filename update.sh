#!/bin/sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
cd "$ROOT"
./mvnw clean verify
exec "$ROOT/scripts/install.sh" "$@"
