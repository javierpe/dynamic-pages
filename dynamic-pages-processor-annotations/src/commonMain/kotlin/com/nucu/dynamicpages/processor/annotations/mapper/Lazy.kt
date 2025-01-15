package com.nucu.dynamicpages.processor.annotations.mapper

/**
 * With this annotation you tell the processor that the mapper creates an instance of LazyMapper
 * to execute the mapping on demand.
 */
@Target(AnnotationTarget.PROPERTY)
annotation class Lazy
