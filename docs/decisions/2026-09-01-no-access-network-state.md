# Decision: no ACCESS_NETWORK_STATE in v1.0.0

## Context

`AndroidManifest.xml` declares `INTERNET`, `VIBRATE`, and `REQUEST_INSTALL_PACKAGES`. `res/xml/network_security_config.xml` sets `cleartextTrafficPermitted="false"` with exceptions for `localhost` and `10.0.2.2`. No code in `app/src/main/java/` calls `ConnectivityManager` (verified by `grep -rn ConnectivityManager app/src/main/java/` → no matches).

## Decision

Do not add `android.permission.ACCESS_NETWORK_STATE` to v1.0.0.

## Rationale

* The permission is `normal` (not `runtime`), so adding it would not prompt the user — the objection is not UX friction, it is shipping a capability that nothing reads.
* SII-29 (auto-reconnect on network change) is canceled. Nothing in v1.0.0 needs `ConnectivityManager`.
* `ACCESS_NETWORK_STATE` does not enable any capability on its own; without a call site it is dead code in the manifest.

## User-visible consequence

The app has no offline detection. When the connection drops, `TmuxWebViewClient`'s error callback fires, `MainActivity` shows `R.string.error_load_failed`, and re-prompts via `ServerUrlDialog`. This is documented in the README under "Network prerequisites" (SII-52):

> v1.0.0 has no offline detection. A dropped connection surfaces as an error message and a re-prompt for the server URL.

## When to revisit

Revive SII-29 (auto-reconnect) or any feature that needs `ConnectivityManager`. The permission must be added at the same time as the first call site, never alone.