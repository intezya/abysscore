package com.intezya.abysscore.security.utils

import com.intezya.abysscore.utils.crypto.decodeFromBase64
import com.intezya.abysscore.utils.crypto.encodeToBase64
import com.intezya.abysscore.utils.crypto.sha256
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class PasswordUtils(private val passwordEncoder: PasswordEncoder) {
    fun hashPassword(password: String): String = encodeToBase64(passwordEncoder.encode(password))

    fun verifyPassword(raw: String, hash: String): Boolean = passwordEncoder.matches(raw, decodeFromBase64(hash))

    fun hashHwid(input: String): String = encodeToBase64(sha256(input))

    fun verifyHwid(raw: String, hash: String): Boolean = hashHwid(raw) == hash
}
