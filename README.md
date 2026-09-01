# tmux-mobile Android

Native Android wrapper for [tmux-mobile](https://github.com/KSiig/tmux-mobile) — a mobile-first tmux web interface.

## What it does

Wraps tmux-mobile's web frontend in a native Android WebView with:

- Full-screen terminal with JavaScript/WebSocket support
- JavaScript bridge for native features (haptic feedback, app info)
- Network security config (HTTPS enforced, cleartext only for localhost dev)
- Orientation and screen-on handling
- Track-aware in-app self-update: the release build downloads `app-release.apk`, the debug build downloads `app-debug.apk`; the updater never crosses tracks

## Requirements

- Android 8.0+ (API 26)
- A running [tmux-mobile](https://github.com/KSiig/tmux-mobile) server, reachable over HTTPS (Cloudflare tunnel or Tailscale)
- For sideloading: the per-app "Install unknown apps" permission enabled for your browser or file manager

## Before you install

If you are upgrading from a build installed before **v1.0.0**, you must uninstall first. The release track uses a different signing key than earlier dev-track builds, and installing over them fails:

```sh
adb uninstall com.ksiig.tmuxmobile
```

The failure message, if you skip this step, is `INSTALL_FAILED_UPDATE_INCOMPATIBLE`.

> Your saved server URL may be restored automatically after reinstalling, but this depends on Android Auto Backup and your Google account and is not guaranteed. Be ready to re-enter your tmux-mobile server URL.

## Install

Releases are at <https://github.com/KSiig/tmux-mobile-android/releases/latest>. Two APKs are published:

- **`app-release.apk`** — **take this one.** This is the production-signed build.
- **`app-debug.apk`** — the development track, signed with a different key. Only useful for testers staying on the dev track.

Only those two APKs are published. Sideloading is the only install path; there is no listing on any store.

To install on stock Android:

1. Open Settings and enable the per-app **"Install unknown apps"** permission for your browser (or file manager, if you downloaded the APK that way). The exact Settings path varies by manufacturer and Android version — the permission's name is the same everywhere.
2. Tap the downloaded APK and confirm the install prompt.

Alternative, from a desktop with `adb`:

```sh
adb install -r app-release.apk
```

GrapheneOS: follow the OS-specific install steps at <https://grapheneos.org/usage#installing-apps>.

CalyxOS: follow the OS-specific install steps at <https://calyxos.org/docs/guide/apps/>.

## Verify the download

In the directory you downloaded the release into:

```sh
sha256sum -c checksums.txt
apksigner verify --print-certs app-release.apk
```

Compare the SHA-256 certificate fingerprint printed by `apksigner verify` against the one committed in [SECURITY.md](./SECURITY.md). If the values do not match, do not install the APK.

> The published checksums identify the exact artifact built by CI. A local `./gradlew assembleRelease` will not reproduce them byte for byte, because R8 output and build timestamps are not deterministic. Authenticity comes from the signing certificate fingerprint, not from rebuilding.

## First run

On first launch the app prompts for a tmux-mobile server URL. A bare hostname is normalized to `https://`.

- Tap the update FAB to check for updates.
- Long-press the same FAB to change the server URL later.

The in-app updater stays on the track you installed. A release install never offers the debug build, and a debug install never offers the release build. Cross-track moves happen via manual sideload (see [Before you install](#before-you-install)), never through the in-app updater.

## Network prerequisites

The tmux-mobile server must be reachable over HTTPS. A Cloudflare tunnel in front of `npx tmux-mobile` is the typical setup; Tailscale works too. Cleartext HTTP is blocked by the app except for `localhost` and `10.0.2.2` (the Android emulator alias for the host machine).

> v1.0.0 has no offline detection. A dropped connection surfaces as an error message and a re-prompt for the server URL.

## Build from source

A debug build needs nothing:

```sh
./gradlew assembleDebug
```

The APK is at `app/build/outputs/apk/debug/app-debug.apk`.

A release build requires four environment variables (set after [SII-55 Part A](https://linear.app/siig/issue/SII-55)):

- `RELEASE_KEYSTORE_PATH`
- `RELEASE_KEYSTORE_PASSWORD`
- `RELEASE_KEY_ALIAS`
- `RELEASE_KEY_PASSWORD`

Without them, `./gradlew assembleRelease` fails with an explicit error rather than emitting an unsigned APK:

```
RELEASE_KEYSTORE_PATH not set - refusing to build an unsigned release APK.
```

## Project structure

```text
app/src/main/java/com/ksiig/tmuxmobile/
├── MainActivity.kt           # WebView setup, URL prompt, lifecycle, Back handling
├── TmuxWebViewClient.kt      # URL filtering, error handling, connectivity callback
├── TmuxWebChromeClient.kt    # window.prompt / JS dialog plumbing
├── TmuxBridge.kt             # JavaScript ↔ native bridge (vibration, version)
├── JsPromptDialog.kt         # Native prompt dialog backing the WebChromeClient
├── ServerUrlDialog.kt        # Server URL input dialog
└── UpdateManager.kt          # In-app self-update against the GitHub Releases API
```

## License

MIT
