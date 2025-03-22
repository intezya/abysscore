package com.intezya.abysscore.security.utils

import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.utils.crypto.sha512
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

// TODO: rewrite code
@Component
class JwtUtils(
    @Value("\${jwt.secret:}") private val rawSecret: String,
    @Value("\${jwt.expirationMinutes}") private val expirationMinutes: Int,
    @Value("\${jwt.issuer}") private val issuer: String,
) {
    private val secret: SecretKey by lazy {
        val key = sha512(rawSecret.takeUnless { it.isBlank() } ?: UUID.randomUUID().toString())
        Keys.hmacShaKeyFor(key.toByteArray())
    }

    fun generateToken(user: User, extraExpirationMinutes: Int = expirationMinutes): String {
        val claims = HashMap<String, Any>()
        if (user.hwid != null) {
            claims["hwid"] = user.hwid!!
        }
        return Jwts.builder()
            .setClaims(claims)
            .setIssuer(issuer)
            .setSubject(user.getUsername())
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + expirationMinutes * 60 * 1000))
            .signWith(secret, SignatureAlgorithm.HS256)
            .compact()
    }

    fun isTokenValid(token: String, userDetails: UserDetails): Boolean {
        val username = extractUsername(token)
        val tokenHwid = extractHwid(token)
        if (userDetails is User) {
            return (username == userDetails.username) &&
                !isTokenExpired(token) &&
                isHwidValid(userDetails.hwid, tokenHwid)
        }

        return (username == userDetails.username) && !isTokenExpired(token)
    }

    fun isTokenValid(token: String): Boolean = !isTokenExpired(token)

    private fun isHwidValid(dbHwid: String?, tokenHwid: String): Boolean {
        if (dbHwid == null) return true

        return dbHwid == tokenHwid
    }

    fun extractUsername(token: String): String = extractClaim(token) { it.subject }

    fun extractHwid(token: String): String = extractClaim(token) { claims ->
        claims["hwid"] as? String ?: ""
    }

    private fun isTokenExpired(token: String): Boolean = extractExpiration(token).before(Date())

    private fun extractExpiration(token: String): Date = extractClaim(token) { it.expiration }

    fun <T> extractClaim(token: String, claimsResolver: (Claims) -> T): T {
        val claims = extractAllClaims(token)
        return claimsResolver(claims)
    }

    private fun extractAllClaims(token: String): Claims = Jwts.parserBuilder()
        .setSigningKey(secret)
        .build()
        .parseClaimsJws(token)
        .body
}
