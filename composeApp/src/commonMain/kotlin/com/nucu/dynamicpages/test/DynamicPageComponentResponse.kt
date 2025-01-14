package com.nucu.dynamicpages.test

import com.nucu.dynamicpages.processor.annotations.DefaultDynamicComponent
import com.nucu.dynamicpages.processor.annotations.DynamicPage
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonElement

@DynamicPage
@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("render")
@Polymorphic
abstract class DynamicPageComponentResponse {
    @SerialName("index")
    abstract val index: Int

    @SerialName("render")
    abstract val render: String

    @SerialName("resource")
    abstract val resource: Any?

    @SerialName("is_sticky")
    abstract val isSticky: Boolean?
}

@DefaultDynamicComponent
@Serializable
data class UnregisteredComponent(
    override val index: Int,
    override val render: String,
    override val resource: JsonElement? = null,
    override val isSticky: Boolean? = false
) : DynamicPageComponentResponse()