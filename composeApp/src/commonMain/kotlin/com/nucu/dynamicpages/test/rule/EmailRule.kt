package com.nucu.dynamicpages.test.rule

import com.nucu.dynamicpages.processor.annotations.mapper.MapperRule
import org.koin.core.annotation.Factory

@Factory
class EmailRule (
    private val emailValidator: EmailValidator
): MapperRule<String?, Boolean> {
    override suspend fun map(data: String?): Boolean {
        return emailValidator.isValidEmail(data ?: "")
    }
}

@Factory
class EmailValidator {
    fun isValidEmail(email: CharSequence?): Boolean {
        val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()
        return email?.matches(emailRegex) == true
    }
}
