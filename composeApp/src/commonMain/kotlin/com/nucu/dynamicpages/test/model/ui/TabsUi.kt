package com.nucu.dynamicpages.test.model.ui

import com.nucu.dynamicpages.processor.annotations.mapper.Mapper
import com.nucu.dynamicpages.test.RenderType
import com.nucu.dynamicpages.test.model.response.TabsResponse

@Mapper(
    matchWith = [
        RenderType.TABS
    ],
    parent = TabsResponse::class
)
data class TabsUi(
    val text: String
)
