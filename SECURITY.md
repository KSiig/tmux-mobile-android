# Security

## Release signing certificate

Release builds (`app-release.apk`) are signed with this certificate:

    SHA256: C1:E6:A6:7E:33:ED:89:4F:A1:51:D7:5A:72:45:A2:AB:98:8A:40:97:3D:A9:6B:2E:C4:30:68:02:D6:A6:DC:F0

Verify a downloaded APK before installing it:

    apksigner verify --print-certs app-release.apk

If the printed SHA-256 does not match the value above, do not install it.

`app-debug.apk` is the development track and is signed with a different
key. Its fingerprint is deliberately not published here.

## Reporting an issue

Open a GitHub issue, or contact the maintainer directly for anything
sensitive.
