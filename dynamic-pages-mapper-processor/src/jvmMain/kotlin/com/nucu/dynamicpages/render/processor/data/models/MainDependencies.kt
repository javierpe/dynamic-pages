package com.nucu.dynamicpages.render.processor.data.models

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec

data class MainDependencies(
    val properties: List<PropertySpec>,
    val constructor: FunSpec.Builder
)
