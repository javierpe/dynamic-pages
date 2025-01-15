package com.nucu.ksp.common.contract

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 * Should be used by any module creator.
 */
interface ModuleComponentContract {

    /**
     * Receives a list of specified classes that previously are filtered by module annotation.
     *
     * @param validatedSymbols [KSClassDeclaration] list of definition classes.
     */
    suspend fun makeComponent(
        validatedSymbols: List<KSClassDeclaration>,
        dependencies: Dependencies
    ) { }
}