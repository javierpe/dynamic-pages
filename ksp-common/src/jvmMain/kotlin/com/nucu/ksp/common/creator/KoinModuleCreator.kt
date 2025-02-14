package com.nucu.ksp.common.creator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.nucu.ksp.common.definitions.DefinitionNames
import com.nucu.ksp.common.extensions.create
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec

/**
 * Class that creates a Koin module.
 */
class KoinModuleCreator {

    /**
     * Create a Koin module with specific name.
     * @param name The name of the module.
     * @param codeGenerator The code generator.
     */
    fun create(
        codeGenerator: CodeGenerator,
        name: String
    ) {
        val fileSpec = FileSpec.builder(
            packageName = DefinitionNames.PACKAGE_DI,
            fileName = name
        )

        fileSpec.apply {
            addType(
                TypeSpec.classBuilder(name)
                    .apply {
                        addAnnotations(
                            listOf(
                                AnnotationSpec
                                    .builder(ClassName.bestGuess(DefinitionNames.PACKAGE_KOIN_MODULE))
                                    .build(),
                                AnnotationSpec
                                    .builder(ClassName.bestGuess(DefinitionNames.PACKAGE_KOIN_COMPONENT_SCAN))
                                    .addMember(CodeBlock.of("\"${DefinitionNames.PACKAGE_ROOT}\""))
                                    .build()
                            )
                        )
                    }
                    .build()
            )
        }

        fileSpec.create(codeGenerator, Dependencies.ALL_FILES)
    }
}
