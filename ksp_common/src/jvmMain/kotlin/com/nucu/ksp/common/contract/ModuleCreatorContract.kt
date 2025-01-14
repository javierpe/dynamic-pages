package com.nucu.ksp.common.contract

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated

/**
 * Module creator interface that should be extend all creators.
 */
interface ModuleCreatorContract : ModuleComponentContract {

    /**
     * Should be receive all symbols and filter them by annotation.
     *
     * @param resolver [SymbolProcessor] with access to compiler details such as Symbols.
     * @return a unable list to processed class
     */
    suspend fun start(resolver: Resolver): List<KSAnnotated>
}