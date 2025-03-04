package com.intezya.abysscore.utils.auth

import com.intezya.abysscore.model.dto.user.UserAuthInfoDTO
import com.intezya.abysscore.model.entity.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession
import java.util.*

@Component
class AuthUtils {
    @Value("\${jwt.secret}")
    private lateinit var jwtSecret: String

    @Value("\${jwt.expiration}")
    private var jwtExpirationMs: Int = 0

    fun generateJwtToken(user: User, accessLevel: Int = -1, extraExpirationMs: Int = jwtExpirationMs): String {
        return Jwts.builder()
            .setSubject(user.id.toString())
            .claim("service", "com.intezya.abysscore.auth")
            .claim("hwid", user.hwid)
            .claim("user", user.username)
            .claim("access_level", accessLevel)
            .setIssuedAt(Date())
            .setExpiration(Date(Date().time + extraExpirationMs))
            .signWith(Keys.hmacShaKeyFor(jwtSecret.toByteArray()), SignatureAlgorithm.HS512)
            .compact()
    }

    fun getClaimsFromJwtToken(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.toByteArray()))
            .build()
            .parseClaimsJws(token)
            .body
    }

    fun getUserInfoFromToken(token: String): UserAuthInfoDTO {
        val claims = getClaimsFromJwtToken(token)
        return UserAuthInfoDTO(
            id = claims["sub"].toString().toLong(),
            username = claims["user"].toString(),
            hwid = claims["hwid"].toString(),
            accessLevel = claims["access_level"].toString().toInt(),
        )
    }

    fun validateJwtToken(authToken: String): Boolean {
        try {
            getClaimsFromJwtToken(authToken)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun getClientIp(request: HttpServletRequest): String {
        val headers = listOf(
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_CLIENT_IP",
            "REMOTE_ADDR"
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
        val headers = listOf(
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_CLIENT_IP",
            "REMOTE_ADDR"
        )

        for (header in headers) {
            val ip = session.handshakeHeaders[header]?.firstOrNull()
            if (!ip.isNullOrEmpty() && ip != "unknown") {
                return ip.split(",").first().trim()
            }
        }
        return session.remoteAddress.toString()
    }
}
