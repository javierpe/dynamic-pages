package com.nucu.dynamicpages.test.rule

import com.nucu.dynamicpages.processor.annotations.mapper.IgnoreRule
import com.nucu.dynamicpages.test.model.response.PhoneResponse
import org.koin.core.annotation.Factory

@Factory
class PhoneIgnoreRule : IgnoreRule<PhoneResponse> {
    override suspend fun shouldBeIgnored(data: PhoneResponse): Boolean {
        return data.phone.isNullOrEmpty()
    }
}
