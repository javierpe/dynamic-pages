package com.nucu.dynamicpages.visitor.processor.creators

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.validate
import com.nucu.dynamicpages.processor.annotations.visitor.Paginate
import com.nucu.dynamicpages.visitor.processor.model.PaginateElement
import com.nucu.ksp.common.contract.ModuleCreatorContract
import com.nucu.ksp.common.definitions.DefinitionNames
import com.nucu.ksp.common.extensions.create
import com.nucu.ksp.common.extensions.filterByAnnotation
import com.nucu.ksp.common.extensions.getDependencies
import com.nucu.ksp.common.extensions.getModulePrefixName
import com.nucu.ksp.common.extensions.isListType
import com.nucu.ksp.common.extensions.logLooking
import com.nucu.ksp.common.extensions.logNotFound
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName

private const val PROCESS_NAME = "PaginateModuleCreator"
private const val PACKAGE_JAVAX_INJECT = "javax.inject.Inject"

class PaginateModuleCreator(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : ModuleCreatorContract {
    override suspend fun start(resolver: Resolver): List<KSAnnotated> {
        return Paginate::class.qualifiedName?.let { name ->
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

    @Suppress("LongMethod")
    private fun makeComponent(
        validatedSymbols: List<KSPropertyDeclaration>,
        dependencies: Dependencies
    ) {

        // Check if properties are list type
        val invalidElements = validatedSymbols.filter {
            !it.type.resolve().declaration.isListType()
        }.map { it.simpleName.getShortName() }

        if (invalidElements.isNotEmpty()) {
            error(
                "The following properties cannot be paged: $invalidElements. Make sure the property is a list type!"
            )
        }

        // Create simplified list to process data.
        val paginateElements = validatedSymbols.map { prop ->
            val parentClass = prop.parentDeclaration as KSClassDeclaration
            val propertyName = prop.simpleName.getShortName()
            PaginateElement(
                parentClass,
                propertyName,
                prop.annotations.filterByAnnotation(Paginate::class.java.simpleName)
                    .first().arguments.first().value.toString()
            )
        }

        val name = options.getModulePrefixName() + DefinitionNames.PAGING_CLASS_NAME

        val fileSpec = FileSpec.builder(
            packageName = DefinitionNames.PACKAGE_VISITORS,
            fileName = name
        )

        fileSpec.apply {
            paginateElements.forEach { data ->
                addImport(data.parentClass.toClassName(), listOf(""))
            }

            val constructor = FunSpec.constructorBuilder()
                .addAnnotation(ClassName.bestGuess(PACKAGE_JAVAX_INJECT))
                .build()

            addType(
                TypeSpec.classBuilder(name)
                    .primaryConstructor(constructor)
                    .addFunction(
                        FunSpec.builder("invoke")
                            .returns(ANY)
                            .addParameter(
                                name = "oldValue",
                                type = ANY
                            )
                            .addParameter(
                                name = "updatedValue",
                                type = ANY
                            )
                            .addParameter(
                                name = "key",
                                type = String::class
                            )
                            .addModifiers(KModifier.OPERATOR)
                            .addModifiers(KModifier.SUSPEND)
                            .beginControlFlow("return when (oldValue)")
                            .apply {
                                paginateElements.forEach {
                                    val className = it.parentClass.toClassName().simpleName
                                    val validation = "if (key == oldValue.${it.key}) {"
                                    val lastSentence = "oldValue.copy(" +
                                        "${it.propertyName} = oldValue.${it.propertyName}" +
                                        ".plus((updatedValue as $className).${it.propertyName}).distinct()" +
                                        ")"
                                    val code = CodeBlock.builder()
                                        .addStatement("is $className -> ")
                                        .indent()
                                        .addStatement(validation)
                                        .indent()
                                        .addStatement(lastSentence)
                                        .unindent()
                                        .addStatement("} else { oldValue }")
                                        .unindent()
                                    addCode(code.build())
                                }
                            }
                            .addStatement("else -> oldValue")
                            .endControlFlow()
                            .build()
                    )
                    .build()
            )
        }

        fileSpec.create(codeGenerator, dependencies)
    }
}
