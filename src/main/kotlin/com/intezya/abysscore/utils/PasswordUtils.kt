package com.intezya.abysscore.utils

import de.mkammerer.argon2.Argon2
import org.springframework.stereotype.Service
import java.security.MessageDigest

@Service
class PasswordUtils(
    private val argon2: Argon2,
) {
    companion object {
        private const val MEMORY = 65536    // 64MB in KB
        private const val ITERATIONS = 3     // Number of iterations
        private const val PARALLELISM = 4
    }

    fun hashPassword(password: String): String {
        return argon2.hash(ITERATIONS, MEMORY, PARALLELISM, password.toCharArray())
    }

    fun verifyPassword(raw: String, hash: String): Boolean {
        return argon2.verify(hash, raw.toCharArray())
    }

    fun hashHwid(input: String): String {
        val digest = MessageDigest.getInstance("SHA-512")
        val hashedBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashedBytes.joinToString("") { String.format("%02x", it) }
    }

    fun verifyHwid(raw: String, hash: String): Boolean {
        return hashHwid(raw) == hash
    }
}
