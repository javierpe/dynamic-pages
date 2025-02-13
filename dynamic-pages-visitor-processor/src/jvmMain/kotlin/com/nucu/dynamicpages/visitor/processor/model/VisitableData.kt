package com.nucu.dynamicpages.visitor.processor.model

import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 * Visitable data that will be used to generate the visitor code.
 */
data class VisitableData(
    val mainClass: KSClassDeclaration,
    val visitorClass: KSClassDeclaration,
    val visitableObject: KSClassDeclaration? = null,
    val visitableObjectPropertyName: String,
    val visitableObjectIsList: Boolean = false
)
