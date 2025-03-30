package com.intezya.abysscore.security.interceptor

import com.intezya.abysscore.model.entity.user.User
import com.intezya.abysscore.security.annotations.RequiresAccessLevel
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AccessLevelInterceptor : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler is HandlerMethod) {
            val requiredAccessLevel = handler.method.getAnnotation(RequiresAccessLevel::class.java)
            if (requiredAccessLevel != null) {
                val authData = SecurityContextHolder.getContext().authentication.principal as User

                if (authData.accessLevel < requiredAccessLevel.level) {
                    throw ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Access denied. Required level: ${requiredAccessLevel.level.value}, your level: ${authData.accessLevel.value}",
                    )
                }
            }
        }
        return true
    }
}
