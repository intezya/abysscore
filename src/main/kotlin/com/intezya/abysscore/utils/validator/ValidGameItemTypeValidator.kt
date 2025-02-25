package com.intezya.abysscore.utils.validator

import com.intezya.abysscore.enum.GameItemType
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class ValidGameItemTypeValidator : ConstraintValidator<ValidGameItemType, GameItemType> {
    override fun isValid(value: GameItemType?, context: ConstraintValidatorContext?): Boolean {
        return value != null && value.value >= 0
    }
}
