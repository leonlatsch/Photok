package dev.leonlatsch.photok.encryption.domain.models

class WrappedVmk(
    val params: VaultProtectionParams,
    val wrappedVMK: ByteArray,
)