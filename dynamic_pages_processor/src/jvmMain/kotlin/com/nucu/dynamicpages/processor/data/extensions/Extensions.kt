package com.nucu.dynamicpages.processor.data.extensions

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.nucu.dynamicpages.processor.annotations.render.RenderModel
import com.nucu.ksp.common.extensions.camelCaseToSnakeCase
import com.nucu.ksp.common.extensions.semanticName

/**
 * Return a list of render types names from
 */
fun List<KSClassDeclaration>.getRenderTypesNames(): List<String> {
    val polymorphicNames = mutableListOf<String>()

    forEach { ksAnnotated ->

        val arguments = ksAnnotated.annotations.firstOrNull { annotation ->
            annotation.shortName.asString() == RenderModel::class.simpleName
        }?.arguments?.first()!!.value as List<*>

        arguments.asSequence().forEach {
            val semanticName = if (arguments.size > 1) {
                (it as KSType).declaration.simpleName.asString().camelCaseToSnakeCase()
            } else {
                ksAnnotated.semanticName()
            }

            polymorphicNames.add(semanticName)
        }
    }

    return polymorphicNames
}