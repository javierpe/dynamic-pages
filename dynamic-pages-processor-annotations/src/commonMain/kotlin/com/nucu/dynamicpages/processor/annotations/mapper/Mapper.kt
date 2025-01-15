package com.nucu.dynamicpages.processor.annotations.mapper

import kotlin.reflect.KClass

/**
 * Annotation that allows the processor to identify which data class is the source.
 * This annotation must be in the data class that we need as a result of the mapping.
 *
 * @param parent Data class origin
 * @param deepNode Deep node of the data class like data.otherObject
 * @param matchWith Render types used only for Dynamic List to map the data class.
 * @param ignoredByRule Rule to ignore mapping of this data class.
 */
@Target(AnnotationTarget.CLASS)
annotation class Mapper(
    val parent: KClass<*>,
    val deepNode: String = "",
    vararg val matchWith: String = [],
    val ignoredByRule: KClass<out IgnoreRule<*>> = DefaultIgnoreRule::class
)