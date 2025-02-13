package com.nucu.ksp.common.definitions

object DefinitionNames {

    /**
     * Package of all generated classes. Path: build -> generated -> ksp -> debug | release
     */
    const val PACKAGE_ROOT = "com.nucu.dynamicpages"

    /**
     * Package of data layer
     */
    private const val PACKAGE_DATA = "$PACKAGE_ROOT.data"

    /**
     * Package of Mappers.
     */
    const val PACKAGE_MAPPERS = "$PACKAGE_DATA.mappers"

    /**
     * Package for DI.
     */
    const val PACKAGE_DI = "$PACKAGE_ROOT.di"

    /**
     * Package of all data classes.
     */
    const val PACKAGE_MODELS = "$PACKAGE_DATA.models"

    const val PACKAGE_VISITORS = "$PACKAGE_DATA.visitors"

    /**
     * Serializer class name of dynamic page models.
     */
    const val COMPONENT_SERIALIZER_FILE_NAME = "ComponentSerializer"

    /**
     * All generated data models from Dynamic Page responses.
     */
    const val PARENT_MODELS_CATALOG_FILE_NAME = "DynamicPageResponseModels"

    /**
     * Mapper that transform all data classes that has Render types.
     */
    const val RENDER_MAPPER_CLASS = "RenderMapperFactory"

    /**
     * Create a visitor class for all renders that has a @Visitable annotation.
     */
    const val VISITOR_CLASS_NAME = "DynamicPageVisitorUseCase"

    /**
     * Should be used to name any class to distinct it from other classes.
     */
    const val MODULE_PREFIX = "DP_MODULE_PREFIX"

    /**
     * The paging engine use case
     */
    const val PAGING_CLASS_NAME = "GetDynamicListPagingComponentUseCase"

    /**
     * This key allows to create render mapper class.
     */
    const val ENGINE_KEY = "DP_RENDER_MAPPER_ENGINE"

    const val KEY_DEFAULT_SERIALIZER = "DP_INCLUDE_DEFAULT_SERIALIZER"

    const val KEY_INCLUDE_KOIN_MODULE = "DP_INCLUDE_KOIN_MODULE"

    const val DI_PLUGIN = "DP_DI_PLUGIN"

    const val PACKAGE_JAVAX_INJECT = "javax.inject.Inject"

    const val PACKAGE_KOIN_FACTORY = "org.koin.core.annotation.Factory"

    const val PACKAGE_KOIN_COMPONENT_SCAN = "org.koin.core.annotation.ComponentScan"

    const val PACKAGE_KOIN_MODULE = "org.koin.core.annotation.Module"

    const val KOIN_MODULE_NAME = "DynamicPagesModule"
}
