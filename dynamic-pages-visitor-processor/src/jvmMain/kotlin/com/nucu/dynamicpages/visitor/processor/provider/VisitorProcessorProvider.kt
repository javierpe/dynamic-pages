package com.nucu.dynamicpages.visitor.processor.provider

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.nucu.dynamicpages.visitor.processor.creators.PaginateModuleCreator
import com.nucu.dynamicpages.visitor.processor.creators.VisitorModuleCreator
import com.nucu.dynamicpages.visitor.processor.processors.VisitorProcessor
import com.nucu.ksp.common.creator.KoinModuleCreator

/**
 * The main entry of KSP.
 */
class VisitorProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return VisitorProcessor(
            logger = environment.logger,
            visitorModuleCreator = VisitorModuleCreator(
                environment.codeGenerator,
                environment.logger,
                environment.options
            ),
            paginateModuleCreator = PaginateModuleCreator(
                environment.codeGenerator,
                environment.logger,
                environment.options
            ),
            koinModuleCreator = KoinModuleCreator(
                logger = environment.logger,
                codeGenerator = environment.codeGenerator,
                options = environment.options
            )
        )
    }
}
