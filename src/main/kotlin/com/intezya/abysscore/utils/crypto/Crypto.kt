package com.intezya.abysscore.utils.crypto

import java.security.MessageDigest

fun sha512(input: String): String {
    val digest = MessageDigest.getInstance("SHA-512")
    val hashBytes = digest.digest(input.toByteArray())
    return hashBytes.joinToString("") { "%02x".format(it) }
}
