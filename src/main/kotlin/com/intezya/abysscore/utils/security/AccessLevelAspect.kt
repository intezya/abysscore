package com.intezya.abysscore.utils.security

import com.intezya.abysscore.dto.user.UserAuthInfoDTO
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Aspect
@Component
class AccessLevelAspect {

    @Before("@annotation(com.intezya.abysscore.utils.security.RequiresAccessLevel)")
    fun checkAccessLevel(joinPoint: JoinPoint) {
        val signature = joinPoint.signature as MethodSignature
        val method = signature.method
        val requiredAccessLevel = method.getAnnotation(RequiresAccessLevel::class.java).level

        val userAuthData = SecurityContextHolder.getContext().authentication.principal as? UserAuthInfoDTO
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required")

        if (userAuthData.accessLevel < requiredAccessLevel.value) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Access denied. Required level: ${requiredAccessLevel.name}, your level: ${userAuthData.accessLevel}"
            )
        }
    }
}
