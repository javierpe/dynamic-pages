package com.nucu.dynamicpages.processor.annotations.visitor

import kotlin.reflect.KClass

/**
 * This annotation is used to mark a property of a class as a visitable object.
 * otherwise only the main class of [Visitor] annotation.
 * @param visitor is the class that implements [Visitor]
 */
@Target(AnnotationTarget.FIELD)
annotation class VisitableProperty(val visitor: KClass<out Visitor<*, *>>)
