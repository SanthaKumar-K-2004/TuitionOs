This project supports optional Supabase cloud sync for TuitionOS.

Quick setup (local development)

1. Set Supabase credentials (choose one):

  - local.properties (recommended for local dev - DO NOT commit):

    In the project root `local.properties` add:

    SUPABASE_URL=https://your-project.supabase.co
    SUPABASE_KEY=eyJ...your_anon_public_key...

  - or environment variables (CI / container):

    export SUPABASE_URL=https://your-project.supabase.co
    export SUPABASE_KEY=eyJ...your_anon_public_key...

2. Build the app (debug)

```bash
./gradlew :app:assembleDebug -x test
```

3. Install on device/emulator

```bash
./gradlew installDebug
```

Notes
- `app/build.gradle.kts` exposes `BuildConfig.SUPABASE_URL` and `BuildConfig.SUPABASE_KEY` from the Gradle project properties or environment variables.
- The app will fallback to an empty key if none is provided; cloud mode is disabled when key is missing.
- Do not commit `local.properties` or keys. Keep secrets in CI secrets or secure vaults for release builds.

If you want, paste your `SUPABASE_URL` and `SUPABASE_KEY` here (or attach a text file) and I can inject them into `local.properties` locally and run a quick build & cloud sync test.
