package com.nucu.dynamicpages.visitor.processor.model

import com.google.devtools.ksp.symbol.KSClassDeclaration

data class PaginateElement(
    val parentClass: KSClassDeclaration,
    val propertyName: String,
    val key: String
)
