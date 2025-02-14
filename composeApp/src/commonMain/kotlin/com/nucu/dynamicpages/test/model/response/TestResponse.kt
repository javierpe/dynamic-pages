package com.nucu.dynamicpages.test.model.response

import com.nucu.dynamicpages.processor.annotations.render.RenderModel
import com.nucu.dynamicpages.test.RenderType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@RenderModel(
    matchWith = [
        RenderType.TEXT
    ]
)
@Serializable
data class TestResponse(
    @SerialName("name") val name: String? = null
)
