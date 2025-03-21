package com.intezya.abysscore.security.utils

import com.intezya.abysscore.model.entity.User
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.test.util.ReflectionTestUtils
import javax.crypto.SecretKey

class JwtUtilsTest {

    private lateinit var jwtUtils: JwtUtils
    private val rawSecret = "test-secret-key-for-jwt-utils-testing-purposes-only"
    private val issuer = "test-issuer"
    private val expirationMinutes = 60

    @BeforeEach
    fun setUp() {
        jwtUtils = JwtUtils(rawSecret, expirationMinutes, issuer)
        // Initialize the secret key
        ReflectionTestUtils.invokeGetterMethod(jwtUtils, "getSecret")
    }

    @Test
    fun `generateToken should create valid token with correct claims`() {
        val username = "testUser"
        val hwid = "test-hardware-id"
        val user = User(
            username = username,
            password = "asd",
            hwid = hwid,
        )

        val token = jwtUtils.generateToken(user)

        assertNotNull(token)
        assertEquals(username, jwtUtils.extractUsername(token))
        assertEquals(hwid, jwtUtils.extractHwid(token))
        assertTrue(jwtUtils.isTokenValid(token))

        val claims = jwtUtils.extractClaim(token) { it }
        assertEquals(issuer, claims.issuer)
    }

    @Test
    fun `generateToken should create valid token without hwid when user hwid is null`() {
        val username = "testUser"
        val user = User(
            username = username,
            password = "asd",
            hwid = null,
        )

        val token = jwtUtils.generateToken(user)

        assertNotNull(token)
        assertEquals(username, jwtUtils.extractUsername(token))
        assertEquals("", jwtUtils.extractHwid(token))
        assertTrue(jwtUtils.isTokenValid(token))
    }

    /* TODO
    @Test
    fun `generateToken should respect custom expiration time`() {
        val username = "testUser"
        val user = User(
            username = username,
            password = "asd",
            hwid = "test-hardware-id",
        )
        val shortExpirationMinutes = 1

        val token = jwtUtils.generateToken(user, shortExpirationMinutes)

        assertTrue(jwtUtils.isTokenValid(token))

        Thread.sleep(shortExpirationMinutes * 60 * 1000L + 100L)

        assertThrows<ExpiredJwtException> {
            jwtUtils.isTokenValid(token)
        }
    }*/

    @Test
    fun `isTokenValid should return true for valid token and matching user details`() {
        val username = "testUser"
        val hwid = "test-hardware-id"
        val user = User(
            username = username,
            password = "asd",
            hwid = hwid,
        )
        val token = jwtUtils.generateToken(user)

        assertTrue(jwtUtils.isTokenValid(token, user))
    }

    @Test
    fun `isTokenValid should return false for valid token but non-matching username`() {
        // Arrange
        val user = User(
            username = "testUser",
            password = "asd",
            hwid = "test-hardware-id",
        )
        val token = jwtUtils.generateToken(user)

        val differentUser = User(
            username = "differentUser",
            password = "asd",
            hwid = "test-hardware-id-2",
        )

        assertFalse(jwtUtils.isTokenValid(token, differentUser))
    }

    @Test
    fun `isTokenValid should return false for valid token but non-matching hwid`() {
        val user = User(
            username = "testUser",
            password = "asd",
            hwid = "test-hardware-id",
        )
        val token = jwtUtils.generateToken(user)

        val sameUserDifferentHwid = User(
            username = "testUser",
            password = "asd",
            hwid = "different-hardware-id",
        )

        assertFalse(jwtUtils.isTokenValid(token, sameUserDifferentHwid))
    }

    @Test
    fun `isTokenValid should work with non-User UserDetails`() {
        // Arrange
        val username = "testUser"
        val user = User(
            username = username,
            password = "asd",
            hwid = null,
        )
        val token = jwtUtils.generateToken(user)

        val userDetails = mockk<UserDetails>()
        every { userDetails.username } returns username

        assertTrue(jwtUtils.isTokenValid(token, userDetails))
    }

    @Test
    fun `isHwidValid should return true when dbHwid is null`() {
        val user = User(
            username = "username",
            password = "asd",
            hwid = null,
        )
        val token = jwtUtils.generateToken(user)
        val tokenHwid = jwtUtils.extractHwid(token)

        val result = ReflectionTestUtils.invokeMethod<Boolean>(
            jwtUtils,
            "isHwidValid",
            null,
            tokenHwid,
        )
        assertTrue(result!!)
    }

    @Test
    fun `isHwidValid should return true when hwids match`() {
        val hwid = "test-hardware-id"

        val result = ReflectionTestUtils.invokeMethod<Boolean>(
            jwtUtils,
            "isHwidValid",
            hwid,
            hwid,
        )
        assertTrue(result!!)
    }

    @Test
    fun `isHwidValid should return false when hwids don't match`() {
        val dbHwid = "test-hardware-id"
        val tokenHwid = "different-hardware-id"

        val result = ReflectionTestUtils.invokeMethod<Boolean>(
            jwtUtils,
            "isHwidValid",
            dbHwid,
            tokenHwid,
        )
        assertFalse(result!!)
    }

    @Test
    fun `extractAllClaims should throw exception for invalid token`() {
        val invalidToken = "invalid.token.string"

        assertThrows<Exception> {
            ReflectionTestUtils.invokeMethod<Any>(jwtUtils, "extractAllClaims", invalidToken)
        }
    }

    @Test
    fun `secret should be initialized only once`() {
        val jwtUtils1 = JwtUtils(rawSecret, expirationMinutes, issuer)
        val jwtUtils2 = JwtUtils(rawSecret, expirationMinutes, issuer)

        val secret1 = ReflectionTestUtils.invokeGetterMethod(jwtUtils1, "getSecret") as SecretKey
        val secret2 = ReflectionTestUtils.invokeGetterMethod(jwtUtils2, "getSecret") as SecretKey

        assertEquals(secret1, secret2)
    }

    @Test
    fun `secret should be generated when rawSecret is blank`() {
        val jwtUtilsBlankSecret = JwtUtils("", expirationMinutes, issuer)

        val secret = ReflectionTestUtils.invokeGetterMethod(jwtUtilsBlankSecret, "getSecret")

        assertNotNull(secret)
    }
}
