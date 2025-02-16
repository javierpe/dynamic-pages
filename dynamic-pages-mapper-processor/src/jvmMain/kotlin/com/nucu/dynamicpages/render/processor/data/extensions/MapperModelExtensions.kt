package com.nucu.dynamicpages.render.processor.data.extensions

import com.google.devtools.ksp.innerArguments
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.Nullability
import com.nucu.dynamicpages.render.processor.data.models.DeepNodeProperty
import com.nucu.dynamicpages.render.processor.data.models.MapperModel
import com.nucu.ksp.common.extensions.toParameterName

private const val PACKAGE_OF_LAZY_MAPPER = "com.nucu.dynamicpages.render.processor.core.utils.LazyMapper"

/**
 * Map any property between origin model to new model.
 */
@Suppress("LongParameterList")
fun MapperModel.mapProp(
    currentClassName: String,
    validatedSymbols: List<KSClassDeclaration>,
    deepNode: String = "",
    forceDeepNode: Boolean = false,
    onInjectInstance: (KSClassDeclaration) -> Unit,
    onAddPackage: (String) -> Unit
): String {
    return if (forceDeepNode) {
        mapForceDeepNode(deepNode)
    } else if (declaration != null) {
        // Map with specified rule.
        mapToRule(deepNode)
    } else if (propertyDeclaration.hasMapper(validatedSymbols) != null) {
        // Map to some mapper created.
        mapToMapper(deepNode, validatedSymbols, currentClassName)
    } else if (propertyDeclaration.typeOfListHasMapper(validatedSymbols)) {
        // Map to some mapper created for this list.
        mapToMapperList()
    } else if (canMapToDefaults(deepNode.isNotEmpty())) {
        // Map to defaults if is necessary
        mapToDefaults(deepNode)
    } else if (propertyDeclaration.isLazyMapper()) {
        mapToLazyMapper(onAddPackage, onInjectInstance)
    } else {
        // Property can not resolve by any method, maybe you can use @Rule to solve it.
        error(
            "Param $propertyDeclaration is null in origin but ${propertyDeclaration.parentDeclaration} is required. " +
                "Use a @Rule annotation to resolve it."
        )
    }
}

/**
 * Create an instance of LazyMapper with arguments.
 */
private fun MapperModel.mapToLazyMapper(
    onAddPackage: (String) -> Unit,
    onInjectInstance: (KSClassDeclaration) -> Unit
): String {
    val args = propertyDeclaration.type.resolve().arguments

    val firstArg = args.first().type?.resolve()?.declaration as KSClassDeclaration
    val secondArg = args[1].type?.resolve()?.declaration as KSClassDeclaration
    val thirdArg = args[2].type?.resolve()?.declaration as KSClassDeclaration

    // Inject rule
    onInjectInstance(thirdArg)

    // From parent model
    onAddPackage(firstArg.qualifiedName?.asString().orEmpty())
    // Result model
    onAddPackage(secondArg.qualifiedName?.asString().orEmpty())
    // Rule
    onAddPackage(thirdArg.qualifiedName?.asString().orEmpty())

    // Add import
    onAddPackage(PACKAGE_OF_LAZY_MAPPER)

    val argumentDefinition = "${firstArg.simpleName.getShortName()}, ${secondArg.simpleName.getShortName()}, ${thirdArg.simpleName.getShortName()}"
    return "LazyMapper<$argumentDefinition>(${thirdArg.toParameterName()}).apply { setInputData(${consumerType.toParameterName()}) }"
}

/**
 * Check if origin property type can be null.
 */
private fun KSPropertyDeclaration.isNullable() = type.resolve().nullability == Nullability.NULLABLE

/**
 * When the types are primitives.
 */
private fun MapperModel.mapToDefaults(deepNode: String = ""): String {
    return if (origin?.isNullable() == true) {
        // If origin property type is null
        buildString {
            append("${consumerType.toParameterName()}.$origin")
            append(origin.mapTypeToDefaults(propertyDeclaration.isNullable()))
        }
    } else if (deepNode.isNotEmpty()) {
        val prop = consumerType.extractPropFromDeepNode(deepNode, propertyDeclaration.simpleName.getShortName())
        if (prop?.isNullable() == true || prop == null) {
            buildString {
                append("${consumerType.toParameterName()}.$deepNode.$propertyDeclaration")
                append(propertyDeclaration.mapTypeToDefaults(propertyDeclaration.isNullable()))
            }
        } else {
            "${consumerType.toParameterName()}.$deepNode.$propertyDeclaration"
        }
    } else if (origin == null) {
        // If origin does not exist or its name does not match with the final model.
        propertyDeclaration.mapTypeToDefaults()
    } else {
        // The same property.
        "${consumerType.toParameterName()}.$origin"
    }
}

/**
 * Map from deep node.
 */
private fun MapperModel.mapForceDeepNode(deepNode: String): String {
    val lastProp = consumerType.extractLastProperty(deepNode)
    val lastSentence = if (lastProp.canBeNull) {
        " ${lastProp.propertyClass.asType(emptyList()).declaration.mapTypeToDefaults()}"
    } else {
        ""
    }
    return "${consumerType.toParameterName()}.$deepNode$lastSentence"
}

/**
 * Return the last property of a deep node.
 */
private fun KSType.extractLastProperty(deepNode: String): DeepNodeProperty {
    var lastDeclaration = declaration
    var hasNullableProp = false

    deepNode.replace("?", "")
        .split(".")
        .asSequence().forEach { prop ->
            val propertyDeclaration = (lastDeclaration as KSClassDeclaration).getAllProperties().firstOrNull {
                it.simpleName.getShortName() == prop
            }
            if (propertyDeclaration == null) {
                error("Sequence of $prop contains no element matching the predicate")
            }
            if (!hasNullableProp) {
                hasNullableProp = propertyDeclaration.isNullable()
            }
            lastDeclaration = propertyDeclaration.type.resolve().declaration
        }

    val validDeclaration = (lastDeclaration as? KSTypeAlias)?.type?.resolve()?.declaration ?: lastDeclaration

    return DeepNodeProperty(
        propertyClass = validDeclaration as KSClassDeclaration,
        canBeNull = hasNullableProp
    )
}

/**
 * Extract prop from a deep node.
 */
private fun KSType.extractPropFromDeepNode(
    mapFromAll: String,
    destinationPropSimpleName: String
): KSPropertyDeclaration? {

    var lastProps: Sequence<KSPropertyDeclaration> = emptySequence()
    var lastDeclaration: KSDeclaration? = declaration

    mapFromAll.takeIf { it.isNotEmpty() }?.split(".")?.asSequence()?.forEach { prop ->
        lastDeclaration = (lastDeclaration as? KSClassDeclaration)?.getAllProperties()?.firstOrNull {
            it.simpleName.getShortName() == prop
        }?.type?.resolve()?.declaration

        lastProps = (lastDeclaration as? KSClassDeclaration)?.getAllProperties().orEmpty()
    } ?: mapFromAll

    return lastProps.firstOrNull { it.simpleName.getShortName() == destinationPropSimpleName }
}

/**
 * Insert a previously created mapper.
 */
private fun MapperModel.mapToMapper(
    deepNode: String = "",
    validatedSymbols: List<KSClassDeclaration>,
    currentClassName: String
): String {

    // Default prop if deep note is empty, should be the same as the origin.
    val defaultProp = origin?.simpleName?.getShortName()?.let {
        ".$it"
    } ?: ""

    // Extract deep node properties.
    val props = processFromParam(
        deepNode,
        propertyDeclaration.type.resolve().declaration as KSClassDeclaration,
        propertyDeclaration.isNullable()
    ).takeIf {
        it.isNotEmpty()
    } ?: "${consumerType.toParameterName()}$defaultProp"

    // Extract the mapper of the property.
    val mapperParameterName = propertyDeclaration.getMapperType()

    // Check if prop is nullable to map its default value.
    val propOrIt = if (propertyDeclaration.isNullable()) {
        "it"
    } else {
        props
    }

    // Validate if mapper is not the same of current class that is building.
    val finalSentence = if (
        propertyDeclaration.typeIsMapper(validatedSymbols) &&
        currentClassName != (mapperParameterName as KSClassDeclaration).withSuffixName()
    ) {
        // Map to mapper function.
        "${mapperParameterName.withSuffixName(true)}.mapTo${propertyDeclaration.type.resolve().declaration.simpleName.getShortName()}($propOrIt)"
    } else {
        // Map to the same class function
        "mapTo${propertyDeclaration.type}($propOrIt)"
    }

    // Mappers can not receive null parameters.
    return if (propertyDeclaration.isNullable()) {
        "$props?.let { $finalSentence }".format()
    } else {
        finalSentence
    }
}

/**
 * Insert a previously created mapper for any list of models.
 */
private fun MapperModel.mapToMapperList(): String {
    val name = propertyDeclaration.type.resolve().innerArguments.first().type?.resolve()?.declaration.toString()
    val parentMapper = propertyDeclaration.getMapperFromTypeOfList()
    if (parentMapper != null) {
        // List property type has a mapper
        val isNullable = origin?.isNullable() == true
        val originSentenceName = origin?.simpleName?.getShortName()

        val defaults = if (isNullable) {
            // Can be null.
            "${origin?.mapTypeToDefaults(propertyDeclaration.isNullable())}"
        } else {
            ""
        }

        val finalOriginSentence = if (isNullable) {
            "$originSentenceName?"
        } else {
            originSentenceName
        }

        if (finalOriginSentence == null) {
            error("Please change name of param ${propertyDeclaration.simpleName.getShortName()} to the same name of origin!")
        }

        return "${consumerType.toParameterName()}.$finalOriginSentence.map{ ${parentMapper.toParameterName()}Mapper.mapTo$name(it) }$defaults"
    } else {
        // The list property type does not have a mapper so it cannot be mapped.
        error("The list of $name has not a mapper. Please set @Mapper annotation for $name.")
    }
}

/**
 * Insert rule mapper.
 */
private fun MapperModel.mapToRule(deepNode: String = ""): String {
    val fromProp = propertyDeclaration.annotations
        .filteredByRuleAnnotation()
        .first()
        .arguments
        .filterByFromProp().firstOrNull()
    val processedProp =
        processFromParam(
            chain = fromProp?.value?.toString().orEmpty(), consumerType.declaration as KSClassDeclaration,
            assignablePropertyIsNullable = propertyDeclaration.isNullable()
        )
    val param = if (fromProp != null) {
        if (deepNode.isNotEmpty() && processedProp.isNotEmpty()) {
            // Processes a deep node defined in the @Mapper
            // annotation with a property chain defined in the @Rule annotation.
            ".$deepNode.$processedProp"
        } else if (processedProp.isNotEmpty()) {
            // It only processes a property string defined in the @Rule annotation.
            ".$processedProp"
        } else {
            ""
        }
    } else {
        ""
    }

    // No compatible with models that has mappers yet.
    return if (origin?.isListType() == true && param.isNotEmpty()) {
        "${consumerType.toParameterName()}$param.map{ ${declaration.toString().replaceFirstChar { it.lowercase() }}.map(it) }"
    } else {
        "${
            declaration.toString().replaceFirstChar { it.lowercase() }
        }.map(${consumerType.toParameterName()}$param)"
    }
}

/**
 * Extract each property node and check if it is nullable to map its default value.
 */
private fun processFromParam(
    chain: String,
    declaration: KSClassDeclaration,
    assignablePropertyIsNullable: Boolean = false
): String {
    var currentDeclaration: KSClassDeclaration? = declaration
    val newChain = mutableListOf<String>()
    // The chain is transformed into a sequence of nodes.
    val chainSequence = chain.split(".").asSequence().filter { it.isNotEmpty() }

    chainSequence.forEachIndexed { index, node ->
        currentDeclaration?.let { cDeclaration ->
            val prop = getDeclarationForChain(cDeclaration, node)
            if (prop != null) {
                if (prop.isNullable()) {
                    // Prop can be null.
                    if (chainSequence.toList().size == index + 1) {
                        // If it is the last node to map, check its default value
                        newChain.add("$node ${prop.mapTypeToDefaults(assignablePropertyIsNullable)}")
                    } else {
                        // Only the null safety operator is added
                        newChain.add("$node?")
                    }
                } else {
                    // If it is not null then it is just added.
                    newChain.add(node)
                }
                currentDeclaration = prop.type.resolve().declaration as KSClassDeclaration
            } else {
                // If it is not null then it is just added.
                newChain.add(node)
            }
        }
    }
    return newChain.joinToString(separator = ".")
}

/**
 * Gets information about the ownership of a class based on a name.
 */
private fun getDeclarationForChain(declaration: KSClassDeclaration, from: String): KSPropertyDeclaration? {
    return declaration.getAllProperties().firstOrNull { it.qualifiedName?.getShortName() == from }
}
