package com.nucu.dynamicpages.test.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
