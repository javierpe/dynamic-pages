package com.nucu.dynamicpages.render.processor.data.extensions

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.nucu.ksp.common.definitions.DefinitionNames
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec

/**
 * Inject mapper class in current class that is building.
 */
fun FunSpec.Builder.injectMapper(
    verticalName: String,
    mapper: KSClassDeclaration,
    properties: MutableList<PropertySpec>
) {
    val mapperParam = mapper.withSuffixName(true)
    val mapperType = verticalName + mapper.withSuffixName()
    val mapperClassName = ClassName.bestGuess(
        "${DefinitionNames.PACKAGE_MAPPERS}.$mapperType"
    )

    val parameterSpec = ParameterSpec.builder(
        mapperParam,
        mapperClassName
    ).build()

    if (!parameters.contains(parameterSpec)) {
        addParameter(parameterSpec)
        properties.add(
            PropertySpec.builder(mapperParam, mapperClassName)
                .initializer(mapperParam)
                .addModifiers(KModifier.PRIVATE)
                .build()
        )
    }
}