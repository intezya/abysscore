package com.intezya.abysscore.utils.security

import com.intezya.abysscore.enum.AccessLevel

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresAccessLevel(val level: AccessLevel)
