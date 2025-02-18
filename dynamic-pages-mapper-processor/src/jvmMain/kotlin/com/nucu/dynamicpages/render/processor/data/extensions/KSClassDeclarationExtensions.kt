package com.nucu.dynamicpages.render.processor.data.extensions

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.nucu.dynamicpages.render.processor.data.models.MapperWithOriginModel
import com.nucu.dynamicpages.render.processor.data.models.MapperWithRenderModel
import com.nucu.ksp.common.extensions.getValueArgOf
import com.squareup.kotlinpoet.ksp.toClassName

private const val RENDER_MODEL_ANNOTATION_PARENT_PARAM = "parent"
private const val RENDER_MODEL_ANNOTATION_IGNORE_RULE_PARAM = "ignoredByRule"
private const val MAPPER_ANNOTATION_TYPES_PARAM = "matchWith"
private const val DEFAULT_IGNORE_RULE_CLASS_NAME = "DefaultIgnoreRule"

/**
 * This function pack classes with Mapper annotation that his parent has a RenderModel annotation.
 */
fun List<KSClassDeclaration>.extractMappersWithRenders(): List<MapperWithRenderModel> {
    return map {
        MapperWithOriginModel(
            resultMapperClass = it,
            arguments = it.annotations.filteredByMapperAnnotation().first().arguments
        )
    }.distinct().map { model ->
        // Get parent class.
        val parentClass = model.arguments.getValueArgOf(RENDER_MODEL_ANNOTATION_PARENT_PARAM) as KSType

        // Get ignore rule class if exists.
        val ignoreRule = (model.arguments.getValueArgOf(RENDER_MODEL_ANNOTATION_IGNORE_RULE_PARAM) as? KSType).takeIf {
            it?.declaration?.simpleName?.getShortName() != DEFAULT_IGNORE_RULE_CLASS_NAME
        }

        // Get render types of parent class.
        val renderTypesOfParentClass = parentClass.declaration.annotations.filteredByRenderModelAnnotation().map {
            it.arguments.getValueArgOf(MAPPER_ANNOTATION_TYPES_PARAM)
        }.map { (it as List<*>).map { type -> type as String } }.flatten()

        val renders = renderTypesOfParentClass.filter { declaration ->
            val related = model.resultMapperClass.annotations
                .filteredByMapperAnnotation()
                .first()
                .arguments
                .getValueArgOf(MAPPER_ANNOTATION_TYPES_PARAM) as List<*>

            related.firstOrNull { found ->
                found == declaration
            } != null
        }

        MapperWithRenderModel(
            classToMap = parentClass.declaration as KSClassDeclaration,
            renderTypes = renders.distinct().toList(),
            resultMapperClass = model.resultMapperClass,
            ignoreRuleClass = ignoreRule?.declaration as? KSClassDeclaration
        )
    }.filter { it.renderTypes.isNotEmpty() }
}

/**
 * Add Mapper suffix to class name.
 */
fun KSClassDeclaration.withSuffixName(
    asParameter: Boolean = false,
    name: String = "Mapper"
): String {
    return (toClassName().simpleName + name).replaceFirstChar {
        if (asParameter) {
            it.lowercase()
        } else it.toString()
    }
}