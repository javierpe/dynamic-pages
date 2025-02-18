package com.nucu.dynamicpages.test.model.ui

import com.nucu.dynamicpages.processor.annotations.mapper.Mapper
import com.nucu.dynamicpages.processor.annotations.mapper.Rule
import com.nucu.dynamicpages.test.RenderType
import com.nucu.dynamicpages.test.model.response.TestResponse
import com.nucu.dynamicpages.test.rule.NameRule

@Mapper(
    parent = TestResponse::class,
    matchWith = [
        RenderType.TEXT
    ]
)
data class TestUi(
    @Rule(rule = NameRule::class, from = "name") val name: String
)
