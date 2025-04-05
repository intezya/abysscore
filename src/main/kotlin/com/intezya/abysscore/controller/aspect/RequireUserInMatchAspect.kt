package com.intezya.abysscore.controller.aspect

import com.intezya.abysscore.controller.annotations.RequireUserInMatch
import com.intezya.abysscore.enum.DraftState
import com.intezya.abysscore.enum.MatchStatus
import com.intezya.abysscore.model.entity.user.User
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Aspect
@Component
class RequireUserInMatchAspect {
    @Around("@annotation(requireUserInMatch)")
    fun checkUserInMatch(joinPoint: ProceedingJoinPoint, requireUserInMatch: RequireUserInMatch): Any {
        val args = joinPoint.args

        val user = args.find { it is User } as User?
            ?: throw IllegalArgumentException("User not found in arguments")

        if ((user.currentMatch == null || user.currentMatch?.endedAt != null) && requireUserInMatch.expected) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not in a match")
        }

        if (user.currentMatch != null && !requireUserInMatch.expected) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "User is in a match")
        }

        if (
            requireUserInMatch.expected == true &&
            requireUserInMatch.matchStatus != MatchStatus.UNSET &&
            user.currentMatch?.status != requireUserInMatch.matchStatus
        ) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not in the expected match status")
        }

        if (
            requireUserInMatch.expected == true &&
            requireUserInMatch.draftState != DraftState.UNSET &&
            user.currentMatch?.draft?.currentState != requireUserInMatch.draftState
        ) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not in the expected draft state")
        }

        return joinPoint.proceed(args)
    }
}
