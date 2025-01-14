package com.nucu.dynamicpages.render.processor.data.extensions

import com.google.devtools.ksp.innerArguments
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSValueArgument
import com.nucu.dynamicpages.processor.annotations.mapper.LinkedFrom
import com.nucu.dynamicpages.processor.annotations.mapper.Mapper
import com.nucu.dynamicpages.processor.annotations.mapper.Rule
import com.nucu.dynamicpages.processor.annotations.render.RenderModel
import com.nucu.ksp.common.extensions.filterByAnnotation
import com.squareup.kotlinpoet.ksp.toClassName

/**
 * Map param to default type when parent model has nullables properties or property of final model not match with origin.
 */
fun KSPropertyDeclaration.mapTypeToDefaults(
    assignablePropertyIsNullable: Boolean = false
): String {
    return if (assignablePropertyIsNullable) {
        ""
    } else {
        type.resolve().declaration.mapTypeToDefaults()
    }
}

fun KSDeclaration.mapTypeToDefaults(): String {
    return when (qualifiedName?.getShortName()) {
        "Int", "Long" -> "?: 0"
        "String" -> "?: \"\""
        "Boolean" -> "?: false"
        "Float" -> "?: 0f"
        "Double" -> "?: 0.0"
        "List" -> "?: emptyList()"
        "Map" -> "?: emptyMap()"
        "HashMap" -> "?: hashMapOf()"
        else -> ""
    }
}

/**
 * Check if property is a list and type is not primitive.
 */
fun KSPropertyDeclaration.isListType(): Boolean {
    val type = type.resolve()
    val declaration = type.declaration

    if (declaration is KSTypeAlias) {
        val resolvedType = declaration.type.resolve()
        return isCollectionType(resolvedType)
    }

    return isCollectionType(type)
}

private fun isCollectionType(type: KSType): Boolean {
    val basicCollectionPackageName = "kotlin.collections"
    val qualifiedName = type.declaration.qualifiedName?.asString() ?: return false
    val isCollection = qualifiedName.startsWith(basicCollectionPackageName)

    val innerArgType = type.arguments.firstOrNull()?.type?.resolve()
    val innerArgPackageName = innerArgType?.declaration?.packageName?.asString()
    return isCollection &&
        innerArgPackageName != null &&
        innerArgPackageName != basicCollectionPackageName
}

/**
 * Check if property is a primitive type.
 */
fun KSPropertyDeclaration.isBasicType(): Boolean {
    val packageName = type.resolve().toClassName().packageName
    return packageName == "kotlin" || packageName == "kotlin.collections"
}

/**
 * Check if property has a Mapper created. This is to avoid creation of again an reuse it.
 */
fun KSPropertyDeclaration.hasMapper(
    validatedSymbols: List<KSClassDeclaration>
): KSClassDeclaration? {
    return validatedSymbols.firstOrNull {
        it.asType(emptyList()).declaration.toString() == type.resolve().declaration.qualifiedName?.getShortName()
    }
}

/**
 * Check if type is lazy mapper.
 */
fun KSPropertyDeclaration.isLazyMapper(): Boolean {
    return annotations.filterByLazyMapperAnnotation().toList().isNotEmpty()
}

/**
 * Check if the list item type has a mapper.
 */
fun KSPropertyDeclaration.typeOfListHasMapper(
    validatedSymbols: List<KSClassDeclaration>
): Boolean {
    return isListType() && validatedSymbols.firstOrNull {
        it.asType(emptyList()).declaration.toString() == type.resolve().innerArguments.first().type?.resolve()?.declaration.toString()
    } != null
}

/**
 * Check if this property has a mapper.
 */
fun KSPropertyDeclaration.typeIsMapper(
    validatedSymbols: List<KSClassDeclaration>
): Boolean {
    return validatedSymbols.firstOrNull { it.simpleName == type.resolve().declaration.simpleName } != null
}

/**
 * Check is value arg is a type.
 */
fun KSValueArgument.asType(): KSType {
    return (
        ((value as KSType).declaration as KSClassDeclaration)
            .asType(emptyList()).declaration as KSClassDeclaration
        ).asType(
        emptyList()
    )
}

/**
 * Filter annotations by [Rule].
 */
fun Sequence<KSAnnotation>.filteredByRuleAnnotation(): Sequence<KSAnnotation> {
    return filterByAnnotation(Rule::class.java.simpleName)
}

/**
 * Filter annotations by [LinkedFrom].
 */
fun Sequence<KSAnnotation>.filteredByLinkedFromAnnotation(): Sequence<KSAnnotation> {
    return filterByAnnotation(LinkedFrom::class.java.simpleName)
}

/**
 * Filter annotations by [RenderModel].
 */
fun Sequence<KSAnnotation>.filteredByRenderModelAnnotation(): Sequence<KSAnnotation> {
    return filterByAnnotation(RenderModel::class.java.simpleName)
}

/**
 * Filter annotations by [Lazy]
 */
fun Sequence<KSAnnotation>.filterByLazyMapperAnnotation(): Sequence<KSAnnotation> {
    return filterByAnnotation(Lazy::class.java.simpleName)
}

/**
 * Filter annotations by [Mapper]
 */
fun Sequence<KSAnnotation>.filteredByMapperAnnotation(): Sequence<KSAnnotation> {
    return filterByAnnotation(Mapper::class.java.simpleName)
}

/**
 * Filter by rule param name a list of values.
 */
fun List<KSValueArgument>.filterByRuleProp(): List<KSValueArgument> {
    return filter { it.name?.getShortName() == "rule" }
}

/**
 * Filter by parent prop name.
 */
fun List<KSValueArgument>.filterByParentProp(): List<KSValueArgument> {
    return filter { it.name?.getShortName() == "parent" }
}

/**
 * Filter by from param name a list of values.
 */
fun List<KSValueArgument>.filterByFromProp(): List<KSValueArgument> {
    return filter { it.name?.getShortName() == "from" }
}

/**
 * Checks if the list item type has a mapper.
 */
fun KSPropertyDeclaration.getMapperFromTypeOfList(): KSDeclaration? {
    return type.resolve().innerArguments.first().type?.resolve()?.declaration?.annotations
        ?.filteredByMapperAnnotation()?.first()?.arguments
        ?.filterByParentProp()?.first()?.asType()?.declaration
}

/**
 * Extract the mapper class of this property.
 */
fun KSPropertyDeclaration.getMapperType(): KSDeclaration {
    return type
        .resolve()
        .declaration
        .annotations
        .filteredByMapperAnnotation()
        .first()
        .arguments
        .filterByParentProp()
        .first()
        .asType()
        .declaration
}