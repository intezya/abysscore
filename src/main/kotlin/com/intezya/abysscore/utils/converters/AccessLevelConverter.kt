package com.intezya.abysscore.utils.converters

import com.intezya.abysscore.enum.AccessLevel
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class AccessLevelConverter : AttributeConverter<AccessLevel, Int> {
    override fun convertToDatabaseColumn(attribute: AccessLevel): Int {
        return attribute.value
    }

    override fun convertToEntityAttribute(dbData: Int): AccessLevel {
        return AccessLevel.entries
            .filter { it.value <= dbData }
            .maxByOrNull { it.value }
            ?: throw IllegalArgumentException("Unknown access level: $dbData")
    }
}
