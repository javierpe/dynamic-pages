package com.nucu.dynamicpages.processor.serialization

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.nucu.dynamicpages.processor.annotations.render.DefaultDynamicComponent
import com.nucu.dynamicpages.processor.annotations.render.DynamicPage
import com.nucu.dynamicpages.processor.annotations.render.RenderModel
import com.nucu.ksp.common.contract.ModuleCreatorContract
import com.nucu.ksp.common.extensions.getDependencies
import com.nucu.ksp.common.extensions.getValidSymbols
import com.nucu.ksp.common.extensions.logFinishProcess
import com.nucu.ksp.common.extensions.logLooking
import com.nucu.ksp.common.extensions.logNotFound
import com.nucu.ksp.common.extensions.logStartProcess
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

private const val PROCESS_NAME = "Serialized Modules"
private const val DYNAMIC_PAGE_MESSAGE = "Dynamic Page not found, please set @DynamicPage annotation"
private const val DEFAULT_COMPONENT_MESSAGE = "Dynamic Component not found, please set @DefaultDynamicComponent annotation"
private const val SERIALIZED_PARENT_PROCESS = "Serialized Parent"
private const val COMPONENT_SERIALIZED_PROCESS = "Component Serialized"

class SerializerModuleCreator(
    private val serializedParentModelCreator: SerializedParentModelCreator,
    private val componentSerializerCreator: ComponentSerializerCreator,
    private val logger: KSPLogger
) : ModuleCreatorContract {

    override suspend fun start(resolver: Resolver): List<KSAnnotated> {
        return RenderModel::class.qualifiedName?.let { name ->
            logger.logLooking(PROCESS_NAME)
            val resolved = resolver
                .getSymbolsWithAnnotation(name)
                .toList()

            // Get Dynamic Page template
            val dynamicPage = DynamicPage::class.qualifiedName?.let { page ->
                resolver.getSymbolsWithAnnotation(page).toList()
                    .takeIf { it.isNotEmpty() }?.getValidSymbols()?.firstOrNull()
            }

            if (dynamicPage == null) {
                logger.logNotFound(DYNAMIC_PAGE_MESSAGE)
                return emptyList()
            }

            // Get Default Dynamic Component
            val defaultComponent = DefaultDynamicComponent::class.qualifiedName?.let { component ->
                resolver.getSymbolsWithAnnotation(component)
                    .toList()
                    .takeIf { it.isNotEmpty() }?.getValidSymbols()?.firstOrNull()
            }

            if (defaultComponent == null) {
                logger.logNotFound(DEFAULT_COMPONENT_MESSAGE)
                return emptyList()
            }

            if (resolved.isNotEmpty()) {
                makeCustomComponent(
                    defaultDynamicComponent = defaultComponent,
                    dynamicPage = dynamicPage,
                    validatedSymbols = resolved.getValidSymbols(),
                    dependencies = resolver.getDependencies()
                )
            } else {
                logger.logNotFound(PROCESS_NAME)
            }

            resolved.filterNot { it.validate() }.toList()
        } ?: emptyList()
    }

    private fun makeCustomComponent(
        defaultDynamicComponent: KSClassDeclaration,
        dynamicPage: KSClassDeclaration,
        validatedSymbols: List<KSClassDeclaration>,
        dependencies: Dependencies
    ) = runBlocking {
        async {
            logger.logStartProcess(SERIALIZED_PARENT_PROCESS)
            serializedParentModelCreator.makeComponent(
                dynamicPage = dynamicPage,
                validatedSymbols = validatedSymbols,
                dependencies = dependencies
            )
            logger.logFinishProcess()
        }.await()

        async {
            logger.logStartProcess(COMPONENT_SERIALIZED_PROCESS)
            componentSerializerCreator.makeComponentSerializer(
                defaultDynamicComponent = defaultDynamicComponent,
                dynamicPage = dynamicPage,
                validatedSymbols = validatedSymbols,
                dependencies = dependencies
            )
            logger.logFinishProcess()
        }.await()
    }
}