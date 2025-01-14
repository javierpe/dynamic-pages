package com.nucu.dynamicpages.test.rule

import com.nucu.dynamicpages.processor.annotations.mapper.MapperRule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Factory
class NameRule : MapperRule<String?, String> {
    override suspend fun map(data: String?): String {
        return data.orEmpty().uppercase()
    }
}

@Module
@ComponentScan("com.nucu.dynamicpages.test")
class MainModule