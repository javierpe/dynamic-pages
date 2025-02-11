package com.nucu.dynamicpages.test.model

import com.nucu.dynamicpages.processor.annotations.mapper.IgnoreRule
import com.nucu.dynamicpages.processor.annotations.mapper.LinkedFrom
import com.nucu.dynamicpages.processor.annotations.mapper.Mapper
import com.nucu.dynamicpages.processor.annotations.mapper.MapperRule
import com.nucu.dynamicpages.processor.annotations.mapper.Rule
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Factory

@Serializable
data class ContactResponse(
    @SerialName("name") val name: String? = null,
    @SerialName("email") val email: String? = null,
    @SerialName("address") val address: AddressResponse? = null,
    @SerialName("phone") val phone: PhoneResponse? = null
)

@Serializable
data class AddressResponse(
    @SerialName("street") val street: String? = null,
    @SerialName("cp") val cp: String? = null
)

@Serializable
data class PhoneResponse(
    val country: String? = null,
    val phone: String? = null
)

@Mapper(
    parent = ContactResponse::class,
    ignoredByRule = PhoneIgnoreRule::class
)
data class PhoneUi(
    @LinkedFrom("phone?.country") val country: String,
    val name: String
)


class PhoneIgnoreRule : IgnoreRule<PhoneResponse> {
    override suspend fun shouldBeIgnored(data: PhoneResponse): Boolean {
        return data.phone.isNullOrEmpty()
    }
}

@Mapper(
    parent = ContactResponse::class
)
data class ContactUi(
    val name: String,
    @LinkedFrom("address?.street") val addressStreet: String,
    @Rule(rule = CpRule::class, from = "address?.cp") val cp: String
)

@Factory
class CpRule : MapperRule<String?, String> {
    override suspend fun map(data: String?): String {
        return data.orEmpty()
    }
}
