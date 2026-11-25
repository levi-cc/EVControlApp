# EV Control (Android)

WebView app that opens https://control.effettoventuri.eu/ and signs in automatically.

## Set / change the username and password
- On first launch (or when the login fails), a dialog opens automatically.
- At any time: tap the **⚙** button (semi-transparent, bottom right), enter the username and password,
  then **Save** (the session is reset and the app signs in again). **Forget** deletes the stored credentials.
- Credentials are stored encrypted on the phone (EncryptedSharedPreferences) and never in the repository.

## Get the APK (no Android Studio needed)
APKs are built by GitHub Actions (workflow **Build APK**).

- **Release (recommended)**: push a tag starting with `v`; the workflow builds the APK and attaches it
  to a GitHub release (**Releases** tab), named `EVControl-<tag>.apk`.
  ```
  git tag v1.1
  git push origin v1.1
  ```
- **Latest build**: **Actions** tab > **Build APK** > latest run > artifact **EVControl-debug-apk**.

Copy the APK to the phone and install it (allow installs from unknown sources).
