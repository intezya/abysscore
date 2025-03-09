package com.intezya.abysscore.security.jwt

import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.repository.UserRepository
import com.intezya.abysscore.security.dto.UserAuthInfoDTO
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession
import java.security.Key
import java.util.*

@Component
class JwtUtils(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.expirationMinutes}") private val expirationMinutes: Int,
    @Value("\${jwt.issuer}") private val issuer: String,
    private val userRepository: UserRepository,
) {
    private val secretKey: Key by lazy { Keys.hmacShaKeyFor(secret.toByteArray()) }
    private val logger = LoggerFactory.getLogger(JwtUtils::class.java)

    fun generateJwtToken(
        user: User,
        extraExpirationMinutes: Int = expirationMinutes,
    ): String =
        Jwts
            .builder()
            .setSubject(user.id.toString())
            .setIssuer(issuer)
            .claim("hwid", user.hwid)
            .claim("user", user.username)
            .setIssuedAt(Date())
            .setExpiration(Date(Date().time + extraExpirationMinutes * 60 * 1000))
            .signWith(secretKey, SignatureAlgorithm.HS512)
            .compact()

    fun getClaimsFromJwtToken(token: String): Claims =
        Jwts
            .parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body

    fun getUserInfoFromToken(token: String): UserAuthInfoDTO {
        val claims = getClaimsFromJwtToken(token)
        val id = claims["sub"].toString().toLong()
        return UserAuthInfoDTO(
            id = id,
            username = claims["user"].toString(),
            hwid = claims["hwid"].toString(),
            accessLevel = getAccessLevel(id),
        )
    }

    fun validateJwtToken(authToken: String): Boolean {
        try {
            getClaimsFromJwtToken(authToken)
            return true
        } catch (e: Exception) {
//            logger.error("JWT validation error: ${e.message}; $expirationMinutes", e)
            return false
        }
    }

    fun getClientIp(request: HttpServletRequest): String {
        val headers =
            listOf(
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_CLIENT_IP",
                "REMOTE_ADDR",
            )

        for (header in headers) {
            val ip = request.getHeader(header)
            if (!ip.isNullOrEmpty() && ip != "unknown") {
                return ip.split(",").first().trim()
            }
        }
        return request.remoteAddr
    }

    fun getClientIp(session: WebSocketSession): String {
        val headers =
            listOf(
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_CLIENT_IP",
                "REMOTE_ADDR",
            )

        for (header in headers) {
            val ip = session.handshakeHeaders[header]?.firstOrNull()
            if (!ip.isNullOrEmpty() && ip != "unknown") {
                return ip.split(",").first().trim()
            }
        }
        return session.remoteAddress.toString()
    }

    private fun getAccessLevel(userId: Long) =
        // TODO: redis cache
        userRepository
            .findById(userId)
            .orElse(null)
            ?.accessLevel
            ?.value ?: -1
}
