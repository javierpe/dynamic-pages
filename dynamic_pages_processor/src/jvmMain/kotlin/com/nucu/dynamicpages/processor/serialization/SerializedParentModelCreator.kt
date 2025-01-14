package com.nucu.dynamicpages.processor.serialization

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.nucu.dynamicpages.processor.annotations.RenderModel
import com.nucu.ksp.common.definitions.DefinitionNames
import com.nucu.ksp.common.extensions.camelCaseToSnakeCase
import com.nucu.ksp.common.extensions.create
import com.nucu.ksp.common.extensions.isDataClass
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class SerializedParentModelCreator(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) {

    @Suppress("LongMethod", "NestedBlockDepth")
    fun makeComponent(
        dynamicPage: KSClassDeclaration,
        validatedSymbols: List<KSClassDeclaration>,
        dependencies: Dependencies
    ) {
        val fileSpec = FileSpec.builder(
            packageName = DefinitionNames.PACKAGE_MODELS,
            fileName = DefinitionNames.PARENT_MODELS_CATALOG_FILE_NAME
        )

        validatedSymbols.forEach { ksAnnotated ->
            if (ksAnnotated.isDataClass()) {
                val arguments = ksAnnotated.annotations.firstOrNull { annotation ->
                    annotation.shortName.asString() == RenderModel::class.simpleName
                }?.arguments?.first()!!.value as List<*>

                arguments.asSequence().forEach {
                    val arg = it as String

                    fileSpec.addType(
                        create(
                            semanticName = ksAnnotated.simpleName.getShortName().camelCaseToSnakeCase(),
                            resourceTypeName = ksAnnotated.asType(emptyList()).toTypeName(),
                            serialName = arg,
                            dynamicPage = dynamicPage
                        )
                    )
                }
            }
        }

        fileSpec.create(codeGenerator, dependencies)
    }

    private fun create(
        dynamicPage: KSClassDeclaration,
        semanticName: String,
        resourceTypeName: TypeName,
        serialName: String? = null,
        isSerializable: Boolean = true
    ): TypeSpec {

        val parentClass = dynamicPage.toClassName()
        val params = dynamicPage.getAllProperties()

        val parameterSpecs = params.map { member ->
            val propertyName = member.simpleName.asString()
            val propertyType = member.type.resolve().toTypeName()
            val isNullable = propertyType.isNullable

            ParameterSpec.builder(
                name = propertyName,
                type = if (propertyName == POLYMORPHIC_PROPERTY_NAME) {
                    resourceTypeName.copy(nullable = isNullable)
                } else {
                    propertyType
                }
            ).apply {
                if (isNullable) {
                    defaultValue("null")
                }
            }.build()
        }

        val propSpecs = params.map { member ->
            val propertyName = member.simpleName.asString()
            val propertyType = member.type.resolve().toTypeName()
            val isNullable = propertyType.isNullable

            val serialNameAnnotation = member.annotations.find {
                it.annotationType.resolve().toClassName() == SerialName::class.asClassName()
            }?.arguments?.firstOrNull()?.value?.toString()

            PropertySpec
                .builder(
                    name = propertyName,
                    type = if (propertyName == POLYMORPHIC_PROPERTY_NAME) {
                        resourceTypeName.copy(nullable = isNullable)
                    } else {
                        propertyType
                    }
                )
                .apply {
                    serialNameAnnotation?.let {
                        addAnnotation(
                            AnnotationSpec.builder(SerialName::class)
                                .addMember("value = \"$it\"")
                                .build()
                        )
                    }
                }
                .initializer(propertyName)
                .addModifiers(KModifier.OVERRIDE)
                .build()
        }

        return TypeSpec.classBuilder(semanticName)
            .addModifiers(KModifier.DATA)
            .superclass(parentClass)
            .addProperties(propSpecs.toList())
            .primaryConstructor(
                primaryConstructor = FunSpec
                    .constructorBuilder()
                    .addParameters(parameterSpecs.toList())
                    .build()
            )
            .apply {
                if (isSerializable) {
                    addAnnotation(Serializable::class)
                }

                if (serialName != null) {
                    addAnnotation(
                        AnnotationSpec
                            .builder(SerialName::class)
                            .addMember("value = \"${serialName}\"")
                            .build()
                    )
                }
            }
            .build()
    }

    companion object {
        private const val POLYMORPHIC_PROPERTY_NAME = "resource"
    }
}