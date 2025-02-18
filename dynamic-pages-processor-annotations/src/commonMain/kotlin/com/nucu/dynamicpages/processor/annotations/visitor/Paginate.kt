package com.nucu.dynamicpages.processor.annotations.visitor

/**
 * This annotation allows the processor to generate code that adds elements to the specified field.
 * @param key Unique id of parent model to add new elements.
 */
@Target(AnnotationTarget.FIELD)
annotation class Paginate(val key: String)
