package com.intezya.abysscore.utils.validator

import com.intezya.abysscore.enum.GameItemRarity
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class ValidGameItemRarityValidator : ConstraintValidator<ValidGameItemRarity, GameItemRarity> {
    override fun isValid(value: GameItemRarity?, context: ConstraintValidatorContext?): Boolean {
        return value != null && value.value >= 0
    }
}
