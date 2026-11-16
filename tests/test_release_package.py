import importlib.util
from pathlib import Path
import tarfile
import tempfile
import unittest
from unittest.mock import patch
import os
import struct
import subprocess
import sys

ROOT = Path(__file__).absolute().parents[1]
spec = importlib.util.spec_from_file_location('release_package', ROOT / 'scripts/package_release.py')
package = importlib.util.module_from_spec(spec)
spec.loader.exec_module(package)

class ReleasePackageTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.app = self.root / 'corona-gel-2.0.0'
        (self.app / 'lib').mkdir(parents=True)
        (self.app / 'data/figures/slides').mkdir(parents=True)
        (self.app / 'corona-gel.jar').write_bytes(b'application')
        (self.app / 'lib/dependency.jar').write_bytes(b'dependency with notices')
        (self.app / 'data/figures/slides/01.png').write_bytes(b'demo media')
        (self.app / 'run.sh').write_text('#!/bin/sh\nexit 0\n')
        (self.app / 'run.sh').chmod(0o755)
        (self.app / 'autostart.py').write_text('# configuration helper\n')
        (self.app / 'LICENSE').write_text('MIT\n')
        (self.app / 'README.txt').write_text('installation\n')

    def test_archive_keeps_runtime_media_licenses_and_executable_launcher(self):
        output = self.root / 'release.tar.gz'
        package.archive(self.app, output, 1794787200)
        with tarfile.open(output) as bundle:
            names = set(bundle.getnames())
            self.assertIn('corona-gel-2.0.0/corona-gel.jar', names)
            self.assertIn('corona-gel-2.0.0/lib/dependency.jar', names)
            self.assertIn('corona-gel-2.0.0/data/figures/slides/01.png', names)
            self.assertIn('corona-gel-2.0.0/autostart.py', names)
            self.assertIn('corona-gel-2.0.0/LICENSE', names)
            self.assertEqual(0o755, bundle.getmember('corona-gel-2.0.0/run.sh').mode)
            jar = bundle.getmember('corona-gel-2.0.0/corona-gel.jar')
            self.assertEqual((0, 0, 1794787200, 0o644), (jar.uid, jar.gid, jar.mtime, jar.mode))
            self.assertEqual(0, struct.unpack('<I', output.read_bytes()[4:8])[0])
            self.assertEqual(b'dependency with notices', bundle.extractfile('corona-gel-2.0.0/lib/dependency.jar').read())

    def test_archive_is_reproducible_when_input_mtimes_change(self):
        import os
        first = self.root / 'first.tar.gz'; second = self.root / 'second.tar.gz'
        package.archive(self.app, first, 1794787200)
        os.utime(self.app / 'corona-gel.jar', (100, 200))
        package.archive(self.app, second, 1794787200)
        self.assertEqual(first.read_bytes(), second.read_bytes())

    def test_checksum_references_archive_basename_and_actual_bytes(self):
        import hashlib
        output = self.root / 'release.tar.gz'; output.write_bytes(b'archive contents')
        sums = package.checksum(output)
        self.assertEqual(hashlib.sha256(output.read_bytes()).hexdigest() + '  release.tar.gz\n', sums.read_text())

    def test_symbolic_links_are_rejected(self):
        (self.app / 'outside').symlink_to(self.root / 'outside')
        with self.assertRaisesRegex(ValueError, 'link'):
            package.archive(self.app, self.root / 'release.tar.gz', 1794787200)

    def fixture_project(self):
        project = self.root / 'source'; project.mkdir()
        (project / 'docs/releases').mkdir(parents=True)
        (project / 'pom.xml').write_text('''<project xmlns="http://maven.apache.org/POM/4.0.0">
<version>2.0.0</version><properties><project.build.outputTimestamp>2026-11-16T00:00:00Z</project.build.outputTimestamp></properties></project>''')
        (project / 'LICENSE').write_text('MIT license')
        (project / 'THIRD_PARTY.md').write_text('dependency notices')
        (project / 'docs/releases/INSTALL.txt').write_text('Corona Gel @VERSION@')
        install = project / 'install.sh'
        install.write_text('#!/bin/sh\nset -eu\nmkdir -p "$1/lib"\nprintf application > "$1/corona-gel.jar"\nprintf dependency > "$1/lib/runtime.jar"\nprintf launcher > "$1/run.sh"\nchmod +x "$1/run.sh"\nprintf helper > "$1/autostart.py"\n')
        install.chmod(0o755)
        jdk = self.root / 'jdk'; (jdk / 'bin').mkdir(parents=True)
        java = jdk / 'bin/java'
        java.write_text('''#!/usr/bin/env python3
import pathlib,sys
root=pathlib.Path(sys.argv[3])
for name in ('slides/01.png','slides/02.png','sensors/01.png'):
 p=root/'figures'/name; p.parent.mkdir(parents=True,exist_ok=True); p.write_bytes(b'demo')
''')
        java.chmod(0o755)
        return project, jdk

    def test_full_packaging_uses_pom_version_and_can_be_repeated(self):
        project, jdk = self.fixture_project()
        output = self.root / 'new output/release'
        with patch.object(package, 'ROOT', project), patch.dict(os.environ, {'JAVA_HOME': str(jdk)}), \
             patch.object(sys, 'argv', ['package-release', '--output-dir', str(output)]):
            package.main()
            artifact = output / 'corona-gel-2.0.0-linux-arm64.tar.gz'
            first = artifact.read_bytes()
            package.main()
        self.assertEqual(first, artifact.read_bytes())
        with tarfile.open(artifact) as bundle:
            self.assertEqual(b'Corona Gel 2.0.0', bundle.extractfile('corona-gel-2.0.0/README.txt').read())
            self.assertEqual(b'MIT license', bundle.extractfile('corona-gel-2.0.0/LICENSE').read())
            self.assertEqual(b'demo', bundle.extractfile('corona-gel-2.0.0/data/figures/sensors/01.png').read())

    def test_failed_installer_stops_packaging(self):
        project, jdk = self.fixture_project()
        (project / 'install.sh').write_text('#!/bin/sh\nexit 7\n')
        with patch.dict(os.environ, {'JAVA_HOME': str(jdk)}), self.assertRaises(subprocess.CalledProcessError):
            package.stage(project, self.root / 'staged', '2.0.0')

    def test_failed_media_generation_stops_packaging(self):
        project, jdk = self.fixture_project()
        (jdk / 'bin/java').write_text('#!/bin/sh\nexit 4\n')
        with patch.dict(os.environ, {'JAVA_HOME': str(jdk)}), self.assertRaises(subprocess.CalledProcessError):
            package.stage(project, self.root / 'staged', '2.0.0')

if __name__ == '__main__': unittest.main()
