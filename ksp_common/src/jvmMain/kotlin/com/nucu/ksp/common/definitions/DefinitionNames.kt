package com.nucu.ksp.common.definitions

object DefinitionNames {

    /**
     * Package of all generated classes. Path: build -> generated -> ksp -> debug | release
     */
    private const val PACKAGE_ROOT = "com.nucu.dynamicpages"

    /**
     * Package of data layer
     */
    private const val PACKAGE_DATA = "$PACKAGE_ROOT.data"

    /**
     * Package of Mappers.
     */
    const val PACKAGE_MAPPERS = "$PACKAGE_DATA.mappers"

    /**
     * Package of all data classes.
     */
    const val PACKAGE_MODELS = "$PACKAGE_DATA.models"

    const val PACKAGE_VISITORS = "$PACKAGE_DATA.visitors"

    /**
     * Serializer class name of dynamic list models.
     */
    const val COMPONENT_SERIALIZER_FILE_NAME = "ComponentSerializer"

    /**
     * All generated data models from Dynamic List responses.
     */
    const val PARENT_MODELS_CATALOG_FILE_NAME = "ResponseModels"

    /**
     * Main Dynamic List response definition.
     */
    const val PARENT_MODEL_FILE_NAME = "DynamicListComponentResponse"

    /**
     * Mapper that transform all data classes that has Render types.
     */
    const val RENDER_MAPPER_CLASS = "RenderMapperFactory"

    /**
     * Create a visitor class for all renders that has a @Visitable annotation.
     */
    const val VISITOR_CLASS_NAME = "DynamicListVisitorUseCase"

    /**
     * The paging engine use case
     */
    const val PAGINATOR_CLASS_NAME = "GetDynamicListPaginatorComponentUseCase"

    /**
     * Should be used to name any class to distinct it from other classes.
     */
    const val VERTICAL_NAME = "vertical.name"
}
