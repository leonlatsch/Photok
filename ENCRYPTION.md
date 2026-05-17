# Encryption Specification

This document describes the encryption formats used across different major versions of the app.  
The format evolved significantly over time. Older formats are kept documented for **backwards compatibility and data migration only**.

---
# Encryption Versions Overview

The table below provides a high-level comparison of the architectural and cryptographic differences across the app's version history.

| Feature             | Version 1.x.x (Legacy)                    | Version 2.x.x (Deprecated)                          | Version 3.x.x (Current)                                                                    |
|:--------------------|:------------------------------------------|:----------------------------------------------------|:-------------------------------------------------------------------------------------------|
| **Status**          | ⚠️ Insecure (Decryption/Migration only)   | ⚠️ Deprecated (Can still be read by 3.x.x)          | 🟩 Active / Current                                                                        |
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

Files created by this version use the extensions:
* `.photok` → encrypted original file
* `.photok.tn` → encrypted thumbnail file
* `.photok.vp` → encrypted video preview file (introduced late in 1.x.x)

## Algorithm
* **Cipher:** AES/GCM/NoPadding (256-bit)
* **Provider:** Android JVM default crypto provider
* **AAD:** None. The entire file was encrypted as a single blob.

## Key & IV Derivation
* **KDF:** None. The key was derived directly via `SHA-256( UTF8(password) )`.
* **IV Generation:** Deterministic. `iv = first 16 bytes of password characters`.
* **Security Issue:** Reusing a static IV with AES-GCM breaks the security guarantees of the cipher, leading to potential plaintext and key recovery. Fixed in issue **#204**.

## File Format

Version 1.x.x files contain **no header or metadata**. The file layout is exactly the raw binary stream output of the encryption operation:

```
┌──────────────────────────────────────┐
│          Raw Output File             │
├──────────────────────────────────────┤
│  Ciphertext                          │
│  ... (Variable Size)                 │
├──────────────────────────────────────┤
│  Authentication Tag                  │
│  (16 Bytes)                          │
└──────────────────────────────────────┘
```

## Password Verification (Shared Preferences)
To verify the user's password locally before attempting decryption, the password was hashed with **bcrypt** and stored in the app's Shared Preferences.

---

## Design Rationale: Why we changed from 1.x.x to 2.x.x

The architecture shift from 1.x.x to 2.x.x was driven by two major requirements:

* **Cryptographic Security:** 1.x.x suffered from critical structural vulnerabilities, notably the lack of a proper Key Derivation Function (KDF) and a deterministic IV structure that caused unsafe IV reuse under AES-GCM. Switching to PBKDF2 and randomized IVs resolved these security issues.
* **Video Player Random Access (Seeking):** The introduction of video files highlighted a severe architectural limitation in AES-GCM. Because AES-GCM is a stream-oriented mode requiring strict authentication tags, **true random access seeking is not natively supported**. Seeking to a specific time in a video required allocating an `AesDataSource` that had to read, decrypt, and entirely throw away (`forceSkip`) all preceding bytes up to the target offset. For large video streams, this caused severe performance degradation and high memory/CPU overhead.

# Version 2.x.x Encryption

✅ **Status: Deprecated but supported. Can still be read by 3.x.x.**

Version 2.x.x addressed the critical IV reuse vulnerability of 1.x.x by introducing a proper key derivation function, randomized nonces, and a dedicated file header structure.

Files created by this version use the extensions:
* `.crypt` → encrypted original file
* `.crypt.tn` → encrypted thumbnail file
* `.crypt.vp` → encrypted video preview file

---

## Algorithm

Encryption used:
AES/CBC/PKCS7Padding
Key size: 256 bit

The encryption utilizes a block cipher mode with randomized initialization vectors to ensure that identical plaintexts result in completely different ciphertexts.

---

## Key Derivation

Unlike the previous version, 2.x.x utilizes a password-based key derivation function:
Key = PBKDF2WithHmacSHA256(password, salt)

The `salt` used during this derivation is generated uniquely for the key and stored directly within the app's Shared Preferences.

> ⚠️ **Note:** Although a `SALT` block is written into each individual file header (see below) for compatibility with external decryption tools, the app itself reads the derivation salt exclusively from Shared Preferences. Except for backups.

---

## IV (Nonce) Generation

The IV is completely randomized for every single file encryption operation, successfully eliminating the IV reuse vulnerability found in 1.x.x.

---

## File Format

Version 2.x.x introduces a structured binary header containing metadata required for external decryption tools.

```
┌───────────────────────────────────────────────────────────────────────┐
│                           File Layout v2.x.x                          │
├───────────┬───────────────────────┬────────────┬──────────────────────┤
│ VERSION   │ SALT                  │ IV         │ ENCRYPTED_DATA       │
│ (1 Byte)  │ (Variable Size)       │ (16 Bytes) │ (Variable Size)      │
├───────────┼───────────────────────┼────────────┼──────────────────────┤
│ 0x01      │ PBKDF2 Salt Payload   │ Random IV  │ Raw CBC Ciphertext   │
└───────────┴───────────────────────┴────────────┴──────────────────────┘
```


---

## Biometric Unlock (2.x.x Legacy implementation)

Biometric authentication was first introduced during the 2.x.x lifecycle.

```
┌────────────────────────────────────────────────────────┐
│        "wrapped_user_key" Shared Preferences           │
├───────────────────────────┬────────────────────────────┤
│ IV                        │ Ciphertext                 │
│ (16 Bytes)                │ (Variable Size)            │
├───────────────────────────┴────────────────────────────┤
│  Base64 Encoded Pipeline Payload                      │
└────────────────────────────────────────────────────────┘
```


1. A biometric-protected cipher was requested from the Android Keystore system.
2. This biometric cipher was used to wrap the core password-derived key.
3. The resulting structure was saved inside a dedicated Shared Preferences file (`biometric_keys`) as a Base64 string under the key `"wrapped_user_key"`.

---

## Password Verification (Shared Preferences)

Carried over from the 1.x.x security model, the user's password is encrypted/hashed with **bcrypt** and stored in Shared Preferences. This allows the app to validate the password instantly on login before executing any file decryption routines.

---

## Migration from 1.x.x

The app includes a built-in migration flow that:
1. Detects legacy `.photok` files
2. Decrypts them using the 1.x.x parameters
3. Re-encrypts them into the modern format (3.x.x)o

This comes with a comprehensive ui and a background process.

Backup is forced before starting the migration. The backup is created in the format V3 (see beblow).

---

# Version 3.x.x Encryption (Current)

Version 3.x.x reinvents the underlying architecture by decoupling file encryption keys from user passwords using a **Vault Master Key (VMK)** pattern. This architectural shift facilitates seamless password alterations, native biometric updates, and robust cryptographic storage.

---

## Architecture: The VMK Paradigm

Instead of encrypting local media via a key compiled directly from the user's password, files are now encrypted via a static **Vault Master Key (VMK)**.


* **File Ciphering:** The VMK utilizes the identical primitive as 2.x.x: `AES/CBC/PKCS7Padding` (256-bit).
* **Key Separation:** The file headers no longer store dynamic individual salt packets, as individual payloads are governed strictly by the static VMK context.
* **Key Wrapping:** The VMK itself is kept secure at rest by wrapping it inside a **Key Encryption Key (KEK)** derived dynamically from the password or a hardware-backed biometric instance.

The wrapped VMK blob is persisted cleanly inside the app's SQLite database alongside an explicit metadata payload (`VaultProtectionParams`) that records the exact configuration values required to re-derive the matching KEK (including the target KDF, KDF Iterations, Cipher Algorithm, and Key Size).

---

## File Format

Because the core key context is offloaded to the database architecture, the written files are vastly simplified. The salt placeholder is removed entirely.

```
┌───────────────────────────────────────────────────┐
│                File Layout v3.x.x                 │
├───────────┬────────────┬──────────────────────────┤
│ VERSION   │ IV         │ ENCRYPTED_DATA           │
│ (1 Byte)  │ (16 Bytes) │ (Variable Size)          │
├───────────┼────────────┼──────────────────────────┤
│ 0x02      │ Random IV  │ Raw CBC Ciphertext       │
└───────────┴────────────┴──────────────────────────┘
```


---

## Design Rationale: Why we changed from 2.x.x to 3.x.x

The 3.x.x version marks the modernization of Photok's data management layers. While 2.x.x addressed basic primitive file security and video stream performance, its tightly-coupled key architecture became a limiting bottleneck.

The VMK/KEK split in 3.x.x provides several distinct architectural advantages:

* **Instant Password Modifications:** In older versions, because the file encryption key was derived directly from the user's password, changing a password required completely reading, decrypting, and re-writing every single data file on disk. In 3.x.x, changing a password requires only re-encrypting the small **VMK** blob stored inside the SQLite database with a new KEK. The underlying media files remain untouched.
* **Simplified Biometric Management:** Biometric authentication no longer requires awkward out-of-band hacks in standalone Shared Preferences files. Biometrics simply act as an alternative Key Encryption Key (KEK) pipeline that exposes the same underlying database VMK.
* **Foundation for Recovery Schemes:** Separating the data key (VMK) from the authentication mechanism (KEK) establishes the vital technical groundwork required to implement a deterministic recovery phrase feature (BIP-39 mnemonic phrases) down the line.

---

## Migration Architecture

When transitioning from 1.x.x or 2.x.x databases into 3.x.x, the system hooks into the native authentication loops to safely construct and capture the persistent VMK layer.
This also happens before starting a potential migration from 1.x.x to 3.x.x.

### 1. Password-Based Migration
When a user authenticates with their password, the system validates the string against the historic **bcrypt** hash.
* **If migrating from Pre-2.x.x:** A completely new salt array is freshly initialized via `SecureRandom` since 1.x.x variants lacked a native KDF salt.
* **If migrating from 2.x.x:** The legacy password-derived key is rebuilt using the existing historical user salt stored in Shared Preferences. This key is designated directly as the static **VMK** to ensure back-compatibility with older ciphered targets.

Once the VMK context is established, a clean 3.x.x KEK infrastructure is instantiated:
1. A fresh cryptographic salt and random IV pair are explicitly minted.
2. A new KEK is built using the user's password string combined with this newly generated salt.
3. The core VMK is encrypted using this KEK via `AES/CBC/PKCS7Padding`.
4. The output `wrappedVMK` packet along with its operational metadata (`VaultProtectionParams`) is pushed to the SQLite database.

### 2. Biometric-Based Migration
If the user authenticates via biometrics first, the legacy structure can be securely migrated independently:
1. The app extracts the base64 string from the `"wrapped_user_key"` index within the `biometric_keys` Shared Preferences file.
2. The payload is parsed into its component chunks: the initial 16 bytes serve as the initialization vector (`IV`), and the remainder serves as the raw `wrappedVmk`.
3. A clean `VaultProtection` record is drafted using these structural components (the KDF and Salt variables are set to `null` as the cryptographic security relies directly on the hardware-backed Android Keystore infrastructure).
4. The historic base64 key entry is purged from the `biometric_keys` shared preference file once migration successfully resolves.

### Defensive Migration Fail-Safes
It is entirely expected that a user might trigger a biometric migration path without executing a password migration run concurrently. The database safely maintains partial states; the password migration context triggers organically during subsequent structural lifecycle changes such as a manual unlock with password, password change, or when triggering an backup.

> 🔒 **Critical Recovery Policy:** To protect user nodes against unforeseen migration errors or environment crashes, the historical configuration items (`legacyPasswordHash` and `legacyUserSalt`) are **never deleted** from Shared Preferences. This ensures the app can re-attempt data correction procedures safely if a legacy migration routine fails to write successfully.

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