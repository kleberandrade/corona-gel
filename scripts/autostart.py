#!/usr/bin/env python3
"""Manage only Corona Gel's block in a user's Labwc desktop autostart file."""
import argparse
import os
from pathlib import Path
import re
import shlex
import shutil
import stat
import subprocess
import sys
import tempfile

BEGIN = '# BEGIN CORONA GEL AUTOSTART'
END = '# END CORONA GEL AUTOSTART'


def read_config(path):
    if path.is_symlink():
        raise ValueError('Refusing to replace a symlinked Labwc autostart file')
    return path.read_text() if path.exists() else '#!/bin/sh\n'


def without_managed_block(text):
    counts = (text.count(BEGIN), text.count(END))
    if counts == (0, 0):
        return text
    if counts != (1, 1):
        raise ValueError('Malformed or duplicate Corona Gel autostart markers; inspect the file')
    return remove_block(text)


def remove_block(text):
    before, start, rest = text.partition('\n' + BEGIN + '\n')
    _, end, after = rest.partition(END + '\n')
    if not start or not end:
        raise ValueError('Malformed Corona Gel autostart block; inspect the file')
    return before + after


def installed_app(path):
    for name in ('run.sh', 'corona-gel.jar'):
        if not (path / name).is_file():
            raise ValueError('Install the application first; missing ' + str(path / name))


def java21(candidate):
    executable = candidate or shutil.which('java')
    if not executable:
        raise ValueError('Java 21 not found; use --java /absolute/path/to/java')
    return verify_java(Path(executable).resolve())


def verify_java(path):
    result = subprocess.run([str(path), '-version'], capture_output=True, text=True, timeout=15)
    if result.returncode or not re.search(r'\bversion "21(?:\.|\")', result.stdout + result.stderr):
        raise ValueError('Select a Java 21 runtime with --java /absolute/path/to/java')
    return path


def atomic_write(path, text, mode):
    path.parent.mkdir(parents=True, exist_ok=True)
    descriptor, name = tempfile.mkstemp(prefix='.corona-gel-', dir=path.parent)
    temporary = Path(name)
    try:
        with os.fdopen(descriptor, 'w') as output:
            output.write(text)
        temporary.chmod(mode)
        temporary.replace(path)
    finally:
        temporary.unlink(missing_ok=True)


def launcher(app, java):
    return f'''#!/bin/sh
# Managed by Corona Gel autostart.py
set -eu
APP_DIR={shlex.quote(str(app))}
JAVA_BIN={shlex.quote(str(java))}
STATE_DIR="${{XDG_STATE_HOME:-$HOME/.local/state}}/corona-gel"
mkdir -p "$STATE_DIR"
exec >> "$STATE_DIR/autostart.log" 2>&1
printf '\\nCorona Gel desktop start: %s\\n' "$(date -Iseconds)"
exec flock --nonblock --no-fork "$STATE_DIR/session.lock" "$JAVA_BIN" \\
    "-Dcoronagel.data=$APP_DIR/data" -Dcoronagel.hardware=true \\
    -jar "$APP_DIR/corona-gel.jar"
'''


def enable(path, base, app, java):
    installed_app(app)
    runtime = java21(java)
    launch = app / 'autostart.sh'
    check_launcher(launch)
    atomic_write(launch, launcher(app, runtime), 0o700)
    block = '\n' + BEGIN + '\n' + shlex.quote(str(launch)) + ' &\n' + END + '\n'
    mode = stat.S_IMODE(path.stat().st_mode) if path.exists() else 0o700
    atomic_write(path, base + block, mode | stat.S_IXUSR)
    print('enabled: hardware starts at the next Labwc desktop login; Java=' + str(runtime))


def check_launcher(path):
    if path.exists() and '# Managed by Corona Gel autostart.py' not in path.read_text():
        raise ValueError('Refusing to overwrite an unmanaged launcher: ' + str(path))


def disable(path, original, base):
    if original != base:
        atomic_write(path, base, stat.S_IMODE(path.stat().st_mode))
    print('disabled: future desktop starts removed; close the current window separately')


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('action', choices=('enable', 'disable', 'status'))
    parser.add_argument('--app-dir', type=Path, default=Path.home() / '.local/share/corona-gel')
    parser.add_argument('--java', help='Java 21 executable to pin for automatic startup')
    args = parser.parse_args()
    config = Path(os.environ.get('XDG_CONFIG_HOME', Path.home() / '.config')) / 'labwc/autostart'
    original = read_config(config)
    base = without_managed_block(original)
    perform(args, config, original, base)


def perform(args, config, original, base):
    if args.action == 'enable':
        enable(config, base, args.app_dir.resolve(), args.java)
    elif args.action == 'disable':
        disable(config, original, base)
    else:
        print('enabled' if original != base else 'disabled')


if __name__ == '__main__':
    try:
        main()
    except (OSError, ValueError, subprocess.SubprocessError) as failure:
        print('autostart: ' + str(failure), file=sys.stderr)
        sys.exit(1)
