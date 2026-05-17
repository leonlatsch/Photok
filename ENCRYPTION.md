# Encryption Specification

This document describes the encryption formats used across different major versions of the app.  
The format evolved significantly over time. Older formats are kept documented for **backwards compatibility and data migration only**.

---
# Encryption Versions Overview

The table below provides a high-level comparison of the architectural and cryptographic differences across the app's version history.

| Feature             | Version 1.x.x (Legacy)                    | Version 2.x.x (Deprecated)                          | Version 3.x.x (Current)                                                                    |
|:--------------------|:------------------------------------------|:----------------------------------------------------|:-------------------------------------------------------------------------------------------|
| **Status**          | ⚠️ Insecure (Decryption/Migration only)   | ✅ Deprecated (Can still be read by 3.x.x)            | ✅ Active / Current                                                                        |
| **File Extensions** | `.photok`, `.photok.tn`, `.photok.vp`     | `.crypt`, `.crypt.tn`, `.crypt.vp`                  | `.crypt`, `.crypt.tn`, `.crypt.vp`                                                         |
| **Cipher & Mode**   | AES / GCM / NoPadding (256-bit)           | AES / CBC / PKCS7Padding (256-bit)                  | AES / CBC / PKCS7Padding (256-bit)                                                         |
| **Header Format**   | **None** (Raw ciphertext stream)          | **Header Version 1** (`0x01`)                       | **Header Version 2** (`0x02`)                                                              |
| **Header Layout**   | *No metadata packet*                      | Version (1B) + Salt (16B) + IV (16B)                | Version (1B) + IV (16B)                                                                    |
| **Key Derivation**  | Insecure: `SHA-256(UTF8(password))`       | `PBKDF2WithHmacSHA256(password, salt)`              | **Vault Master Key (VMK)** pattern wrapped by KEK                                          |
| **IV**              | Static / Deterministic (`first 16 bytes`) | Completely Randomized                               | Completely Randomized                                                                      |
| **Design Focus**    | Legacy encryption implementation          | Fixed GCM IV reuse, random access for video seeking | Password decoupling, instant password changes, native biometrics, base for recovery phrase |
---

# Version 1.x.x Encryption (Legacy)

⚠️ **Status: Deprecated and insecure — supported for decryption & migration only**

**Extensions:** `.photok`, `.photok.tn`, `.photok.vp`

* **Cipher:** AES/GCM/NoPadding (256-bit), no AAD
* **Key Derivation:** `SHA-256(UTF8(password))` — no KDF
* **IV:** Deterministic — `first 16 bytes of password`
* **Security Issue:** Static IV with AES-GCM breaks cipher guarantees. Fixed in **#204**
* **Password Verification:** bcrypt hash stored in Shared Preferences

**File Format:** Raw ciphertext + 16-byte authentication tag (no header).

**Why 1→2:** 1.x.x lacked a proper KDF and had deterministic IV reuse. AES-GCM also doesn't support random access seeking — large videos required reading and discarding all preceding bytes, causing severe performance issues.

---

# Version 2.x.x Encryption

✅ **Status: Deprecated but supported. Can still be read by 3.x.x.**

**Extensions:** `.crypt`, `.crypt.tn`, `.crypt.vp`

* **Cipher:** AES/CBC/PKCS7Padding (256-bit)
* **Key Derivation:** `PBKDF2WithHmacSHA256(password, salt)` — salt stored in Shared Preferences
* **IV:** Completely randomized per file
* **Password Verification:** bcrypt hash in Shared Preferences

**File Format — header v1 (`0x01`) with salt:**

```
┌───────────┬───────────────┬────────────┬──────────────────────┐
│ VERSION   │ SALT          │ IV         │ ENCRYPTED_DATA       │
│ (1 Byte)  │ (Variable)    │ (16 Bytes) │ (Variable Size)      │
├───────────┼───────────────┼────────────┼──────────────────────┤
│ 0x01      │ PBKDF2 Salt   │ Random IV  │ Raw CBC Ciphertext   │
└───────────┴───────────────┴────────────┴──────────────────────┘
```

> ⚠️ The salt in the file header exists for external decryption compatibility. The app reads salt exclusively from Shared Preferences (except for backups).

**Biometric Unlock (2.x.x legacy):** A biometric-protected cipher from Android Keystore wrapped the password-derived key. The result was stored as Base64 in `biometric_keys` Shared Preferences under `"wrapped_user_key"`.

**Migration from 1.x.x:** Built-in flow detects `.photok` files, decrypts them, and re-encrypts to 3.x.x format. Backup (V3) is forced before migration.

---

# Version 3.x.x Encryption (Current)

**Extensions:** `.crypt`, `.crypt.tn`, `.crypt.vp`

Version 3.x.x decouples file encryption keys from user passwords using a **Vault Master Key (VMK)** pattern.

**Architecture:**

* **File Ciphering:** VMK encrypted via `AES/CBC/PKCS7Padding` (256-bit)
* **Key Separation:** File headers no longer store per-file salt — governed by the static VMK
* **Key Wrapping:** VMK wrapped by a **Key Encryption Key (KEK)** derived from password or biometrics
* **Storage:** Wrapped VMK + `VaultProtectionParams` persisted in SQLite

**File Format — header v2 (`0x02`), simplified:**

```
┌───────────┬────────────┬──────────────────────────┐
│ VERSION   │ IV         │ ENCRYPTED_DATA           │
│ (1 Byte)  │ (16 Bytes) │ (Variable Size)          │
├───────────┼────────────┼──────────────────────────┤
│ 0x02      │ Random IV  │ Raw CBC Ciphertext       │
└───────────┴────────────┴──────────────────────────┘
```

**Why 2→3:** The VMK/KEK split enables instant password changes (only the VMK in SQLite is re-encrypted, no media files touched), simplified biometric management (biometrics = alternative KEK pipeline), and lays groundwork for BIP-39 recovery phrases.

**Migration to 3.x.x:**

When transitioning from 1.x.x or 2.x.x databases into 3.x.x, the system hooks into the native authentication loops to safely construct and capture the persistent VMK layer.
This also happens before starting a potential migration from 1.x.x to 3.x.x.

**1. Password-Based Migration**
When a user authenticates with their password, the system validates the string against the historic **bcrypt** hash.
* **If migrating from Pre-2.x.x:** A completely new VMK is randomly generated as if the user installed 3.x.x freshly, since 1.x.x variants lacked a native KDF salt and re-encryption is needed anyway.
* **If migrating from 2.x.x:** The legacy password-derived key is rebuilt using the existing historical user salt stored in Shared Preferences. This key is designated directly as the static **VMK** to ensure back-compatibility with older ciphered targets.

Once the VMK is established, a clean 3.x.x KEK is derived:
1. A fresh cryptographic salt and random IV pair are explicitly minted.
2. A new KEK is built using the user's password string combined with this newly generated salt.
3. The core VMK is encrypted using this KEK via `AES/CBC/PKCS7Padding`.
4. The output `wrappedVMK` packet along with its operational metadata (`VaultProtectionParams`) is pushed to the SQLite database.

**2. Biometric-Based Migration**
If the user authenticates via biometrics first, the legacy structure can be securely migrated independently:
1. The app extracts the base64 string from the `"wrapped_user_key"` index within the `biometric_keys` Shared Preferences file.
2. The payload is parsed into its component chunks: the initial 16 bytes serve as the initialization vector (`IV`), and the remainder serves as the raw `wrappedVmk`.
3. A clean `VaultProtection` record is drafted using these structural components (the KDF and Salt variables are set to `null` as the cryptographic security relies directly on the hardware-backed Android Keystore infrastructure).
4. The historic base64 key entry is purged from the `biometric_keys` shared preference file once migration successfully resolves.

**Defensive Migration Fail-Safes**
It is entirely expected that a user might trigger a biometric migration path without executing a password migration run concurrently. The database safely maintains partial states; the password migration context triggers organically during subsequent structural lifecycle changes such as a manual unlock with password, password change, or when triggering an backup.

> **Critical Recovery Policy:** To protect user nodes against unforeseen migration errors or environment crashes, the historical configuration items (`legacyPasswordHash` and `legacyUserSalt`) are **never deleted** from Shared Preferences. This ensures the app can re-attempt data correction procedures safely if a legacy migration routine fails to write successfully.

# Backup Specification

This section describes the backup archive formats used across different generations of the app.

All backups are compiled as standard unencrypted **ZIP archives** containing an unencrypted structural metadata manifest (`meta.json`) alongside the respective encrypted media artifacts. The underlying encryption format of the media files inside the ZIP reflects the app's encryption version active at the time the backup was created.

---

## Backup Format V1
Used during the early lifecycle of the application.

### Archive Structure
```
┌───────────────────────────────┐
│          backup.zip           │
├───────────────────────────────1
│ meta.json                     │
│   {                           │
│     "password": String,       │  ← bcrypt hash of password
│     "photos": [PhotoBackup],  │  ← list of photo uuids with file metadata
│     "createdAt": Long,        │  ← timestamp of backup creation
│     "backupVersion": Int      │  ← backup version (1)
│   }                           │
│                               │    1.x.x format. GCM - no headers
│ <uuid>.photok                 │  ← Encrypted original file
│ ...                           │
└───────────────────────────────┘
```

## Backup Format V2
Introduced basic video preview logic and thumbnail persistence into the archive.

### Archive Structure
```
┌───────────────────────────────┐
│          backup.zip           │
├───────────────────────────────1
│ meta.json                     │
│   {                           │
│     "password": String,       │  ← bcrypt hash of password
│     "photos": [PhotoBackup],  │  ← list of photo uuids with file metadata
│     "createdAt": Long,        │  ← timestamp of backup creation
│     "backupVersion": Int      │  ← backup version (2)
│   }                           │
│                               │    1.x.x format. GCM - no headers
│ <uuid>.photok                 │  ← Encrypted original file
│ <uuid>.photok.tn              │  ← Encrypted thumbnail
│ <uuid>.photok.vp              │  ← Encrypted video preview
│ ...                           │
└───────────────────────────────┘
```

## Backup Format V3
Introduces albums to the backups.

### Archive Structure
```
┌───────────────────────────────┐
│          backup.zip           │
├───────────────────────────────1
│ meta.json                     │
│   {                           │
│     "password": String,       │  ← bcrypt hash of password
│     "photos": [PhotoBackup],  │  ← list of photo uuids with file metadata
│     "albums": [AlbumBackup],  │  ← list of albums with title, etc.
│     "albumPhotoRefs":         │  ← list of album-photo references. uuid to uuid.
│        [AlbumPhotoRefBackup], │
│     "createdAt": Long,        │  ← timestamp of backup creation
│     "backupVersion": Int      │  ← backup version (3)
│   }                           │
│                               │    1.x.x format. GCM - no headers
│ <uuid>.photok                 │  ← Encrypted original file
│ <uuid>.photok.tn              │  ← Encrypted thumbnail
│ <uuid>.photok.vp              │  ← Encrypted video preview
│ ...                           │
└───────────────────────────────┘
```

## Backup Format V4
Aligns file extensions with the updated Version 2.x.x block cipher specifications.

### Archive Structure
```
┌───────────────────────────────┐
│          backup.zip           │
├───────────────────────────────1
│ meta.json                     │
│   {                           │
│     "password": String,       │  ← bcrypt hash of password
│     "photos": [PhotoBackup],  │  ← list of photo uuids with file metadata
│     "albums": [AlbumBackup],  │  ← list of albums with title, etc.
│     "albumPhotoRefs":         │  ← list of album-photo references. uuid to uuid.
│        [AlbumPhotoRefBackup], │
│     "createdAt": Long,        │  ← timestamp of backup creation
│     "backupVersion": Int      │  ← backup version (4)
│   }                           │
│                               │    2.x.x format. CBC - PKCS7 Padding. Header (v1) + ciphertext
│ <uuid>.crypt                  │  ← Encrypted original file
│ <uuid>.crypt.tn               │  ← Encrypted thumbnail
│ <uuid>.crypt.vp               │  ← Encrypted video preview
│ ...                           │
└───────────────────────────────┘
```

## Backup Format V5
Modern backup implementation built around the Version 3.x.x decoupled Vault Master Key (VMK) security architecture.

### Archive Structure
```
┌─────────────────────────────────────────┐
│               backup.zip                │
├─────────────────────────────────────────1
│ meta.json                               │
│   {                                     │
│     "wrappedVmk": String,               │  ← the wrapped vault master key
│     "params": [VaultProtectionParams],  │  ← the vault protection parameters needed to decrypt the vmk
│     "photos": [PhotoBackup],            │  ← list of photo uuids with file metadata
│     "albums": [AlbumBackup],            │  ← list of albums with title, etc.
│     "albumPhotoRefs":                   │  ← list of album-photo references. uuid to uuid.
│        [AlbumPhotoRefBackup],           │
│     "createdAt": Long,                  │  ← timestamp of backup creation
│     "backupVersion": Int                │  ← backup version (5)
│   }                                     │
│                                         │    3.x.x format. CBC - PKCS7 Padding. Header (V2) + ciphertext
│ <uuid>.crypt                            │  ← Encrypted original file
│ <uuid>.crypt.tn                         │  ← Encrypted thumbnail
│ <uuid>.crypt.vp                         │  ← Encrypted video preview
│ ...                                     │
└─────────────────────────────────────────┘
```