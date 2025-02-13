package com.nucu.dynamicpages.visitor.processor.creators

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.validate
import com.nucu.dynamicpages.processor.annotations.visitor.VisitableProperty
import com.nucu.dynamicpages.visitor.processor.model.VisitableData
import com.nucu.ksp.common.contract.ModuleCreatorContract
import com.nucu.ksp.common.definitions.DefinitionNames
import com.nucu.ksp.common.extensions.create
import com.nucu.ksp.common.extensions.filterByAnnotation
import com.nucu.ksp.common.extensions.getDependencies
import com.nucu.ksp.common.extensions.getModulePrefixName
import com.nucu.ksp.common.extensions.getValueArgOf
import com.nucu.ksp.common.extensions.isListType
import com.nucu.ksp.common.extensions.logLooking
import com.nucu.ksp.common.extensions.logNotFound
import com.nucu.ksp.common.extensions.toParameterName
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName

private const val PROCESS_NAME = "VisitorModuleCreator"
private const val PACKAGE_JAVAX_INJECT = "javax.inject.Inject"

class VisitorModuleCreator(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>,
) : ModuleCreatorContract {

    override suspend fun start(resolver: Resolver): List<KSAnnotated> {
        return VisitableProperty::class.qualifiedName?.let { name ->
            logger.logLooking(PROCESS_NAME)
            val resolved = resolver
                .getSymbolsWithAnnotation(name)
                .toList()

            if (resolved.isNotEmpty()) {
                makeComponent(
                    validatedSymbols = resolved.map { it as KSPropertyDeclaration },
                    dependencies = resolver.getDependencies()
                )
            } else {
                logger.logNotFound(PROCESS_NAME)
            }

            resolved.filterNot { it.validate() }.toList()
        } ?: emptyList()
    }

    private fun makeComponent(
        validatedSymbols: List<KSPropertyDeclaration>,
        dependencies: Dependencies
    ) {
        val visitableModels = mutableListOf<VisitableData>()

        // Get all visitable classes.
        validatedSymbols.forEach { prop ->

            val annotationArguments = prop
                .annotations
                .filterByAnnotation(VisitableProperty::class.java.simpleName)
                .first()

            // Get visitor class.
            val visitorClass = annotationArguments.arguments.getValueArgOf("visitor")
            val visitorClassDeclaration = (visitorClass as KSType).declaration as KSClassDeclaration

            val propertyType = prop.type.resolve()

            val isListType = propertyType.declaration.isListType()

            // Get class type if is list or single object.
            val visitableObjectProperty = if (isListType) {
                propertyType.arguments.first().type?.resolve()?.declaration as? KSClassDeclaration
            } else {
                propertyType.declaration as KSClassDeclaration
            }

            // Create visitable model that has all the data needed to generate the code.
            visitableObjectProperty?.let {
                visitableModels.add(
                    VisitableData(
                        mainClass = prop.parentDeclaration as KSClassDeclaration,
                        visitorClass = visitorClassDeclaration,
                        visitableObject = it,
                        visitableObjectPropertyName = prop.simpleName.getShortName(),
                        visitableObjectIsList = isListType
                    )
                )
            }
        }

        // Finally, generate the code.
        generateCode(
            visitableModels,
            dependencies
        )
    }

    @Suppress("NestedBlockDepth", "LongMethod")
    private fun generateCode(
        visitableData: List<VisitableData>,
        dependencies: Dependencies
    ) {
        val fileName = options.getModulePrefixName() + DefinitionNames.VISITOR_CLASS_NAME
        val fileSpec = FileSpec.builder(
            packageName = DefinitionNames.PACKAGE_VISITORS,
            fileName = fileName
        )
        fileSpec.apply {

            val properties = mutableListOf<PropertySpec>()

            // Generate code.
            visitableData.forEach { data ->
                val property = PropertySpec.Companion.builder(
                    data.visitorClass.toParameterName(),
                    data.visitorClass.toClassName()
                )
                    .initializer(data.visitorClass.toParameterName())
                    .addModifiers(KModifier.PRIVATE)
                    .build()

                if (properties.contains(property).not()) {
                    properties.add(property)
                }

                addImport(data.mainClass.toClassName(), listOf(""))
            }

            val constructor = FunSpec.constructorBuilder()
                .addAnnotation(ClassName.bestGuess(PACKAGE_JAVAX_INJECT))
                .apply {
                    visitableData.forEach { data ->
                        val parameter = ParameterSpec.Companion.builder(
                            data.visitorClass.toParameterName(),
                            data.visitorClass.toClassName()
                        ).build()
                        if (!parameters.contains(parameter)) {
                            addParameter(parameter)
                        }
                    }
                }
                .build()

            addType(
                TypeSpec.classBuilder(fileName)
                    .addProperties(properties)
                    .primaryConstructor(constructor)
                    .addFunction(
                        FunSpec.builder("invoke")
                            .returns(ANY)
                            .addParameter("resource", ANY)
                            .addParameter(
                                name = "updatedValue",
                                type = ANY
                            )
                            .addModifiers(KModifier.OPERATOR)
                            .addModifiers(KModifier.SUSPEND)
                            .beginControlFlow("return when (resource)")
                            .apply {
                                createCodeBlock(
                                    fileSpec = fileSpec,
                                    funSpec = this,
                                    visitableData = visitableData
                                )
                            }
                            .addStatement("else -> resource")
                            .endControlFlow()
                            .build()
                    )
                    .build()
            )
        }

        fileSpec.create(codeGenerator, dependencies)
    }

    @Suppress("NestedBlockDepth")
    private fun createCodeBlock(
        fileSpec: FileSpec.Builder,
        funSpec: FunSpec.Builder,
        visitableData: List<VisitableData>
    ) {
        funSpec.apply {
            visitableData.forEach { data ->

                val implementationType = data.visitorClass
                    .superTypes
                    .first()
                    .resolve()
                    .arguments[0].type!!.resolve().toClassName()

                fileSpec.addImport(implementationType, listOf(""))

                val startCodeBlock = CodeBlock.builder()
                    .addStatement("is ${data.mainClass.simpleName.getShortName()} ->")

                val startStatement = if (data.visitableObjectIsList) {
                    "resource.${data.visitableObjectPropertyName}.map { value ->"
                } else {
                    ""
                }

                val additionalCenterStatement = if (data.visitableObjectIsList) {
                    "value"
                } else {
                    "resource"
                }

                val parameterName = data.visitorClass.toParameterName()

                val centerStatement = "${parameterName}.visitValue(updatedValue, $additionalCenterStatement)"

                startCodeBlock
                    .indent()
                    .addStatement("resource.copy(")
                    .apply {
                        val operation = "${data.visitableObjectPropertyName} = $startStatement".trim()
                        if (data.visitableObjectIsList) {
                            indent()
                            beginControlFlow(operation)
                        } else {
                            addStatement(operation)
                        }
                    }
                    .addStatement(centerStatement)
                    .apply {
                        if (data.visitableObjectIsList) {
                            unindent()
                            endControlFlow()
                            addStatement(".distinct()")
                            unindent()
                        }
                    }
                    .addStatement(")")

                addCode(startCodeBlock.build())
            }
        }
    }
}
