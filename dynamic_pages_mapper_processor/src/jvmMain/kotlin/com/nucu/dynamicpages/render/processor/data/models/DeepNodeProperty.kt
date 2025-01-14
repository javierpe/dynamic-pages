package com.nucu.dynamicpages.render.processor.data.models

import com.google.devtools.ksp.symbol.KSClassDeclaration

data class DeepNodeProperty(
    val propertyClass: KSClassDeclaration,
    val canBeNull: Boolean
)
