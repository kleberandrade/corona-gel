import os
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import time
import unittest

ROOT = Path(__file__).absolute().parents[1]
SCRIPT = ROOT / 'scripts' / 'autostart.py'

class AutostartTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.app = self.root / "app space' $literal%"
        self.app.mkdir()
        (self.app / 'corona-gel.jar').touch()
        (self.app / 'run.sh').write_text('#!/bin/sh\n')
        self.config = self.root / 'config'
        self.state = self.root / 'state'
        self.target = self.config / 'labwc' / 'autostart'
        self.java = self.root / 'java 21'
        self.java.write_text('''#!/usr/bin/env python3
import json,os,pathlib,sys,time
if sys.argv[1:] == ['-version']:
 print('openjdk version "21.0.9"'); sys.exit(0)
p=pathlib.Path(os.environ['TEST_JAVA_LOG'])
with p.open('a') as out: out.write(json.dumps(sys.argv[1:])+'\\n')
if os.environ.get('TEST_JAVA_WAIT'):
 while True: time.sleep(0.05)
''')
        self.java.chmod(0o755)
        self.log = self.root / 'java-arguments.jsonl'
        self.env = dict(os.environ, XDG_CONFIG_HOME=str(self.config),
                        XDG_STATE_HOME=str(self.state), TEST_JAVA_LOG=str(self.log))

    def cli(self, action, expected=0):
        result = subprocess.run([sys.executable, str(SCRIPT), action, '--app-dir', str(self.app),
                                 '--java', str(self.java)], env=self.env, capture_output=True, text=True)
        self.assertEqual(expected, result.returncode, result.stdout + result.stderr)
        return result

    def test_enable_is_idempotent_and_disable_preserves_existing_session_commands(self):
        self.target.parent.mkdir(parents=True)
        original = '#!/bin/sh\n# user configuration\npcmanfm &\nprintf "other app"'
        self.target.write_text(original)
        self.assertIn('disabled', self.cli('status').stdout)
        self.cli('enable'); first = self.target.read_text()
        self.cli('enable'); self.assertEqual(first, self.target.read_text())
        self.assertIn('enabled', self.cli('status').stdout)
        self.assertIn(original, first)
        self.cli('disable'); self.assertEqual(original, self.target.read_text())
        self.cli('disable'); self.assertEqual(original, self.target.read_text())
        self.assertIn('disabled', self.cli('status').stdout)

    def test_enable_requires_an_installed_application_before_editing_configuration(self):
        (self.app / 'corona-gel.jar').unlink()
        result = self.cli('enable', expected=1)
        self.assertIn('Install the application first; missing', result.stderr)
        self.assertIn('corona-gel.jar', result.stderr)
        self.assertNotIn('Traceback', result.stderr)
        self.assertFalse(self.target.exists())

    def test_unsupported_java_is_rejected_before_editing_configuration(self):
        self.java.write_text('#!/bin/sh\necho \'openjdk version "17.0.1"\'\n')
        self.cli('enable', expected=1)
        self.assertFalse(self.target.exists())

    def test_malformed_managed_block_is_not_overwritten(self):
        self.target.parent.mkdir(parents=True)
        original = '#!/bin/sh\n# BEGIN CORONA GEL AUTOSTART\nunknown command\n'
        self.target.write_text(original)
        self.cli('enable', expected=1)
        self.assertEqual(original, self.target.read_text())

    def test_reversed_managed_markers_are_not_overwritten(self):
        self.target.parent.mkdir(parents=True)
        original = '#!/bin/sh\n# END CORONA GEL AUTOSTART\n# BEGIN CORONA GEL AUTOSTART\n'
        self.target.write_text(original)
        self.cli('enable', expected=1)
        self.assertEqual(original, self.target.read_text())

    def test_launcher_is_private_to_the_current_user(self):
        import stat
        self.cli('enable')
        self.assertEqual(0o700, stat.S_IMODE((self.app / 'autostart.sh').stat().st_mode))

    def test_symlink_configuration_is_not_replaced(self):
        self.target.parent.mkdir(parents=True)
        other = self.root / 'other'; other.write_text('unrelated')
        self.target.symlink_to(other)
        self.cli('enable', expected=1)
        self.assertEqual('unrelated', other.read_text())
        self.assertTrue(self.target.is_symlink())

    def test_unmanaged_launcher_is_not_overwritten(self):
        launch = self.app / 'autostart.sh'; launch.write_text('user launcher')
        result = self.cli('enable', expected=1)
        self.assertIn('Refusing to overwrite an unmanaged launcher:', result.stderr)
        self.assertNotIn('Traceback', result.stderr)
        self.assertEqual('user launcher', launch.read_text())
        self.assertFalse(self.target.exists())

    def test_disable_without_configuration_creates_nothing(self):
        self.cli('disable')
        self.assertFalse(self.target.exists())

    def test_enable_can_resolve_java_from_path(self):
        bin_dir = self.root / 'bin'; bin_dir.mkdir(); (bin_dir / 'java').symlink_to(self.java)
        env = dict(self.env, PATH=str(bin_dir) + os.pathsep + self.env['PATH'])
        result = subprocess.run([sys.executable, str(SCRIPT), 'enable', '--app-dir', str(self.app)],
                                env=env, capture_output=True, text=True)
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertTrue(self.target.exists())

    def test_missing_java_is_rejected_without_configuration(self):
        empty = self.root / 'empty-path'; empty.mkdir()
        env = dict(self.env, PATH=str(empty))
        result = subprocess.run([sys.executable, str(SCRIPT), 'enable', '--app-dir', str(self.app)],
                                env=env, capture_output=True, text=True)
        self.assertEqual(1, result.returncode)
        self.assertFalse(self.target.exists())

    @unittest.skipUnless(shutil.which('flock'), 'Linux util-linux flock required for launcher execution')
    def test_launcher_uses_hardware_media_and_pinned_java_with_quoted_paths(self):
        import json
        self.cli('enable')
        subprocess.run(['/bin/sh', str(self.app / 'autostart.sh')], env=self.env, check=True)
        args = json.loads(self.log.read_text())
        self.assertEqual(['-Dcoronagel.data=' + str(self.app / 'data'),
                          '-Dcoronagel.hardware=true', '-jar', str(self.app / 'corona-gel.jar')], args)

    @unittest.skipUnless(shutil.which('flock'), 'Linux util-linux flock required for single-instance check')
    def test_two_launches_cannot_start_two_java_instances(self):
        self.cli('enable')
        env = dict(self.env, TEST_JAVA_WAIT='1')
        first = subprocess.Popen(['/bin/sh', str(self.app / 'autostart.sh')], env=env)
        try:
            deadline = time.monotonic() + 5
            while not self.log.exists() and time.monotonic() < deadline: time.sleep(0.01)
            self.assertTrue(self.log.exists(), 'first instance did not start')
            second = subprocess.run(['/bin/sh', str(self.app / 'autostart.sh')], env=env, timeout=5)
            self.assertNotEqual(0, second.returncode)
            self.assertEqual(1, len(self.log.read_text().splitlines()))
        finally:
            first.terminate(); first.wait(timeout=5)

if __name__ == '__main__': unittest.main()
