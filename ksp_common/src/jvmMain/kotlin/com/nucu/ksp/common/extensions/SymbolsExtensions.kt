package com.nucu.ksp.common.extensions

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.validate

/**
 * Return a list of a valid KSClassDeclaration symbols when processor found symbols with specified annotation.
 */
fun List<KSAnnotated>.getValidSymbols(): List<KSClassDeclaration> {
    return asSequence().filter { ksAnnotated ->
        ksAnnotated.validate() && ksAnnotated.isValid()
    }.filterIsInstance<KSClassDeclaration>().filter(KSNode::validate).toList()
}

fun Resolver.getDependencies(): Dependencies {
    return Dependencies(false, *getAllFiles().toList().toTypedArray())
}