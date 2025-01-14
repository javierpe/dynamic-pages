package com.nucu.dynamicpages.processor.annotations.render

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class RenderModel(
    vararg val matchWith: String
)