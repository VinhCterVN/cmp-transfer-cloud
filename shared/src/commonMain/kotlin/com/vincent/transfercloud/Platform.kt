package com.vincent.transfercloud

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform