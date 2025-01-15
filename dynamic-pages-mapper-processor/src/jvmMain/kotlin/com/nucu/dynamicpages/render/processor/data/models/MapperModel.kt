package com.nucu.dynamicpages.render.processor.data.models

import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.nucu.dynamicpages.render.processor.data.extensions.isBasicType

/**
 * @param declaration is the rule of property that can be null
 * @param propertyDeclaration is the property of simplified model
 * @param origin is the property of original model
 * @param consumerType is the type of annotation rule
 */
data class MapperModel(
    val declaration: KSDeclaration? = null,
    val propertyDeclaration: KSPropertyDeclaration,
    val origin: KSPropertyDeclaration? = null,
    val consumerType: KSType,
    val fromDeepNode: String? = null
) {

    /**
     * Check if property of simplified model can be null to map with default types.
     */
    fun canMapToDefaults(deepNodeMapperMode: Boolean): Boolean {
        return origin != null || propertyDeclaration.isBasicType() ||
            propertyDeclaration.isBasicType() && deepNodeMapperMode
    }
}
