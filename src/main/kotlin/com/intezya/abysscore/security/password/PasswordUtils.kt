package com.intezya.abysscore.security.password

import com.intezya.abysscore.utils.crypto.decodeFromBase64
import com.intezya.abysscore.utils.crypto.encodeToBase64
import com.intezya.abysscore.utils.crypto.sha256
import de.mkammerer.argon2.Argon2
import org.springframework.stereotype.Component

@Component
class PasswordUtils(
    private val argon2: Argon2,
) {
    companion object {
        private const val MEMORY = 65536 // 64MB in KB
        private const val ITERATIONS = 3 // Number of iterations
        private const val PARALLELISM = 4
    }

    fun hashPassword(password: String): String = encodeToBase64(argon2.hash(ITERATIONS, MEMORY, PARALLELISM, password.toCharArray()))

    fun verifyPassword(
        raw: String,
        hash: String,
    ): Boolean = argon2.verify(decodeFromBase64(hash), raw.toCharArray())

    fun hashHwid(input: String): String = encodeToBase64(sha256(input))

    fun verifyHwid(
        raw: String,
        hash: String,
    ): Boolean = hashHwid(raw) == hash
}
