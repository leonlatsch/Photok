# Encryption Specification

This document describes the encryption formats used across different major versions of the app.  
The format evolved significantly over time. Older formats are kept documented for **backwards compatibility and data migration only**.

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
Version 1.x.x files contain **no header or metadata**. The file contents are exactly the raw output of the encryption operation:
`[ ciphertext || authentication_tag ]`

## Password Verification (Shared Preferences)
To verify the user's password locally before attempting decryption, the password was hashed with **bcrypt** and stored in the app's Shared Preferences.

---

# Version 2.x.x Encryption

⚠️ **Status: Deprecated — supported for decryption & migration only**

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

> ⚠️ **Note:** Although a `SALT` block is written into each individual file header (see below) for compatibility with external decryption tools, the app itself reads the derivation salt exclusively from Shared Preferences.

---

## IV (Nonce) Generation

The IV is completely randomized for every single file encryption operation, successfully eliminating the IV reuse vulnerability found in 1.x.x.

---

## File Format

Version 2.x.x introduces a structured binary header containing metadata required for external decryption tools.

The file layout is structured as follows:
`[ENC_VERSION_BYTE][SALT][IV][ENCRYPTED_DATA]`

| Field | Size | Description |
| :--- | :--- | :--- |
| `ENC_VERSION_BYTE` | 1 Byte | Version marker. Fixed to **`0x01`** |
| `SALT` | Variable | The salt used for PBKDF2 key derivation |
| `IV` | 16 Bytes | The random initialization vector used for the file |
| `ENCRYPTED_DATA` | Variable | The raw AES-CBC encrypted ciphertext |

---

## Biometric Unlock (2.x.x Legacy implementation)

Biometric authentication was first introduced during the 2.x.x lifecycle.

1. A biometric-protected cipher was requested from the Android Keystore system.
2. This biometric cipher was used to wrap the core password-derived key.
3. The resulting structure was saved inside a dedicated Shared Preferences file (`biometric_keys`) as a Base64 string under the key `"wrapped_user_key"`.
4. The stored payload layout was: `[ IV + Ciphertext ]`.

---

## Password Verification (Shared Preferences)

Carried over from the 1.x.x security model, the user's password is encrypted/hashed with **bcrypt** and stored in Shared Preferences. This allows the app to validate the password instantly on login before executing any file decryption routines.

---

## Migration

The app includes a built-in migration flow that:
1. Detects legacy `.photok` files
2. Decrypts them using the 1.x.x parameters
3. Re-encrypts them into the modern format (3.x.x)

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

The file layout is structured as follows:
`[ENC_VERSION_BYTE][IV][ENCRYPTED_DATA]`

| Field | Size | Description                                        |
| :--- | :--- |:---------------------------------------------------|
| `ENC_VERSION_BYTE` | 1 Byte | Version marker. Fixed to **`0x02`**                |
| `IV` | 16 Bytes | The random initialization vector used for the file |
| `ENCRYPTED_DATA` | Variable | The raw AES-CBC encrypted ciphertext               |

---

## Migration Architecture

When transitioning from 1.x.x or 2.x.x databases into 3.x.x, the system hooks into the native authentication loops to safely construct and capture the persistent VMK layer.

### 1. Password-Based Migration
When a user authenticates with their master password, the system validates the string against the historic **bcrypt** hash.
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
It is entirely expected that a user might trigger a biometric migration path without executing a password migration run concurrently. The database safely maintains partial states; the password migration context triggers organically during subsequent structural lifecycle changes such as a manual login challenge, password change, or when triggering an application backup pipeline.

> 🔒 **Critical Recovery Policy:** To protect user nodes against unforeseen migration errors or environment crashes, the historical configuration items (`legacyPasswordHash` and `legacyUserSalt`) are **never deleted** from Shared Preferences. This ensures the app can re-attempt data correction procedures safely if a legacy migration routine fails to write successfully.