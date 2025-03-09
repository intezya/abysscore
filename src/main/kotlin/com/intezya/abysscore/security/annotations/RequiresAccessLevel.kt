package com.intezya.abysscore.security.annotations

import com.intezya.abysscore.enum.AccessLevel

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresAccessLevel(
    val level: AccessLevel,
)
