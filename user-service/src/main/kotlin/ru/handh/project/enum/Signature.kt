package ru.handh.project.enum

import com.nimbusds.jose.JWSAlgorithm

enum class Signature(
    val algorithm: JWSAlgorithm?
) {
    NON(null),
    SECRET(JWSAlgorithm.HS256),
    RSA(JWSAlgorithm.RS256)
}
