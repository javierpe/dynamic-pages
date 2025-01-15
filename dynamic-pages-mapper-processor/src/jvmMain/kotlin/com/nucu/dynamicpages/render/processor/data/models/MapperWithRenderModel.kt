package com.nucu.dynamicpages.render.processor.data.models

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSValueArgument

/**
 * Data class that pack all the information needed to generate the mapper.
 */
data class MapperWithRenderModel(
    val classToMap: KSClassDeclaration,
    val renderTypes: List<String>,
    val resultMapperClass: KSClassDeclaration,
    val ignoreRuleClass: KSClassDeclaration? = null
)

/**
 * Data class that pack first classes with arguments to generate the mapper.
 */
data class MapperWithOriginModel(
    val resultMapperClass: KSClassDeclaration,
    val arguments: List<KSValueArgument>
)
