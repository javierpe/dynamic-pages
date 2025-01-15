package com.nucu.dynamicpages.processor.processors

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.nucu.dynamicpages.processor.serialization.SerializerModuleCreator
import com.nucu.ksp.common.extensions.logEndProcessor
import com.nucu.ksp.common.extensions.logStartProcessor
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlin.time.measureTime
import kotlin.time.toJavaDuration

private const val PROCESSOR_NAME = "Dynamic List Processor"

internal class DynamicListProcessor(
    private val logger: KSPLogger,
    private val serializerModuleCreator: SerializerModuleCreator
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
        val unableSerializers = async {
            serializerModuleCreator.start(resolver)
        }.await()

        unableSerializers
    }
}
