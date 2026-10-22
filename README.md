# PELEZA UPDATES — Android app

This branch (`peleza-updates-android-build`) contains the **PELEZA UPDATES** Android app. It is deliberately separate from the Peleza Stream code on the repository's default branch.

## App identity

- Application ID: `org.pelezaupdates.app`
- App label: `PELEZA UPDATES`
- Website: https://www.pelezaupdates.org/
- Version name: `1.0.1`
- Version code: `2`
- Minimum API: 24; target/compile API: 36
- Android Java source: `app/src/main/java/com/pelezastream/app/MainActivity.java` (the Java package is internal; the Play Store application ID is different)

**Do not upload Peleza Stream artifacts to the Peleza Updates Google Play listing.** Never change `org.pelezaupdates.app` for an update to the existing listing.

## Android bottom navigation (v1.0.1)

The previous horizontal row of wide orange text buttons has been replaced with a fixed icon navigation bar using the Peleza Updates navy and orange brand palette.

Five main tabs: **Home, News, Jobs, Education, More**. The **More** bottom sheet keeps **Opportunities, Entertainment, Community, Search, and Refresh** available with icons. The selected tab is highlighted. The website and Peleza Stream app are unchanged.

## Building APK / AAB

The workflow is `.github/workflows/peleza-updates-ci.yml` and runs automatically on pushes to this branch. Navigate to GitHub **Actions → PELEZA UPDATES - APK and AAB** to find the latest run and download its artifact.

Every run produces:
- `PELEZA-UPDATES-v1.0.1-TEST.apk` — debug build, for direct local testing.
- `PELEZA-UPDATES-v1.0.1-SIGNED.aab` **only when Peleza Updates signing secrets are set**, otherwise `PELEZA-UPDATES-v1.0.1-UNSIGNED.aab`.
- `SHA256SUMS.txt`.

**An unsigned AAB cannot be uploaded to Google Play.** To sign the AAB using the original, existing Peleza Updates upload key, configure these GitHub Actions repository secrets:

- `PELEZA_UPDATES_KEYSTORE_BASE64` — Base64 of the **original** Peleza Updates upload keystore.
- `PELEZA_UPDATES_KEYSTORE_PASSWORD`
- `PELEZA_UPDATES_KEY_ALIAS`
- `PELEZA_UPDATES_KEY_PASSWORD`

Keep the keystore and credentials private and out of source control. Do not generate a replacement key for an existing Play listing unless using Google's approved upload-key reset process.

## Publishing the navigation update to internal testers

1. Confirm the build has succeeded in GitHub Actions.
2. Download the **signed** Peleza Updates v1.0.1 AAB.
3. Open the `org.pelezaupdates.app` app in Google Play Console.
4. Choose **Testing → Internal testing → Create new release**, upload the signed AAB (version code 2), and roll out the update.
5. Open Google Play on an opted-in tester account and update the installed app.
6. Verify category navigation and responsive labels on a small screen.

The changes are in the source branch; **they do not change the already-installed Play Store build** until the new signed release is rolled out.
