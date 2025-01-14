package com.nucu.dynamicpages.render.processor.creators.mapper

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.validate
import com.nucu.dynamicpages.processor.annotations.mapper.Mapper
import com.nucu.ksp.common.contract.ModuleCreatorContract
import com.nucu.ksp.common.definitions.DefinitionNames
import com.nucu.ksp.common.extensions.create
import com.nucu.ksp.common.extensions.getDependencies
import com.nucu.ksp.common.extensions.getValidSymbols
import com.nucu.ksp.common.extensions.getModulePrefixName
import com.nucu.ksp.common.extensions.logFinishProcess
import com.nucu.ksp.common.extensions.logLooking
import com.nucu.ksp.common.extensions.logNotFound
import com.nucu.ksp.common.extensions.logStartProcess
import com.nucu.dynamicpages.render.processor.data.extensions.extractMappersWithRenders
import com.nucu.dynamicpages.render.processor.data.extensions.withSuffixName
import com.nucu.dynamicpages.render.processor.data.models.MainDependencies
import com.nucu.dynamicpages.render.processor.data.models.MapperWithRenderModel
import com.nucu.ksp.common.extensions.getDependencyInjectionPlugin
import com.nucu.ksp.common.model.DependencyInjectionPlugin
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName

private const val PROCESS_NAME = "Render Mapper Creator"
private const val MAPPER_MAP_VALUE_NAME = "mapperMap"
private const val RULE_SUFFIX_NAME = "Rule"
private const val IGNORE_RULE_FUNC_NAME = "shouldBeIgnored"

class RenderMapperCreator(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : ModuleCreatorContract {
    override suspend fun start(resolver: Resolver): List<KSAnnotated> {
        return Mapper::class.qualifiedName?.let { name ->
            logger.logLooking(PROCESS_NAME)
            val resolvedMappers = resolver
                .getSymbolsWithAnnotation(name)
                .toList()
                .getValidSymbols()

            val mappersWithRenders = resolvedMappers.extractMappersWithRenders()

            if (resolvedMappers.isNotEmpty()) {
                create(
                    data = mappersWithRenders,
                    dependencies = resolver.getDependencies()
                )
            } else {
                logger.logNotFound(PROCESS_NAME)
            }

            val result = mappersWithRenders.flatMap {
                listOf(
                    it.resultMapperClass,
                    it.classToMap,
                )
            }

            result.filterNot { it.validate() }.toList()
        } ?: emptyList()
    }

    private fun create(
        data: List<MapperWithRenderModel>,
        dependencies: Dependencies
    ) {
        logger.logStartProcess(PROCESS_NAME)
        if (data.isNotEmpty()) {

            val prefixName = options.getModulePrefixName()

            val fileSpec = FileSpec.builder(
                packageName = DefinitionNames.PACKAGE_MAPPERS,
                fileName = prefixName + DefinitionNames.RENDER_MAPPER_CLASS
            )

            fileSpec.apply {
                // Create constructor
                val mainDependencies = createConstructor(data)

                val mapperMapType = MAP.parameterizedBy(
                    String::class.asClassName(),
                    LambdaTypeName.get(
                        returnType = ANY.copy(nullable = true),
                        parameters = listOf(
                            ParameterSpec.builder("", ANY)
                                .build()
                        )
                    ).copy(suspending = true)
                )


                val mapperMapProperty = PropertySpec.builder(MAPPER_MAP_VALUE_NAME, mapperMapType)
                    .initializer(
                        CodeBlock.builder()
                            .apply {
                                addStatement("mapOf(")
                                data.forEach { model ->
                                    addImport(model.classToMap.toClassName().packageName, model.classToMap.toClassName().simpleName)
                                    val castResource = "(resource as ${model.classToMap.simpleName.getShortName()})"

                                    model.renderTypes.forEach { render ->
                                        val mapSentence = model.classToMap.withSuffixName(true) +
                                            ".mapTo${model.resultMapperClass.simpleName.getShortName()}" + castResource

                                        if (model.ignoreRuleClass != null) {
                                            val paramName = model.ignoreRuleClass.withSuffixName(true, RULE_SUFFIX_NAME)
                                            val ignoreRuleStatement =
                                                "if ($paramName.$IGNORE_RULE_FUNC_NAME$castResource) { null } else { $mapSentence }"
                                            addStatement("\"$render\" to { resource -> $ignoreRuleStatement },")
                                        } else {
                                            addStatement("\"$render\" to { resource -> $mapSentence },")
                                        }
                                    }
                                }
                                addStatement(")")
                            }
                            .build()
                    )
                    .build()

                addType(
                    TypeSpec.classBuilder(prefixName + DefinitionNames.RENDER_MAPPER_CLASS)
                        .addProperties(mainDependencies.properties)
                        .primaryConstructor(mainDependencies.constructor.build())
                        .apply {
                            if (options.getDependencyInjectionPlugin() == DependencyInjectionPlugin.KOIN) {
                                addAnnotation(ClassName.bestGuess(DefinitionNames.PACKAGE_KOIN_FACTORY))
                            }
                            addProperty(mapperMapProperty)
                            createMainFunction(this)
                        }
                        .build()
                )
            }

            fileSpec.create(codeGenerator, dependencies)
        } else {
            logger.logNotFound(PROCESS_NAME)
        }
        logger.logFinishProcess()
    }

    private fun createMainFunction(
        typeSpec: TypeSpec.Builder
    ) {
        typeSpec.addFunction(
            FunSpec.builder("invoke")
                .returns(ANY.copy(nullable = true))
                .addParameter("resource", ANY)
                .addParameter("renderType", String::class)
                .addModifiers(KModifier.OPERATOR)
                .addModifiers(KModifier.SUSPEND)
                .addStatement("return $MAPPER_MAP_VALUE_NAME[renderType]?.invoke(resource)")
                .build()
        )
    }

    @Suppress("NestedBlockDepth")
    private fun createConstructor(
        data: List<MapperWithRenderModel>
    ): MainDependencies {
        val properties = mutableListOf<PropertySpec>()

        val primaryConstructor = FunSpec.constructorBuilder()
            .apply {

                if (options.getDependencyInjectionPlugin() == DependencyInjectionPlugin.INJECT) {
                    addAnnotation(ClassName.bestGuess(DefinitionNames.PACKAGE_JAVAX_INJECT))
                }

                data.forEach { model ->

                    val mapper = ClassName(
                        DefinitionNames.PACKAGE_MAPPERS,
                        options.getModulePrefixName() + model.classToMap.withSuffixName()
                    )
                    if (model.ignoreRuleClass != null) {
                        val parameterIgnoreRule = ParameterSpec.builder(
                            model.ignoreRuleClass.withSuffixName(true, RULE_SUFFIX_NAME),
                            model.ignoreRuleClass.toClassName()
                        ).build()

                        if (!parameters.contains(parameterIgnoreRule)) {
                            addParameter(parameterIgnoreRule)
                            properties.add(
                                PropertySpec.builder(
                                    model.ignoreRuleClass.withSuffixName(true, RULE_SUFFIX_NAME),
                                    model.ignoreRuleClass.toClassName()
                                )
                                    .initializer(model.ignoreRuleClass.withSuffixName(true, RULE_SUFFIX_NAME))
                                    .addModifiers(KModifier.PRIVATE)
                                    .build()
                            )
                        }
                    }

                    val parameterSpec = ParameterSpec.builder(
                        model.classToMap.withSuffixName(true),
                        mapper
                    ).build()

                    if (!parameters.contains(parameterSpec)) {
                        addParameter(parameterSpec)
                        properties.add(
                            PropertySpec.builder(
                                model.classToMap.withSuffixName(true),
                                mapper
                            )
                                .initializer(model.classToMap.withSuffixName(true))
                                .addModifiers(KModifier.PRIVATE)
                                .build()
                        )
                    }
                }
            }

        return MainDependencies(
            properties,
            primaryConstructor
        )
    }
}