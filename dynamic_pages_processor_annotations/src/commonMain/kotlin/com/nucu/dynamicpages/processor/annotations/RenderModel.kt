package com.nucu.dynamicpages.processor.annotations

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class RenderModel(
    vararg val matchWith: String
)