# TuitionOS Android App

## Overview

TuitionOS is an Android Jetpack Compose app for tuition center management. It supports both offline local auth and cloud auth with Supabase.

## Prerequisites

- Android SDK installed and configured in `local.properties` via `sdk.dir`
- Java 11
- Android emulator or connected device
- `./gradlew` executable available in the project root

## Supabase Configuration

The app reads Supabase values from `local.properties`, project properties, or environment variables.

Add these entries to `local.properties`:

```properties
SUPABASE_URL=https://<your-project>.supabase.co
SUPABASE_KEY=<your-anon-public-key>
```

The app build config injects these values into `BuildConfig.SUPABASE_URL` and `BuildConfig.SUPABASE_KEY`.

## Build and Install

To build the debug APK:

```bash
./gradlew :app:assembleDebug
```

To install on a connected device/emulator:

```bash
./gradlew :app:installDebug
```

## APK Artifact

Built debug APK location:

- `app/build/outputs/apk/debug/app-debug.apk`

## Running the App

To launch the app on the emulator:

```bash
adb shell monkey -p com.aistudio.tuitionos.pzvsnw -c android.intent.category.LAUNCHER 1
```

To view runtime logs:

```bash
adb logcat -s SupabaseConfig CloudAuth ViewModel AndroidRuntime
```

## Notes

- The current workspace does not contain Git metadata, so pushing the repository to GitHub is not available from this environment.
- Ensure your Supabase credentials are valid and the URL contains `supabase.co`.
- If cloud login fails, verify the `SUPABASE_URL` and `SUPABASE_KEY` values in the generated `BuildConfig.java`.
