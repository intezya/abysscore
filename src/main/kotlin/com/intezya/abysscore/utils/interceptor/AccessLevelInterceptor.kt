package com.intezya.abysscore.utils.interceptor

import com.intezya.abysscore.security.annotations.RequiresAccessLevel
import com.intezya.abysscore.security.dto.UserAuthInfoDTO
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.messaging.handler.HandlerMethod
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AccessLevelInterceptor : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        if (handler is HandlerMethod) {
            val requiredAccessLevel = handler.method.getAnnotation(RequiresAccessLevel::class.java)
            if (requiredAccessLevel != null) {
                val authData = SecurityContextHolder.getContext().authentication.principal as UserAuthInfoDTO

                if (authData.accessLevel < requiredAccessLevel.level.value) {
                    response.status = HttpServletResponse.SC_FORBIDDEN
                    response.contentType = "application/json"
                    response.writer.write("{\"error\":\"Access denied\"}")
                    return false
                }
            }
        }
        return true
    }
}
