# 🦜 Parrot mobile — TL;DL, Too Long, Do Listen

Native Android and iOS companions to [Parrot for Firefox](https://github.com/g-ongenae/parrot). Early working implementation; not a store-ready release.

## Android

A draggable accessibility overlay stays above other apps. Tap 🦜 to capture the visible text, then choose **Read aloud**, **Summarize**, or **Summarize & read**. Stop ends playback; Close clears the captured text and collapses the panel. Tap the bird to collapse or reopen. Disable the Parrot accessibility service to remove the button entirely.

Open Parrot → Enable floating reader → read the disclosure → enable Parrot in Android Accessibility settings. On some sideloaded installations Android requires allowing restricted settings in App info first. Only enable this for builds you trust.

Capture happens on tap only. It reads visible accessibility nodes, skips editable/password nodes, and does not scroll or scrape other pages. Apps with inaccessible/custom-drawn content may return nothing. Share text into Parrot or paste it as a fallback. URL-only sharing reads the URL, not the linked article. There is no OCR yet.

## iOS

Apple does not provide a general third-party overlay or cross-app accessibility reader. This version uses **Share → Read with Parrot**. Safari preprocessing supplies the selected text or visible article/main/body text. Other apps must share plain text; otherwise copy and paste into Parrot. Playback in the Share extension stops when the extension closes. There is no background audio or systemwide floating button on iOS.

## Summaries and privacy

This first version uses an on-device extractive summary: it ranks sentences by word frequency, keeps three, and presents them in source order. It is a key-sentence excerpt, not a generative AI rewrite. Short inputs are unchanged. No accounts, API keys, network permission, analytics, or persisted page history. Speech uses the operating system's selected voice; voice availability and any voice-service network behavior depend on your device settings. Install offline voices for offline reading.

The existing Firefox app's Claude/OpenAI providers, streamed AI summaries, premium voices, cost tracking, highlighting, pause/resume and seek have **not** been ported yet. Text handed to summarization is capped at roughly 100,000 characters; Android captures only the current accessible screen. Do not expect the complete article from a partially visible mobile page.

## Build

### Android (Linux, macOS or Windows)

Requires JDK 17, Android SDK 35, and Gradle 8.11.1. Set `ANDROID_HOME` or supply `android/local.properties` with `sdk.dir`.

```sh
gradle -p android assembleDebug lintDebug
adb install -r android/app/build/outputs/apk/debug/app-debug.apk
```

The repository intentionally uses the installed Gradle version; no wrapper is checked in. CI provisions the pinned version and uploads the debug APK.

### iOS (macOS + Xcode)

```sh
brew install xcodegen
cd ios
xcodegen generate
open Parrot.xcodeproj
```

Choose your Apple development team and unique bundle identifiers for both targets before installing on a physical device. Simulator build:

```sh
xcodebuild -project Parrot.xcodeproj -scheme Parrot -sdk iphonesimulator -configuration Debug CODE_SIGNING_ALLOWED=NO build
```

## Validation

GitHub Actions builds/lints Android and builds the iOS app and embedded Share extension on a simulator SDK. A Java regression harness checks summary selection, sentence order, short/empty inputs and newline captures. These checks do not replace device testing.

Manual checks before release:

- Enable Android access after reading the disclosure; verify the button, drag it, and capture a screen in a separate app.
- Exercise all three actions, Stop and Close; verify editable/password text is omitted and no capture happens on navigation alone.
- Disable access and verify removal. Try a screen with no accessible text and use the paste/share fallback.
- On iOS share Safari article content and selected text, then plain text from another app; exercise each action and close during playback.
- Check long text, installed/offline voices, rotation, large fonts and small screens on real devices.

No store submission, signing credentials or release distribution is configured. Accessibility use requires review against the current distribution-store policies before publication.
