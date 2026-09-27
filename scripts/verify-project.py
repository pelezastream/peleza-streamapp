#!/usr/bin/env python3
from pathlib import Path
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
required = [
    'settings.gradle','build.gradle','gradle.properties','app/build.gradle',
    'app/src/main/AndroidManifest.xml',
    'app/src/main/java/com/pelezastream/app/MainActivity.java',
    'app/src/main/java/com/pelezastream/app/SplashActivity.java',
    'app/src/main/assets/offline.html',
    'app/src/main/res/layout/activity_splash.xml',
    'app/src/main/res/drawable/peleza_mark.xml',
    'app/src/main/res/drawable/splash_background.xml',
    'app/src/main/res/mipmap/ic_launcher.xml',
    'app/src/main/res/mipmap/ic_launcher_round.xml',
    'app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml',
    'app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml',
    '.github/workflows/android-ci.yml',
    '.github/workflows/google-play-release.yml',
]
errors = []
for rel in required:
    if not (ROOT / rel).is_file():
        errors.append(f'missing required file: {rel}')

for rel in [
    'app/src/main/AndroidManifest.xml',
    'app/src/main/res/layout/activity_main.xml',
    'app/src/main/res/layout/activity_splash.xml',
    'app/src/main/res/values/colors.xml',
    'app/src/main/res/values/strings.xml',
    'app/src/main/res/values/themes.xml',
    'app/src/main/res/values-v31/themes.xml',
    'app/src/main/res/xml/network_security_config.xml',
    'app/src/main/res/drawable/peleza_mark.xml',
    'app/src/main/res/drawable/splash_background.xml',
    'app/src/main/res/mipmap/ic_launcher.xml',
    'app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml',
]:
    try:
        ET.parse(ROOT / rel)
    except Exception as exc:
        errors.append(f'invalid XML {rel}: {exc}')

build = (ROOT / 'app/build.gradle').read_text(encoding='utf-8')
main = (ROOT / 'app/src/main/java/com/pelezastream/app/MainActivity.java').read_text(encoding='utf-8')
manifest = (ROOT / 'app/src/main/AndroidManifest.xml').read_text(encoding='utf-8')
all_text = '\n'.join(
    p.read_text(encoding='utf-8', errors='ignore')
    for p in ROOT.rglob('*')
    if p.is_file() and p.resolve() != Path(__file__).resolve()
    and p.suffix.lower() in {'.java','.gradle','.xml','.yml','.yaml','.md','.html','.txt','.properties'}
)

checks = {
    "applicationId 'com.pelezastream.app'": build,
    "namespace 'com.pelezastream.app'": build,
    "compileSdk 36": build,
    "targetSdk 36": build,
    "versionName '1.1.0'": build,
    'https://www.pelezastream.com/': all_text,
    'package com.pelezastream.app;': main,
    'android:name=".SplashActivity"': manifest,
    '@mipmap/ic_launcher': manifest,
    'status == 408 || status == 429 || status >= 500': main,
    'setAcceptThirdPartyCookies': main,
    'DownloadManager.Request': main,
    'onShowFileChooser': main,
    'onShowCustomView': main,
}
for needle, haystack in checks.items():
    if needle not in haystack:
        errors.append(f'missing expected project setting: {needle}')

if 'WebView.enableSafeBrowsing' in all_text:
    errors.append('invalid WebView.enableSafeBrowsing call is still present')
if 'pelezastream.top' in all_text.lower():
    errors.append('legacy pelezastream.top URL is still present')
if '\n          tracks:' in all_text:
    errors.append('Google Play workflow still uses invalid plural tracks input')

if errors:
    print('Peleza Stream project verification FAILED:')
    for err in errors:
        print(f' - {err}')
    sys.exit(1)

print('Peleza Stream project verification passed.')
print('applicationId: com.pelezastream.app')
print('version: 1.1.0 (110)')
print('home: https://www.pelezastream.com/')
print('branding: adaptive Peleza launcher + branded splash')
