package com.intezya.abysscore.unit.aspect.security.utils

import com.intezya.abysscore.security.utils.PasswordUtils
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.springframework.security.crypto.password.PasswordEncoder

class PasswordUtilsTest {
    private lateinit var passwordUtils: PasswordUtils
    private lateinit var passwordEncoder: PasswordEncoder

    @BeforeEach
    fun setup() {
        passwordEncoder = mockk()
        passwordUtils = PasswordUtils(passwordEncoder)
    }

    @Test
    fun `hashPassword should encode and convert to base64`() {
        val password = "secure_password"
        val encodedPassword = "encoded_password_bytes"
        every { passwordEncoder.encode(password) } returns encodedPassword

        val result = passwordUtils.hashPassword(password)

        verify { passwordEncoder.encode(password) }
        assertEquals(encodeToBase64(encodedPassword), result)
    }

    @Test
    fun `verifyPassword should decode and verify with encoder`() {
        val rawPassword = "secure_password"
        val hashedPassword = encodeToBase64("encoded_password_bytes")
        val decodedHash = decodeFromBase64(hashedPassword)

        every { passwordEncoder.matches(rawPassword, decodedHash) } returns true

        val result = passwordUtils.verifyPassword(rawPassword, hashedPassword)

        verify { passwordEncoder.matches(rawPassword, decodedHash) }
        assertTrue(result)
    }

    @Test
    fun `verifyPassword should return false for non-matching passwords`() {
        val rawPassword = "wrong_password"
        val hashedPassword = encodeToBase64("encoded_password_bytes")
        val decodedHash = decodeFromBase64(hashedPassword)

        every { passwordEncoder.matches(rawPassword, decodedHash) } returns false

        val result = passwordUtils.verifyPassword(rawPassword, hashedPassword)

        verify { passwordEncoder.matches(rawPassword, decodedHash) }
        assertFalse(result)
    }

    @Test
    fun `hashHwid should calculate SHA-256 hash and encode to base64`() {
        val hwid = "device_hardware_id"

        val result = passwordUtils.hashHwid(hwid)

        assertEquals(encodeToBase64(sha256(hwid)), result)
    }

    @Test
    fun `verifyHwid should return true for matching hardware IDs`() {
        val rawHwid = "device_hardware_id"
        val hashedHwid = encodeToBase64(sha256(rawHwid))

        val result = passwordUtils.verifyHwid(rawHwid, hashedHwid)

        assertTrue(result)
    }

    @Test
    fun `verifyHwid should return false for non-matching hardware IDs`() {
        val rawHwid = "device_hardware_id"
        val differentHashedHwid = encodeToBase64(sha256("different_device_id"))

        val result = passwordUtils.verifyHwid(rawHwid, differentHashedHwid)

        assertFalse(result)
    }

    private fun encodeToBase64(input: String): String = "base64:$input"

    private fun decodeFromBase64(input: String): String = input.substringAfter("base64:")

    private fun sha256(input: String): String = "sha256:$input"
}
