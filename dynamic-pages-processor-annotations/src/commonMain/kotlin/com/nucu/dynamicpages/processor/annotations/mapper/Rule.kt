package com.nucu.dynamicpages.processor.annotations.mapper

import kotlin.reflect.KClass

/**
 * Annotation that allows the processor to identify rules.
 * This annotation must be in the property of final data class.
 *
 * @param rule Specified rule that extends from MapperRule.
 * @param from Origin property of parent model (optional).
 *
 * Note: Default values are not supported in multi node properties like data.item.name
 */
@Target(AnnotationTarget.PROPERTY)
annotation class Rule(
    val rule: KClass<out MapperRule<*, *>>,
    val from: String = ""
)