# Releasing Photok

All build, sign and release logic lives in [`fastlane/Fastfile`](fastlane/Fastfile).
GitHub Actions only invoke lanes, so the exact same steps run locally or on any CI.

## Branch model

`main` is the trunk. There is no `develop`/`master` anymore.

```
main ──┬─────────────────────────────────────────────►
       │                                         ▲
       └─ release/3.2.0 ─ bump ─ changelog ─ tag │ merge back (branch kept alive)
                                  3.2.0 ──────────┘
                                  └─► triggers Release
```

1. Branch `release/X.Y.Z` off `main`.
2. Stabilize: fix bugs, run `Prepare Release` (or `fastlane prepare version:X.Y.Z`)
   to bump the version, refresh license reports and scaffold the changelog.
3. Fill in `fastlane/metadata/android/en-US/changelogs/<versionCode>.txt`.
4. Wait for **CI** on the release branch to go green.
5. Push the tag: `git tag X.Y.Z && git push origin X.Y.Z`. This triggers **Release**.
6. Merge the release branch back into `main`, but **keep the branch alive** so you can
   ship hotfixes (`X.Y.Z+1`) from it without pulling in unreleased work from `main`.

## What a release does

Pushing a tag runs the `release` lane, which:

| Channel       | Artifact            | How it ships                                              |
| ------------- | ------------------- |-----------------------------------------------------------|
| Google Play   | signed `play` AAB   | uploaded via `supply` as a **draft**                      |
| GitHub release| signed `foss` APK   | release created with the APK attached                     |
| IzzyOnDroid   | (the GitHub APK)    | picks up the APK from the GitHub release automatically    |
| F-Droid       | builds `foss` itself| builds from the pushed tag (fdroiddata recipe, UCM: Tags) |

> F-Droid builds from source on its own infra — it only needs the tag and the
> `fastlane/metadata` listing. Nothing in this repo pushes to F-Droid directly.

## Lanes

```sh
bundle exec fastlane test                  # unit tests (CI)
bundle exec fastlane build                 # compile check (CI)
bundle exec fastlane prepare version:X.Y.Z # bump version + licenses + changelog stub
bundle exec fastlane release               # build, sign, upload to Play, GitHub release
bundle exec fastlane translations          # refresh README translation badges
```

Run `release` locally by exporting the same env vars the workflow sets (see below)
and pointing `KEYSTORE_PATH` / `PLAY_JSON_KEY_FILE` at local files.

## Required GitHub secrets

| Secret                       | Used for                                                        |
| ---------------------------- | --------------------------------------------------------------- |
| `SIGNING_KEY`                | base64 of the upload keystore (`base64 -i keystore.jks`)        |
| `KEY_ALIAS`                  | key alias in the keystore                                       |
| `KEY_PASSWORD`               | keystore **and** key password (reused for both)                 |
| `TELEMETRY_DECK_APP_ID`      | TelemetryDeck app id baked into the build                       |
| `PLAY_SERVICE_ACCOUNT_JSON`  | Google Play service-account JSON (see supply setup docs)        |
| `GITHUB_TOKEN`               | provided automatically; creates the GitHub release              |

The keystore and Play JSON are decoded into temp files by the workflow and never
committed (`.gitignore` blocks `*.jks` / `*.keystore` / `play-service-account.json`).
