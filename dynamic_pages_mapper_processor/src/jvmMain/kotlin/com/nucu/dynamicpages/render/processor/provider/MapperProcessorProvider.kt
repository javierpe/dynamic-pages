package com.nucu.dynamicpages.render.processor.provider

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.nucu.dynamicpages.render.processor.creators.mapper.KoinModuleCreator
import com.nucu.dynamicpages.render.processor.creators.mapper.MapperCreator
import com.nucu.dynamicpages.render.processor.creators.mapper.RenderMapperCreator
import com.nucu.dynamicpages.render.processor.processors.MapperProcessor

/**
 * The main entry of KSP.
 */
class MapperProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return MapperProcessor(
            logger = environment.logger,
            mapperCreator = MapperCreator(
                environment.codeGenerator,
                environment.logger,
                environment.options
            ),
            renderMapperCreator = RenderMapperCreator(
                environment.codeGenerator,
                environment.logger,
                environment.options
            ),
            koinModuleCreator = KoinModuleCreator(
                environment.codeGenerator,
                environment.logger,
                environment.options
            ),
            environment.options
        )
    }
}
