package com.nucu.ksp.common.model

enum class DependencyInjectionPlugin(
    val type: String
) {
    NONE("disabled"),
    KOIN("koin"),
    INJECT("inject")
}