package com.nucu.dynamicpages.render.processor.processors

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.nucu.dynamicpages.render.processor.creators.mapper.MapperCreator
import com.nucu.dynamicpages.render.processor.creators.mapper.RenderMapperCreator
import com.nucu.ksp.common.creator.KoinModuleCreator
import com.nucu.ksp.common.definitions.DefinitionNames
import com.nucu.ksp.common.definitions.DefinitionNames.ENGINE_KEY
import com.nucu.ksp.common.extensions.getDependencyInjectionPlugin
import com.nucu.ksp.common.extensions.getModulePrefixName
import com.nucu.ksp.common.extensions.includeKoinModule
import com.nucu.ksp.common.extensions.logEndProcessor
import com.nucu.ksp.common.extensions.logStartProcessor
import com.nucu.ksp.common.model.DependencyInjectionPlugin
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlin.time.measureTime
import kotlin.time.toJavaDuration

private const val PROCESSOR_NAME = "Mapper Processor"
private const val KOIN_MODULE_PROCESSOR_NAME  = "Koin Mapper Module Creator"

internal class MapperProcessor(
    private val logger: KSPLogger,
    private val mapperCreator: MapperCreator,
    private val renderMapperCreator: RenderMapperCreator,
    private val koinModuleCreator: KoinModuleCreator,
    private val options: Map<String, String>,
    private val codeGenerator: CodeGenerator
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.logStartProcessor(PROCESSOR_NAME)
        val unableToProcess = mutableListOf<KSAnnotated>()

        val elapsedTime = measureTime {
            unableToProcess.addAll(make(resolver = resolver))
        }

        logger.logEndProcessor(PROCESSOR_NAME, elapsedTime.toJavaDuration().toMillis())
        return unableToProcess
    }

    private fun make(
        resolver: Resolver,
    ): List<KSAnnotated> = runBlocking {
        val processRenderMapperClass = options.getOrDefault(ENGINE_KEY, "false").toBoolean()
        logger.warn("$ENGINE_KEY: $processRenderMapperClass")

        val processedMappers = async { mapperCreator.start(resolver) }.await()

        val processedRenderMappers = if (processRenderMapperClass) {
            async { renderMapperCreator.start(resolver) }.await()
        } else emptyList()

        if (options.getDependencyInjectionPlugin() == DependencyInjectionPlugin.KOIN && options.includeKoinModule()) {
            logger.logStartProcessor(KOIN_MODULE_PROCESSOR_NAME)
            val elapsedTime = measureTime {
                koinModuleCreator.create(
                    name = options.getModulePrefixName() + DefinitionNames.KOIN_DYNAMIC_PAGES_MODULE_NAME,
                    codeGenerator = codeGenerator
                )
            }

            logger.logEndProcessor(
                processorName = KOIN_MODULE_PROCESSOR_NAME,
                duration = elapsedTime.toJavaDuration().toMillis()
            )
        }

        processedMappers + processedRenderMappers
    }
}
