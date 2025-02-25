package com.intezya.abysscore.utils.validator

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Constraint(validatedBy = [ValidGameItemRarityValidator::class])
annotation class ValidGameItemRarity(
    val message: String = "Invalid rarity value",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
