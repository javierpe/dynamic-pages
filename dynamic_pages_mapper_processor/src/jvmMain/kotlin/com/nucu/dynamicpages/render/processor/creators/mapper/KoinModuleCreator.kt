package com.nucu.dynamicpages.render.processor.creators.mapper

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.nucu.ksp.common.contract.ModuleCreatorContract
import com.nucu.ksp.common.definitions.DefinitionNames
import com.nucu.ksp.common.extensions.create
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec

class KoinModuleCreator(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : ModuleCreatorContract {

    override suspend fun start(resolver: Resolver): List<KSAnnotated> {

        val fileSpec = FileSpec.builder(
            packageName = DefinitionNames.PACKAGE_DI,
            fileName = DefinitionNames.KOIN_MODULE_NAME
        )

        fileSpec.apply {
            addType(
                TypeSpec.classBuilder(DefinitionNames.KOIN_MODULE_NAME)
                    .apply {
                        addAnnotation(
                            AnnotationSpec
                                .builder(ClassName.bestGuess(DefinitionNames.PACKAGE_KOIN_COMPONENT_SCAN))
                                .addMember(CodeBlock.of("\"${DefinitionNames.PACKAGE_ROOT}\""))
                                .build()
                        )
                    }
                    .build()
            )
        }

        fileSpec.create(codeGenerator, Dependencies.ALL_FILES)

        return emptyList()
    }
}