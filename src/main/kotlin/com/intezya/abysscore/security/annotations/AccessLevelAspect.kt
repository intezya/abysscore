package com.intezya.abysscore.security.annotations

// import com.intezya.abysscore.security.dto.UserAuthInfoDTO

// @Aspect
// @Component
// class AccessLevelAspect {
//    @Before("@annotation(com.intezya.abysscore.security.annotations.RequiresAccessLevel)")
//    fun checkAccessLevel(joinPoint: JoinPoint) {
//        val signature = joinPoint.signature as MethodSignature
//        val method = signature.method
//        val requiredAccessLevel = method.getAnnotation(RequiresAccessLevel::class.java).level
//
//        val userAuthData =
//            SecurityContextHolder.getContext().authentication.principal as? UserAuthInfoDTO
//                ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required")
//
//        if (userAuthData.accessLevel < requiredAccessLevel.value) {
//            throw ResponseStatusException(
//                HttpStatus.FORBIDDEN,
//                "Access denied. Required level: ${requiredAccessLevel.name}, your level: ${userAuthData.accessLevel}",
//            )
//        }
//    }
// }
