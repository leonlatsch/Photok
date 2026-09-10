# AGENTS.md — Photok Codebase Guide

This document is the authoritative guide for AI agents working in the Photok codebase.  
Read it fully before writing any code.

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Repository Layout](#repository-layout)
3. [Modules](#modules)
4. [Architecture](#architecture)
5. [UI Patterns](#ui-patterns)
6. [Encryption System](#encryption-system)
7. [Key Libraries](#key-libraries)
8. [Database](#database)
9. [Dependency Injection](#dependency-injection)
10. [Translations & Strings](#translations--strings)
11. [Testing](#testing)
12. [Product Flavors](#product-flavors)
13. [Rules & Conventions](#rules--conventions)

---

## Project Overview

Photok is an Android app (Kotlin) that provides an on-device encrypted photo and video vault. All media is stored inside the Android private files directory, encrypted with AES/CBC. The app supports photos, GIFs, and videos. It has no server component.

Key features: gallery, albums, backup/restore, biometric unlock, recovery phrase, password change, dark/light theme, hide-app mode (stealth dialer), and a settings screen.

---

## Repository Layout

The top-level Gradle project contains `app/`, `core/`, `pro/`, `gradle/`, `adr/`, `ENCRYPTION.md`, and this file. `pro/` is a Git submodule and may be absent in a checkout that has not initialized submodules. Source lives in the module that owns the concern; do not assume `app/` owns all code.

To orient yourself, browse the relevant module's `src` tree and the `dev/leonlatsch/photok/` package. Each top-level package is a self-contained feature. The current source tree is the live source of truth; do not rely on an enumeration in this file.

Dependencies are declared by module in `app/build.gradle.kts`, `core/build.gradle.kts`, and, when present, `pro/build.gradle.kts`. Check the owning module's build file before adding or changing a dependency.

Each feature follows the same internal structure:

- **`data/`** — Room DAOs, table entities, repository implementations.
- **`domain/`** — pure Kotlin interfaces, models, use cases. No Android imports.
- **`di/`** — Hilt modules that bind `data` implementations to `domain` interfaces.
- **`ui/`** — ViewModels, Fragments, Compose screens, navigator classes.
  - **`ui/compose/`** — screen-level and sub-composables.

Shared UI components, the theme, core models, encryption, persistence, and shared interfaces belong in `core/`. App-specific UI and Android entry points belong in `app/`. Legacy base classes (`Bindable*`, `Base*`) live in `app/.../uicomponnets/`. Extensions and misc utilities live in the owning module's `other/` package.

---

## Modules

The project has three Gradle modules:

| Module | Role | Dependency direction |
|---|---|---|
| `:core` | Shared vault/domain infrastructure: encryption, file storage, Room database, common models, shared UI/theme, configuration, and interfaces/stubs used by other modules. | Must not depend on `:app` or `:pro`. |
| `:app` | Installable application: `MainActivity`, manifest, navigation graph, app-specific features, resources, flavor source sets, and composition/wiring. | Depends on `:core`; uses `:pro` only for the Play variant when the submodule is available. |
| `:pro` | Private paid-feature Android library: billing, paywall, intruder warnings, panic lock, and pro implementations. | Git submodule; depends on `:core`, never on `:app`. |

Keep dependency flow one-way: `:app` → `:core` and `:pro` → `:core`. Put reusable contracts in `:core`; neither `:core` nor `:pro` may import app implementation code.

### Pro Git Submodule

`pro/` is declared in `.gitmodules` and is conditionally included by `settings.gradle.kts` only when `pro/build.gradle.kts` exists. A checkout without it is intentional and must still build the FOSS app.

- Initialize it after cloning when pro work or Play builds are needed: `git submodule update --init --recursive`.
- Do not edit, stage, commit, or push inside `pro/` unless the task explicitly includes the private pro repository. Changes there have their own Git history and review lifecycle.
- Changes to the submodule pointer in the parent repository are deliberate release/integration changes; make them only when explicitly requested.
- Never add a compile-time `:pro` dependency that prevents a checkout without the submodule from configuring. Follow the existing guarded inclusion and Play-only dependency pattern.

---

## Architecture

### Core Pattern

The codebase follows a **feature-first layered architecture** within the module that owns a feature:

- **`domain`** — pure Kotlin. Interfaces, models, use cases. No Android imports.
- **`data`** — Room tables, DAOs, repository implementations. Implements `domain` interfaces.
- **`di`** — Hilt modules that bind `data` implementations to `domain` interfaces.
- **`ui`** — ViewModels + Compose screens + Fragments + Navigator classes. App entry points and navigation remain in `:app`.

### Single Activity

The installable `:app` module has a single `MainActivity` (with `DataBinding`). All screens are **Fragments** navigated via `app/src/main/res/navigation/main_nav_graph.xml`. Fragments host Compose UIs via `ComposeView`.

### Navigation

- Declared in `main_nav_graph.xml` with Safe Args.
- Bottom-tab navigation (`MainMenu`, a Compose component) connects to top-level destinations: Gallery, Albums, Settings.
- Fragment-level navigation uses typed `Navigator` classes injected via Hilt.

---

## UI Patterns

### Compose First

**New screens must use Jetpack Compose (Material3).** Legacy screens use XML DataBinding via the `Bindable*` base classes; do not add new DataBinding screens.

### Simple MVI

Every screen follows a **simple, flat MVI**. There is no dedicated MVI framework — it is plain Kotlin + StateFlow.

**The four files per screen:**

| File | Role |
|------|------|
| `XyzFragment.kt` | `@AndroidEntryPoint Fragment`. Creates a `ComposeView`, provides `CompositionLocal`s, collects navigation event flows. |
| `XyzViewModel.kt` | `@HiltViewModel`. Exposes `val uiState: StateFlow<XyzUiState>`. Accepts events via `fun handleUiEvent(event: XyzUiEvent)`. |
| `XyzUiState.kt` | `sealed interface XyzUiState`. Common states: `Empty`, `Loading`, `Content(...)`. |
| `XyzUiEvent.kt` | `sealed interface XyzUiEvent`. One `data class`/`data object` per user action. |
| `XyzScreen.kt` | Top-level `@Composable` that takes the `ViewModel`, collects state with `collectAsStateWithLifecycle()`, and branches on the sealed state. |

Navigation events that must leave the ViewModel are sent via a `Channel<XyzNavigationEvent>` and collected in the Fragment.

**Canonical example** — `GalleryFragment` / `GalleryViewModel` / `GalleryUiState` / `GalleryUiEvent` / `GalleryScreen`.

### Legacy DataBinding Screens

Still used in `unlock` and a few others. They extend `BindableFragment<ViewDataBinding>` / `BindableActivity<ViewDataBinding>`. ViewModels can extend `ObservableViewModel` for two-way bindings. **Do not create new DataBinding screens.**

### Theme

`AppTheme` (in `core/.../ui/theme/Theme.kt`) wraps every Compose entry point. It respects the system dark/light setting. Always call `AppTheme { ... }` at the root of a Fragment's `ComposeView.setContent { }`.

### CompositionLocals

Shared objects are injected into the Compose tree via `CompositionLocal`. Check the relevant module's UI package and feature-specific files (for example, `app/.../transcoding/compose/LocalEncryptedImageLoader.kt`) for the current set.

Provide them in the Fragment's `setContent { }` block using `CompositionLocalProvider`.

### Compose Components

Reusable composables belong in `core/.../ui/components/` when they are module-neutral; otherwise keep them in the owning app or pro feature. **Compose first** means building reusable components at the narrowest shared module boundary.

---

## Encryption System

> **Read `ENCRYPTION.md` before touching any encryption-related code.** Below is a brief summary for navigation.

### Current Format (v3.x.x)

- **Cipher:** AES/CBC/PKCS7Padding, 256-bit.
- **VMK (Vault Master Key):** A single random secret key used to encrypt all media files. Never stored plaintext.
- **KEK (Key Encryption Key):** Derived from password (PBKDF2) or biometrics (Android Keystore). Used to wrap the VMK.
- **Storage:** Wrapped VMK + `VaultProtectionParams` stored in Room (`VaultProtectionTable`).
- **File header (v2):** `0x02` (1 byte) + random IV (16 bytes) + CBC ciphertext.

### Key Classes

Encryption classes live in `core/src/main/kotlin/dev/leonlatsch/photok/encryption/`. Start with `VaultService` (in `encryption/domain/`) to understand the entry point — it orchestrates unlock, create, and reset for all protection types. `VaultProtectionHandler` (in `encryption/domain/handlers/`) is the strategy interface implemented for password, biometric, and recovery-phrase flows. `CryptoEngine` (in `encryption/domain/crypto/`) is the interface for all encrypt/decrypt stream operations. `VaultFileStorage` (in `core/.../io/`) is the only place that opens encrypted file streams. `SessionRepository` (in `encryption/domain/`) holds the active VMK in memory for the current session.

### Rules for Encryption Code

- **Never** store the raw VMK to disk or shared preferences.
- **Never** delete `legacyPasswordHash` or `legacyUserSalt` from shared preferences (migration fail-safe — see `ENCRYPTION.md`).
- Use `CryptoEngine` interface — do not instantiate `CbcCryptoEngine` directly in UI or repository code.
- All file I/O goes through `VaultFileStorage`.

---

## Key Libraries

Check the owning module's build file for the current library list. Key areas to know:

- **Jetpack Compose + Material3** — all new UI.
- **Hilt / Dagger** — DI throughout.
- **Room** — SQLite ORM with auto-migrations.
- **Navigation Component** — single-activity fragment navigation, with Safe Args.
- **Coil** — image loading; there is a custom `EncryptedImageFetcher` in `transcoding/` that decrypts on-the-fly.
- **ExoPlayer / Media3** — video playback.
- **jBCrypt** — legacy password hashing, used for migration only.
- **Gson** — backup JSON serialization.
- **Timber** — logging. Use exclusively; never use `android.util.Log` directly.
- **kotlinx-coroutines** — async throughout.
- **Biometric** — fingerprint/face unlock.
- **TelemetryDeck** — analytics, play flavor only.

---

## Database

The app uses a Room database (`photok.db`). The `PhotokDatabase` class in `core/.../model/database/` is the source of truth for all entities and the current schema version. Room schema exports live in `core/schemas/`.

All schema changes must use **Room auto-migrations** declared in `@Database(autoMigrations = [...])`. Always add a new `AutoMigration(from = N, to = N+1)` entry and bump `DATABASE_VERSION` when changing the schema. Never write manual SQL migrations unless Room cannot handle the change automatically.

---

## Dependency Injection

Hilt is used throughout. Each feature that needs DI has a `di/` sub-package containing a Hilt module — look there for the current bindings. Put bindings with the implementation they construct: shared database/configuration bindings in `:core`, app composition bindings in `app/.../di/`, and paid-feature bindings in `:pro`. Do not make `:core` depend on app or pro implementations.

Use `@Singleton` for expensive objects. ViewModels are `@HiltViewModel`.

---

## Translations & Strings

The supported locales are the `values-*/` directories under `core/src/main/res/`. Check those directories for the current list — do not rely on any enumeration in this file.

### Rule: Always add strings to every locale file

When you add a new string to `values/strings.xml`, you **must** also add a copy of it to every other `values-*/strings.xml` file. Use the English text as the placeholder and annotate with an XML comment `<!-- TODO -->` on the same line.

```xml
<!-- values/strings.xml (English, no TODO) -->
<string name="my_new_string">My new string</string>

<!-- values-de/strings.xml (and all other locales) -->
<string name="my_new_string">My new string</string> <!-- TODO -->
```

The `<!-- TODO -->` annotation is required: the `updateTranslations` Gradle task (in `gradle/updateTranslations.gradle.kts`) counts `<string>` lines that do **not** contain `TODO` to calculate each locale's translation percentage. Lines with `TODO` are intentionally excluded so the badge reflects real human translation coverage.

---

## Testing

Unit tests live in the owning module's `src/test/` tree (`app/src/test/` or `core/src/test/`) and use JUnit 4, Robolectric (Android runtime emulation), MockK, and `kotlinx-coroutines-test`. Look at the encryption tests for representative examples of the style.

When writing tests:
- Prefer integration tests for crypto flows.
- Mock only at the domain/data boundary — avoid mocking internal crypto primitives.
- Use `runTest` for anything involving coroutines.

---

## Product Flavors

| Flavor | `BuildConfig.PLAY` | Notes |
|--------|--------------------|-------|
| `play` | `true` | Google Play release; includes TelemetryDeck |
| `foss` | `false` | F-Droid / sideload release; no telemetry |

Flavor-specific code belongs in the owning module's `src/play/` or `src/foss/` source set. `:app` and `:core` define the `distribution` dimension. `:app` consumes `:pro` only through its guarded `playImplementation` dependency; FOSS behavior is provided by core/app stubs and must not require the submodule.

---

## Rules & Conventions

### Git

- **Do not commit on your own.** Stage and propose changes, but never run `git commit` or `git push`.
- If you are on a feature branch, run `git diff main` (or `git diff $(git merge-base HEAD main)`) early to understand what has already changed in this feature.
- Treat `pro/` as a separate repository according to the [Pro Git Submodule](#pro-git-submodule) rules above.

### Code Style

- **Compose first.** Prefer writing new UI in Jetpack Compose with Material3. Only touch legacy DataBinding code when modifying existing screens that still use it.
- **Simple over complex.** Prefer a straightforward implementation with a few lines of logic over elaborate abstractions, extra layers, or design patterns beyond what the codebase already uses.
- **Stick to the architecture.** New features must follow the `data / domain / di / ui` split. Domain code must not depend on Android SDK classes. UI code must not reach into `data` directly.
- **Compose decomposition.** Break screens into small, focused composables. Follow the existing pattern: `XyzScreen` → `XyzContent` / `XyzPlaceholder` → leaf components.
- **Comments.** Only comment code that genuinely needs clarification. Do not add redundant comments that restate what the code already says.
- **License header.** All new `.kt` files must include the Apache 2.0 license header (copy from any existing file).

### Naming

| Artifact | Convention | Example |
|----------|-----------|---------|
| ViewModel | `<Feature>ViewModel` | `GalleryViewModel` |
| UiState | `<Feature>UiState` (sealed interface) | `GalleryUiState` |
| UiEvent | `<Feature>UiEvent` (sealed interface) | `GalleryUiEvent` |
| Fragment | `<Feature>Fragment` | `GalleryFragment` |
| Composable screen | `<Feature>Screen` | `GalleryScreen` |
| Sub-composable | `<Feature>Content`, `<Feature>Placeholder`, etc. | `GalleryContent` |
| Navigator | `<Feature>Navigator` | `GalleryNavigator` |
| Repository interface | `<Feature>Repository` | `AlbumRepository` |
| Repository impl | `<Feature>RepositoryImpl` | `AlbumRepositoryImpl` |
| Hilt module | `<Feature>Module` | `AlbumsModule` |
| Room table entity | `<Feature>Table` | `AlbumTable` |

### Logging

Use **Timber** exclusively. Never use `android.util.Log` directly.

```kotlin
Timber.d("Debug info")
Timber.e("Error: $e")
Timber.w("Warning")
```

### Coroutines

- All database and I/O work runs on `Dispatchers.IO` inside `withContext` or repository/use-case `suspend` functions.
- ViewModels use `viewModelScope`. App-level coroutines use the `CoroutineScope(Dispatchers.Default)` provided via Hilt.
- Collect flows in the Fragment with `launchLifecycleAwareJob` (from `other/extensions`), never in a raw `lifecycleScope.launch` without `repeatOnLifecycle`.

### Result Handling

Prefer `Result<T>` and `.onSuccess { } .onFailure { }` for operations that can fail, consistent with `VaultService.unlock()`.
