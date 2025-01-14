package com.nucu.ksp.common.extensions

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueArgument
import com.google.devtools.ksp.validate
import com.nucu.ksp.common.definitions.DefinitionNames
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeName

/**
 * Check if annotated object is a KSClassDeclaration and is valid symbol.
 */
fun KSAnnotated.isValid(): Boolean {
    return this is KSClassDeclaration && this.validate()
}

/**
 * Transform KSClassDeclaration name to semantic name, e.g. BannerResponse
 */
fun KSClassDeclaration.semanticName(): String {
    return makeSemanticName(this.simpleName.asString())
}

/**
 * Check if KSClassDeclaration is a Kotlin Data Class.
 */
fun KSClassDeclaration.isDataClass(): Boolean {
    return modifiers.any { it.name == "DATA" }
}

/**
 * Convert string to snake case with capitalize words.
 */
fun String.camelCaseToSnakeCase(withSemanticName: Boolean = true): String {
    val pattern = "_[a-zA-Z]".toRegex()
    val name = lowercase().replace(pattern) {
        it.value.last().uppercase()
    }.replaceFirstChar { it.uppercase() }
    return if (withSemanticName) {
        makeSemanticName(name)
    } else {
        name
    }
}

/**
 * Concat name with Response to make semantic name.
 */
private fun makeSemanticName(name: String): String {
    return "${name}Parent"
}

/**
 * Create a multibinding dagger function.
 */
fun FunSpec.Builder.create(
    type: KSType,
    returnType: TypeName
): FunSpec {
    return addParameter(
        ParameterSpec(
            name = "factory",
            type = type.toTypeName()
        )
    )
        .addAnnotation(
            AnnotationSpec.builder(ClassName("dagger", listOf("Binds"))).build()
        )
        .addAnnotation(
            AnnotationSpec.builder(ClassName("dagger.multibindings", listOf("IntoSet"))).build()
        )
        .addModifiers(listOf(KModifier.ABSTRACT))
        .returns(returnType)
        .build()
}

/**
 * Extract value of argument by name.
 */
fun List<KSValueArgument>.getValueArgOf(paramName: String): Any? {
    return single { it.name?.getShortName() == paramName }.value
}

/**
 * Converts a KSDeclaration to a parameter name.
 */
fun KSDeclaration.toParameterName(): String {
    return toString().toParameterName()
}

/**
 * Format type to parameter name.
 */
fun KSType.toParameterName(): String {
    return toString().toParameterName()
}

/**
 * Converts a String to a parameter name.
 */
fun String.toParameterName() = replaceFirstChar { it.lowercase() }

/**
 * Filter annotations by name.
 */
fun Sequence<KSAnnotation>.filterByAnnotation(annotationName: String): Sequence<KSAnnotation> {
    return filter { it.shortName.asString() == annotationName }
}

/**
 * Find property by name.
 */
fun Sequence<KSPropertyDeclaration>.findProperty(name: String): KSPropertyDeclaration? {
    return this.firstOrNull { it.simpleName.getShortName() == name }
}

/**
 * Check if type is a collection or single.
 */
fun KSDeclaration.isListType(): Boolean {
    return qualifiedName?.getShortName() == "List"
}

/**
 * Return value of vertical name parameter.
 */
fun Map<String, String>.getVerticalName(): String {
    return getOrDefault(DefinitionNames.VERTICAL_NAME, "")
}

fun Map<String, String>.includeDefaultSerializer(): Boolean {
    return getOrDefault("include.default.serializer", "true").toBoolean()
}