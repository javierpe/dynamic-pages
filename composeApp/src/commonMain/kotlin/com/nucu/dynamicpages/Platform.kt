package com.nucu.dynamicpages

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform