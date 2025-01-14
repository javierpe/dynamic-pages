package com.nucu.ksp.common.extensions

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.writeTo

/**
 * Indentation for all Kotlin files.
 */
private const val INDENT = "    "

/**
 * Create file of any FileSpec builder.
 */
fun FileSpec.Builder.create(
    codeGenerator: CodeGenerator,
    dependencies: Dependencies
) {
    try {
        indent(INDENT).build().writeTo(
            codeGenerator = codeGenerator,
            dependencies = dependencies
        )
    } catch (exception: FileAlreadyExistsException) {
        exception.run { printStackTrace() }
    }
}