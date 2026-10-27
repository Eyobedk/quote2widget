# Quote Widget

Android app for keeping your own quotes and showing them on your home screen.

- Add, edit and delete quotes (with an optional author) in the app.
- Home screen widget shows one quote. Tap it to switch to another random one.
- The widget also changes by itself about every 30 minutes (Android's minimum for this).
- Tap the pencil on the widget to jump into the app and edit.
- Pick a widget color in the app (6 themes).
- Everything is stored on the phone. No internet permission, no accounts, no libraries.

Requires Android 7.0 (API 24) or newer.

## Getting the APK WITH,NO SETUP(USING ONLY GITHUB)

    GitHub (no setup): Push the folder to a repo. It includes a workflow that builds the APK on each push to master, and you download app-debug.apk from the run’s artifacts.

## Build the APK

**Android Studio:** File > Open this folder, wait for the Gradle sync, then Build > Build APK(s).
Or press Run with a phone/emulator connected.

**Command line** (JDK 17 and the Android SDK installed):

    ./gradlew assembleDebug

APK: `app/build/outputs/apk/debug/app-debug.apk`

**No local setup (GitHub):** push this folder to a GitHub repo. The included workflow
`.github/workflows/build.yml` builds the APK on every push to `main` (or run it by hand from the
Actions tab). Download `quote-widget-debug-apk` from the run's artifacts.

## Install on your phone

Copy `app-debug.apk` to the phone and open it (allow "Install unknown apps" for your file manager or
browser when asked), or run `adb install app-debug.apk`.

## Add the widget

Open the app and tap **Add widget to home screen**. If your launcher doesn't support that, long-press
the home screen, choose **Widgets**, then **Quote Widget**.

## Where things are

- `QuoteStore.kt`: saved quotes, current quote, color themes
- `QuoteWidgetProvider.kt`: the widget (rendering and tap actions)
- `MainActivity.kt`: the quote manager screen
- `res/layout/widget_quote.xml`: widget layout
