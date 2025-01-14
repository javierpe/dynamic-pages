package com.nucu.dynamicpages.processor.serialization

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.nucu.dynamicpages.processor.data.extensions.getRenderTypesNames
import com.nucu.ksp.common.definitions.DefinitionNames
import com.nucu.ksp.common.extensions.create
import com.nucu.ksp.common.extensions.getVerticalName
import com.nucu.ksp.common.extensions.includeDefaultSerializer
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import kotlinx.serialization.json.Json

class ComponentSerializerCreator(
    private val codeGenerator: CodeGenerator,
    private val options: Map<String, String>,
) {

    fun makeComponentSerializer(
        defaultDynamicComponent: KSClassDeclaration,
        dynamicPage: KSClassDeclaration,
        validatedSymbols: List<KSClassDeclaration>,
        dependencies: Dependencies
    ) {
        val fileName = options.getVerticalName() + DefinitionNames.COMPONENT_SERIALIZER_FILE_NAME
        val includeDefaultSerializer = options.includeDefaultSerializer()

        val fileSpec = FileSpec.builder(
            packageName = DefinitionNames.PACKAGE_MODELS,
            fileName = fileName
        )

        val polymorphicNames = validatedSymbols.getRenderTypesNames()

        val defaultSerializerSentence = if (includeDefaultSerializer) {
            "defaultDeserializer { ${defaultDynamicComponent.simpleName.getShortName()}.serializer() }"
        } else {
            ""
        }
        fileSpec.addType(
            TypeSpec.objectBuilder(fileName)
                .addProperty(
                    PropertySpec
                        .builder("serializerComponent", Json::class)
                        .initializer(
                            CodeBlock.of(
                                "Json { \nserializersModule = SerializersModule {" +
                                    " \npolymorphic(${dynamicPage.simpleName.getShortName()}::class) { \n${
                                        polymorphicNames.joinToString {
                                            "\nsubclass($it::class)"
                                        }.replace(",", "").trim()
                                    } \n$defaultSerializerSentence" +
                                    "\nignoreUnknownKeys = true " +
                                    "\nisLenient = true" +
                                    "\ncoerceInputValues = true" +
                                    "\nencodeDefaults = true} } }"
                            )
                        )
                        .mutable(false)
                        .build()
                )
                .build()
        )
            .addImport(
                "kotlinx.serialization",
                listOf(
                    "json.Json",
                    "modules.SerializersModule",
                    "modules.polymorphic",
                    "modules.subclass"
                )
            )
            .addImport(
                packageName = defaultDynamicComponent.packageName.asString(),
                names = listOf(defaultDynamicComponent.simpleName.asString())
            )
            .addImport(
                packageName = dynamicPage.packageName.asString(),
                names = listOf(dynamicPage.simpleName.asString())
            )


        fileSpec.create(codeGenerator, dependencies)
    }
}