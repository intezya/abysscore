package com.intezya.abysscore.controller.aspect

import com.intezya.abysscore.controller.annotations.RequireUserInMatch
import com.intezya.abysscore.model.entity.User
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Aspect
@Component
class RequireUserInMatchAspect {
    @Around("@annotation(com.intezya.abysscore.controller.annotations.RequireUserInMatch)")
    fun checkUserInMatch(joinPoint: ProceedingJoinPoint, requireUserInMatch: RequireUserInMatch): Any {
        val args = joinPoint.args

        val user = args.find { it is User } as User?
            ?: throw IllegalArgumentException("User not found in arguments")

        if (user.currentMatch == null && requireUserInMatch.expectedThat) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not in a match")
        }

        if (user.currentMatch != null && !requireUserInMatch.expectedThat) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "User is in a match")
        }

        return joinPoint.proceed(args)
    }
}
