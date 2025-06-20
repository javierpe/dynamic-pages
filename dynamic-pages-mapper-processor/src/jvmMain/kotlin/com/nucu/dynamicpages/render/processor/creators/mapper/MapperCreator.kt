package com.nucu.dynamicpages.render.processor.creators.mapper

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.validate
import com.nucu.dynamicpages.processor.annotations.mapper.Mapper
import com.nucu.dynamicpages.render.processor.data.extensions.asType
import com.nucu.dynamicpages.render.processor.data.extensions.filterByParentProp
import com.nucu.dynamicpages.render.processor.data.extensions.filterByRuleProp
import com.nucu.dynamicpages.render.processor.data.extensions.filteredByLinkedFromAnnotation
import com.nucu.dynamicpages.render.processor.data.extensions.filteredByMapperAnnotation
import com.nucu.dynamicpages.render.processor.data.extensions.filteredByRuleAnnotation
import com.nucu.dynamicpages.render.processor.data.extensions.getMapperFromTypeOfList
import com.nucu.dynamicpages.render.processor.data.extensions.injectMapper
import com.nucu.dynamicpages.render.processor.data.extensions.mapProp
import com.nucu.dynamicpages.render.processor.data.extensions.typeIsMapper
import com.nucu.dynamicpages.render.processor.data.extensions.typeOfListHasMapper
import com.nucu.dynamicpages.render.processor.data.extensions.withSuffixName
import com.nucu.dynamicpages.render.processor.data.models.MainDependencies
import com.nucu.dynamicpages.render.processor.data.models.MapperModel
import com.nucu.ksp.common.contract.ModuleCreatorContract
import com.nucu.ksp.common.definitions.DefinitionNames
import com.nucu.ksp.common.extensions.create
import com.nucu.ksp.common.extensions.getDependencies
import com.nucu.ksp.common.extensions.getDependencyInjectionPlugin
import com.nucu.ksp.common.extensions.getModulePrefixName
import com.nucu.ksp.common.extensions.getValidSymbols
import com.nucu.ksp.common.extensions.logFinishProcess
import com.nucu.ksp.common.extensions.logLooking
import com.nucu.ksp.common.extensions.logNotFound
import com.nucu.ksp.common.extensions.logStartProcess
import com.nucu.ksp.common.extensions.toParameterName
import com.nucu.ksp.common.model.DependencyInjectionPlugin
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.withIndent

private const val SUFFIX_CLASS_NAME = "Mapper"
private const val MAPPER_ANNOTATION_PARAM_NAME = "parent"
private const val MAPPER_ANNOTATION_PARAM_DEEP_NODE_NAME = "deepNode"
private const val PREFIX_INNER_MAPPER_FUNCTION = "mapTo"
private const val RETURN_VALUE_NAME = "data"

private const val PROCESS_NAME = "Mapper"

class MapperCreator(
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

            if (resolvedMappers.isNotEmpty()) {
                makeComponent(
                    validatedSymbols = resolvedMappers.getValidSymbols(),
                    dependencies = resolver.getDependencies()
                )
            } else {
                logger.logNotFound(PROCESS_NAME)
            }

            resolvedMappers.filterNot { it.validate() }.toList()
        } ?: emptyList()
    }

    override suspend fun makeComponent(
        validatedSymbols: List<KSClassDeclaration>,
        dependencies: Dependencies
    ) {
        logger.logStartProcess(PROCESS_NAME)
        validatedSymbols.groupBy { declaration ->
            declaration
                .annotations
                .filteredByMapperAnnotation()
                .first()
                .arguments
                .single {
                    it.name?.asString() == MAPPER_ANNOTATION_PARAM_NAME
                }.value as KSType
        }.forEach {

            // Class name as parent model name + suffix name.
            val className = options.getModulePrefixName() + "${it.key}$SUFFIX_CLASS_NAME"

            val fileSpec = FileSpec.builder(
                packageName = DefinitionNames.PACKAGE_MAPPERS,
                fileName = className
            )

            fileSpec.apply {
                // Main constructor definition
                val mainDependencies = createConstructor(className, it, validatedSymbols)

                // Make class with all related mappers
                addType(
                    TypeSpec.classBuilder(className)
                        .apply {
                            if (options.getDependencyInjectionPlugin() == DependencyInjectionPlugin.KOIN) {
                                addAnnotation(
                                    ClassName.bestGuess(DefinitionNames.PACKAGE_KOIN_FACTORY)
                                )
                            }
                            it.value.forEach { ksClassDeclaration ->
                                // Each function mapper.
                                createFunction(
                                    currentClassName = className,
                                    mainDependencies = mainDependencies,
                                    fileSpec = fileSpec,
                                    typeSpec = this,
                                    ksClassDeclaration = ksClassDeclaration,
                                    validatedSymbols = validatedSymbols
                                )
                            }
                        }
                        .build()
                )
            }

            fileSpec.create(codeGenerator, dependencies)
        }
        logger.logFinishProcess()
    }

    @Suppress("NestedBlockDepth", "LongMethod")
    private fun createConstructor(
        currentClassName: String,
        data: Map.Entry<KSType, List<KSClassDeclaration>>,
        allSymbols: List<KSClassDeclaration>
    ): MainDependencies {
        val properties = mutableListOf<PropertySpec>()

        // Mapper constructor with rules injected.
        val primaryConstructor = FunSpec.constructorBuilder()
            .apply {
                if (options.getDependencyInjectionPlugin() == DependencyInjectionPlugin.INJECT) {
                    addAnnotation(ClassName.bestGuess(DefinitionNames.PACKAGE_JAVAX_INJECT))
                }
                data.value.forEach { ksClassDeclaration ->
                    ksClassDeclaration.getAllProperties().toList().forEach { item ->

                        // If prop list has a mapper (only for List)
                        if (item.typeOfListHasMapper(allSymbols)) {

                            // Inject mapper for this element.
                            val mapper = item.getMapperFromTypeOfList()

                            val mapperDeclaration = mapper as KSClassDeclaration
                            injectMapper(options.getModulePrefixName(), mapperDeclaration, properties)
                        }
                        // Check if only has a mapper, the priority is rule annotation.
                        else if (
                            item.typeIsMapper(allSymbols) &&
                            item.annotations.filteredByRuleAnnotation().toList().isEmpty()
                        ) {

                            val mapperAn = item.type
                                .resolve()
                                .declaration
                                .annotations
                                .filteredByMapperAnnotation()
                                .first()

                            val mapperDeclaration = mapperAn.arguments
                                .filterByParentProp()
                                .first()
                                .asType()
                                .declaration as KSClassDeclaration
                            if (mapperDeclaration.withSuffixName() != currentClassName) {
                                injectMapper(options.getModulePrefixName(), mapperDeclaration, properties)
                            }
                        } else {
                            // If prop has a rule
                            item.annotations.filteredByRuleAnnotation().forEach { annotation ->
                                annotation.arguments.filterByRuleProp().first().let { valueArg ->
                                    val rule = valueArg.asType()
                                    val parameterSpec = ParameterSpec.builder(
                                        rule.toParameterName(),
                                        rule.toClassName()
                                    ).build()

                                    if (!parameters.contains(parameterSpec)) {
                                        addParameter(parameterSpec)
                                        properties.add(
                                            PropertySpec.builder(
                                                rule.toParameterName(),
                                                rule.toClassName()
                                            )
                                                .initializer(rule.toParameterName())
                                                .addModifiers(KModifier.PRIVATE)
                                                .apply {
                                                    if (options.getDependencyInjectionPlugin() == DependencyInjectionPlugin.KOIN) {
                                                        addAnnotation(
                                                            ClassName.bestGuess(DefinitionNames.PACKAGE_KOIN_PROVIDED)
                                                        )
                                                    }
                                                }
                                                .build()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

        return MainDependencies(
            properties = properties,
            constructor = primaryConstructor
        )
    }

    @Suppress("LongParameterList", "LongMethod")
    private fun createFunction(
        currentClassName: String,
        mainDependencies: MainDependencies,
        fileSpec: FileSpec.Builder,
        typeSpec: TypeSpec.Builder,
        ksClassDeclaration: KSClassDeclaration,
        validatedSymbols: List<KSClassDeclaration>
    ) {
        val constructorProperties = mainDependencies.properties.toMutableList()

        fileSpec.apply {
            val args = ksClassDeclaration.annotations.filteredByMapperAnnotation().first().arguments
            val deepNode = args
                .first { it.name?.getShortName() == MAPPER_ANNOTATION_PARAM_DEEP_NODE_NAME }
                .value
                ?.toString()

            val consumerType = args.single { it.name?.asString() == MAPPER_ANNOTATION_PARAM_NAME }.value as KSType
            val responseModel = (
                ksClassDeclaration.annotations.filteredByMapperAnnotation()
                    .firstOrNull()?.arguments?.first()?.value as? KSType
                )?.toTypeName()
            val returnType = ksClassDeclaration.asType(emptyList())

            val properties = mutableListOf<MapperModel>()

            val rules = mutableListOf<TypeName>()
            val namedRules = mutableListOf<String>()

            ksClassDeclaration.getAllProperties().toList().forEach { item ->
                val originProps = (consumerType.declaration as KSClassDeclaration).getAllProperties().toList()
                val origin = originProps.firstOrNull { prop -> prop.toString() == item.toString() }

                val fromProp = item.annotations
                    .filteredByLinkedFromAnnotation()
                    .firstOrNull()
                    ?.arguments
                    ?.firstOrNull()
                    ?.value as? String

                val ruleProp = (
                    item.annotations
                        .filteredByRuleAnnotation()
                        .firstOrNull()
                        ?.arguments
                        ?.firstOrNull()
                        ?.value as? KSType
                    )?.declaration

                properties.add(
                    MapperModel(
                        declaration = ruleProp,
                        propertyDeclaration = item,
                        origin = origin,
                        consumerType = consumerType,
                        fromDeepNode = fromProp
                    )
                )

                item.annotations.filteredByRuleAnnotation().forEach { annotation ->
                    rules.add(
                        annotation.arguments.filterByRuleProp().first().let { valueArg ->
                            val rule = valueArg.asType()
                            namedRules.add(rule.toClassName().simpleName)
                            rule.toTypeName()
                        }
                    )
                }
            }

            // Add necessary imports.
            addImport("", names = rules.map { it.toString() })
            if (responseModel != null) {
                addImport("", names = listOf(responseModel.toString()))
            }

            val returnPropList = properties.map { model ->
                val newDeepNode = model.fromDeepNode ?: deepNode.orEmpty()
                // The final sentence as result.
                val sentence = model.mapProp(
                    currentClassName,
                    validatedSymbols,
                    newDeepNode,
                    forceDeepNode = model.fromDeepNode != null,
                    onInjectInstance = { prop ->
                        // Create constructor parameter.
                        val parameterSpec = ParameterSpec.builder(
                            prop.toParameterName(),
                            prop.toClassName()
                        ).build()

                        if (!mainDependencies.constructor.parameters.contains(parameterSpec)) {
                            mainDependencies.constructor.addParameter(parameterSpec)
                            constructorProperties.add(
                                PropertySpec.builder(prop.toParameterName(), prop.toClassName())
                                    .initializer(prop.toParameterName())
                                    .addModifiers(KModifier.PRIVATE)
                                    .build()
                            )
                        }
                    }
                ) { packageName ->
                    // Add any import.
                    addImport("", names = listOf(packageName))
                }
                "${model.propertyDeclaration} = $sentence"
            }

            // Properties
            typeSpec.addProperties(constructorProperties)

            // Add constructor
            typeSpec.primaryConstructor(mainDependencies.constructor.build())

            // We need to define the return method to this function
            typeSpec.addFunction(
                FunSpec.builder("$PREFIX_INNER_MAPPER_FUNCTION$returnType")
                    .returns(returnType.toTypeName(TypeParameterResolver.EMPTY))
                    .addParameter(consumerType.toParameterName(), consumerType.toTypeName(TypeParameterResolver.EMPTY))
                    .addModifiers(KModifier.SUSPEND)
                    .addStatement("val $RETURN_VALUE_NAME = $returnType(")
                    .apply {
                        addCode(
                            CodeBlock.builder()
                                .withIndent {
                                    returnPropList.forEachIndexed { index, s ->
                                        if (index == returnPropList.size - 1) {
                                            addStatement(s)
                                        } else {
                                            addStatement("$s,")
                                        }
                                    }
                                }.build()
                        )
                    }
                    .addStatement(")")
                    .addStatement("return $RETURN_VALUE_NAME")
                    .build()
            )
        }
    }
}
