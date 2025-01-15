package com.nucu.dynamicpages.processor.provider

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.nucu.dynamicpages.processor.serialization.ComponentSerializerCreator
import com.nucu.dynamicpages.processor.serialization.SerializedParentModelCreator
import com.nucu.dynamicpages.processor.serialization.SerializerModuleCreator
import com.nucu.dynamicpages.processor.processors.DynamicListProcessor

/**
 * The main entry of KSP.
 */
class DynamicPagesProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return DynamicListProcessor(
            logger = environment.logger,
            serializerModuleCreator = SerializerModuleCreator(
                SerializedParentModelCreator(
                    environment.codeGenerator,
                    environment.logger
                ),
                ComponentSerializerCreator(
                    environment.codeGenerator,
                    environment.options
                ),
                environment.logger
            )
        )
    }
}
