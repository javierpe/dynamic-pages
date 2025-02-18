package com.nucu.dynamicpages.test.model.ui

import com.nucu.dynamicpages.processor.annotations.mapper.LinkedFrom
import com.nucu.dynamicpages.processor.annotations.mapper.Mapper
import com.nucu.dynamicpages.processor.annotations.mapper.Rule
import com.nucu.dynamicpages.test.model.response.ContactResponse
import com.nucu.dynamicpages.test.rule.EmailRule
import com.nucu.dynamicpages.test.rule.PhoneIgnoreRule

@Mapper(
    parent = ContactResponse::class
)
data class ContactUi(
    val name: String,
    @LinkedFrom("address?.street") val addressStreet: String,
    @Rule(rule = EmailRule::class, from = "email") val hasValidEmail: Boolean
)

@Mapper(
    parent = ContactResponse::class,
    ignoredByRule = PhoneIgnoreRule::class
)
data class PhoneUi(
    @LinkedFrom("phone?.country") val country: String,
    val name: String
)
