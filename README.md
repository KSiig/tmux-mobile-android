# tmux-mobile Android

Native Android wrapper for [tmux-mobile](https://github.com/KSiig/tmux-mobile) — a mobile-first tmux web interface.

## What it does

Wraps tmux-mobile's web frontend in a native Android WebView with:

- Full-screen terminal with JavaScript/WebSocket support
- JavaScript bridge for native features (haptic feedback, app info)
- Network security config (HTTPS enforced, cleartext only for localhost dev)
- Orientation and screen-on handling

## Requirements

- Android 8.0+ (API 26)
- A running [tmux-mobile](https://github.com/KSiig/tmux-mobile) server (locally or via Cloudflare tunnel)

## Build

```bash
./gradlew assembleDebug
```

The APK is at `app/build/outputs/apk/debug/app-debug.apk`.

## Usage

1. Start tmux-mobile on your dev machine: `npx tmux-mobile`
2. Open the app and enter the server URL
3. Done — you have a mobile tmux client

## Project structure

```text
app/src/main/java/com/ksiig/tmuxmobile/
├── MainActivity.kt       # WebView setup, URL prompt, lifecycle
├── TmuxWebViewClient.kt  # URL filtering, error handling
├── TmuxBridge.kt         # JavaScript ↔ native bridge (vibration, version)
└── ServerUrlDialog.kt    # Server URL input dialog
```

## License

MIT
