package com._16minutes.liquiprime.properties

interface SystemPropertyLoader {
    fun load(name: String): String?
}
