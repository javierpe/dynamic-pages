package com.nucu.dynamicpages.visitor.processor.processors

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.nucu.dynamicpages.visitor.processor.creators.PaginateModuleCreator
import com.nucu.dynamicpages.visitor.processor.creators.VisitorModuleCreator
import com.nucu.ksp.common.creator.KoinModuleCreator
import com.nucu.ksp.common.extensions.logEndProcessor
import com.nucu.ksp.common.extensions.logStartProcessor
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlin.time.measureTime
import kotlin.time.toJavaDuration

private const val PROCESSOR_NAME = "Visitor Processor"

internal class VisitorProcessor(
    private val logger: KSPLogger,
    private val visitorModuleCreator: VisitorModuleCreator,
    private val paginateModuleCreator: PaginateModuleCreator,
    private val koinModuleCreator: KoinModuleCreator
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
        val processedVisitors = async {
            visitorModuleCreator.start(resolver)
        }.await()

        val processedPaginationVisitors = async {
            paginateModuleCreator.start(resolver)
        }.await()

        async { koinModuleCreator.create() }.await()

        processedVisitors + processedPaginationVisitors
    }
}
