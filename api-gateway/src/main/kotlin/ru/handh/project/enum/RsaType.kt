package ru.handh.project.enum

import com.nimbusds.jose.jwk.KeyUse

enum class RsaType(
    val keyUse: KeyUse
) {
    SIGN(KeyUse.SIGNATURE),
    ENCRYPT(KeyUse.ENCRYPTION)
}
