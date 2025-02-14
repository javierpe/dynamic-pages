package com.nucu.ksp.common.creator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.nucu.ksp.common.definitions.DefinitionNames
import com.nucu.ksp.common.extensions.create
import com.nucu.ksp.common.extensions.getDependencyInjectionPlugin
import com.nucu.ksp.common.extensions.getModulePrefixName
import com.nucu.ksp.common.extensions.includeKoinModule
import com.nucu.ksp.common.extensions.logEndProcessor
import com.nucu.ksp.common.extensions.logNotFound
import com.nucu.ksp.common.extensions.logStartProcessor
import com.nucu.ksp.common.model.DependencyInjectionPlugin
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import kotlin.time.measureTime
import kotlin.time.toJavaDuration

private const val KOIN_MODULE_PROCESSOR_NAME = "Koin Module Creator"

/**
 * Class that creates a Koin module.
 */
class KoinModuleCreator(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
    private val options: Map<String, String>
) {

    /**
     * Create a Koin module
     */
    suspend fun create() {
        logger.logStartProcessor(KOIN_MODULE_PROCESSOR_NAME)
        if (options.getDependencyInjectionPlugin() == DependencyInjectionPlugin.KOIN && options.includeKoinModule()) {
            val elapsedTime = measureTime {
                val name = options.getModulePrefixName() + DefinitionNames.KOIN_DYNAMIC_PAGES_MODULE_NAME
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
            logger.logEndProcessor(
                processorName = KOIN_MODULE_PROCESSOR_NAME,
                duration = elapsedTime.toJavaDuration().toMillis()
            )
        } else {
            logger.logNotFound(KOIN_MODULE_PROCESSOR_NAME)
        }
    }
}
