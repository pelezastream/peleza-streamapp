# Peleza Stream Android

Production-ready Android wrapper for **https://www.pelezastream.com/**.

## App identity

- App name: **Peleza Stream**
- Application ID: `com.pelezastream.app`
- Version: `1.1.0`
- Version code: `110`
- Minimum Android: API 24 / Android 7.0
- Target/compile SDK: API 36 / Android 16
- Java: 17
- Android Gradle Plugin: 8.13.2
- Gradle: 8.13

This repository establishes `com.pelezastream.app` as the production application ID. Do not change it after the first Google Play upload.

## App features

- Uses a Peleza P/play adaptive launcher icon and a dedicated dark/red branded splash screen.\n- Loads Peleza Stream from the current `pelezastream.com` domain.
- Keeps Peleza links inside the app.
- Opens non-Peleza web links and custom schemes in their appropriate external app.
- Handles fullscreen HTML5 video.
- Supports website file-upload fields.
- Supports downloads through Android Download Manager.
- Retains cookies/session state.
- Displays a matching Peleza-branded retry page for main-frame network failures, HTTP 408/429, and server 5xx errors.
- Accepts Peleza web links through Android VIEW intents.
- Cleartext traffic is disabled at the Android network-security layer.

## Repository verification

Run:

```bash
python3 scripts/verify-project.py
```

The script verifies the package ID, current Peleza domain, Android version settings, required files, and XML syntax. It also fails if a legacy domain is reintroduced.

## GitHub Actions

### Android CI - Build APK and AAB

`.github/workflows/android-ci.yml`

Runs automatically for pushes/PRs to `main` or `master`, and can also be started manually. It:

1. Sets up JDK 17, Android SDK 36, and Gradle 8.13.
2. Validates the repository structure.
3. Runs Android lint.
4. Builds a debug APK and unsigned release AAB.
5. Uploads build artifacts plus SHA-256 checksums.

### Google Play Release

`.github/workflows/google-play-release.yml`

Manual workflow for a signed Play Store release. Configure these GitHub repository secrets:

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`
- `PLAY_SERVICE_ACCOUNT_JSON`

Then open **Actions → Google Play Release → Run workflow** and select the track. Start with `internal`.

> Google Play normally requires the app/package to exist in Play Console and the first AAB to be uploaded/accepted before API-based release automation can manage subsequent releases.

## Create `ANDROID_KEYSTORE_BASE64`

Keep the original `.jks` file private. Convert it to a GitHub Secret locally:

Linux:

```bash
base64 -w 0 peleza-stream-upload.jks
```

macOS:

```bash
base64 < peleza-stream-upload.jks | tr -d '\n'
```

Never commit the keystore or credentials. `.gitignore` already excludes them.

## First GitHub deployment

1. Create an empty GitHub repository.
2. Upload the **contents** of this folder to the repository root.
3. Commit/push to `main`.
4. Open **Actions → Android CI - Build APK and AAB**.
5. When the workflow is green, download `Peleza-Stream-Android-v1.1.0` from Artifacts.
6. Test the APK on a real Android device.
7. Create the app in Google Play Console with application ID `com.pelezastream.app`.
8. Configure app signing and the GitHub Secrets above.
9. Use **Google Play Release** for subsequent signed releases.

## Important deployment note

The Android application depends on the availability and performance of `https://www.pelezastream.com/`. The app includes a retry screen for web/server failures, but it cannot make an unavailable WordPress backend available.
