package com.intezya.abysscore.controller.annotations

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequireUserInMatch(
    val expectedThat: Boolean,
)
