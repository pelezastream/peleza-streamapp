# Google Play deployment checklist

## Before the first upload

- [ ] Create the Peleza Stream app in Google Play Console.
- [ ] Confirm the package ID is `com.pelezastream.app`.
- [ ] Create and safely archive the Android upload keystore.
- [ ] Complete Google Play App Signing setup.
- [ ] Add the app privacy policy URL in Play Console.
- [ ] Complete Data safety, content rating, ads declaration, app access and target-audience forms as applicable.
- [ ] Prepare phone/tablet screenshots and feature graphic.
- [ ] Upload the first signed AAB through Play Console if the Play Developer API does not yet recognize the package.
- [ ] Enable the Google Play Android Developer API and grant the service account access to this app.

## GitHub Secrets

`ANDROID_KEYSTORE_BASE64`
`ANDROID_KEYSTORE_PASSWORD`
`ANDROID_KEY_ALIAS`
`ANDROID_KEY_PASSWORD`
`PLAY_SERVICE_ACCOUNT_JSON`

## Recommended rollout

1. Internal testing
2. Closed testing if required for the account/app
3. Production after Play requirements and testing are complete

The included workflow defaults to **internal** and requires a manual action to choose another track.
