#!/usr/bin/env python3
"""Package a verified build with dependencies, demo media and installation notes."""
import argparse
from datetime import datetime
import gzip
import hashlib
import os
from pathlib import Path
import shutil
import subprocess
import sys
import tarfile
import tempfile
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
NS = {'m': 'http://maven.apache.org/POM/4.0.0'}


def metadata(root):
    pom = ET.parse(root / 'pom.xml').getroot()
    version = pom.findtext('m:version', namespaces=NS)
    timestamp = pom.findtext('m:properties/m:project.build.outputTimestamp', namespaces=NS)
    epoch = int(datetime.fromisoformat(timestamp.replace('Z', '+00:00')).timestamp())
    return version, epoch


def java_executable():
    java_home = os.environ.get('JAVA_HOME')
    return str(Path(java_home) / 'bin/java') if java_home else 'java'


def stage(root, destination, version):
    subprocess.run([str(root / 'install.sh'), str(destination)], check=True)
    subprocess.run([java_executable(), '-Djava.awt.headless=true', str(root / 'examples/GenerateImages.java'),
                    str(destination / 'data'), '--portrait'], check=True)
    for name in ('LICENSE', 'THIRD_PARTY.md'):
        shutil.copy2(root / name, destination / name)
    notes = (root / 'docs/releases/INSTALL.txt').read_text().replace('@VERSION@', version)
    (destination / 'README.txt').write_text(notes)


def member_info(bundle, path, name, epoch):
    member = bundle.gettarinfo(str(path), arcname=str(name))
    if member.issym() or member.islnk():
        raise ValueError('Release archives must not contain links: ' + str(path))
    member.uid = member.gid = 0
    member.uname = member.gname = ''
    member.mtime = epoch
    member.mode = normalized_mode(path, member.isdir())
    return member


def normalized_mode(path, directory):
    return 0o755 if directory or path.stat().st_mode & 0o111 else 0o644


def add_member(bundle, path, app, epoch):
    member = member_info(bundle, path, path.relative_to(app.parent), epoch)
    if member.isfile():
        with path.open('rb') as content:
            bundle.addfile(member, content)
    else:
        bundle.addfile(member)


def archive(app, output, epoch):
    with output.open('wb') as raw:
        with gzip.GzipFile(filename='', mode='wb', fileobj=raw, mtime=0) as compressed:
            with tarfile.open(fileobj=compressed, mode='w') as bundle:
                for path in [app, *sorted(app.rglob('*'))]:
                    add_member(bundle, path, app, epoch)


def checksum(output):
    sums = output.parent / 'SHA256SUMS'
    sums.write_text(hashlib.sha256(output.read_bytes()).hexdigest() + '  ' + output.name + '\n')
    return sums


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output-dir', type=Path, default=ROOT / 'target/release')
    args = parser.parse_args()
    version, epoch = metadata(ROOT)
    args.output_dir.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(prefix='corona-release-') as temporary:
        app = Path(temporary) / ('corona-gel-' + version)
        stage(ROOT, app, version)
        output = args.output_dir / ('corona-gel-' + version + '-linux-arm64.tar.gz')
        archive(app, output, epoch)
    print(output)
    print(checksum(output))


if __name__ == '__main__':
    try:
        main()
    except (OSError, ValueError, subprocess.SubprocessError) as failure:
        print('package-release: ' + str(failure), file=sys.stderr)
        sys.exit(1)
