package com._16minutes.liquiprime.env

interface EnvironmentVariableLoader {
    fun get(name: String): String?
}
