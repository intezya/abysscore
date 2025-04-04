package com.intezya.abysscore.utils.annotation.meta

import com.fasterxml.jackson.annotation.JsonProperty
import java.lang.annotation.Inherited

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@JsonProperty("message_subtype")
annotation class InheritableMessageSubtype
