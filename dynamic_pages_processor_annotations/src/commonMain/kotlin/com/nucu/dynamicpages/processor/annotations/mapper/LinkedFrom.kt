package com.nucu.dynamicpages.processor.annotations.mapper

@Target(AnnotationTarget.PROPERTY)
annotation class LinkedFrom(
    val deepNode: String
)
