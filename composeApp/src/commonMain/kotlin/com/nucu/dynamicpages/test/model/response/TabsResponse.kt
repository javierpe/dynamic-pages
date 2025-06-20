package com.nucu.dynamicpages.test.model.response

import com.nucu.dynamicpages.processor.annotations.render.RenderModel
import com.nucu.dynamicpages.test.RenderType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@RenderModel(
    matchWith = [
        RenderType.TABS
    ]
)
@Serializable
data class TabsResponse(
    @SerialName("text") val text: String? = null
)
