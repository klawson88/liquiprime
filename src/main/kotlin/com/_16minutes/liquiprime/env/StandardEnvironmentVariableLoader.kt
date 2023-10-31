package com._16minutes.liquiprime.env

class StandardEnvironmentVariableLoader: EnvironmentVariableLoader {
    override fun get(name: String): String? {
        return System.getenv(name)
    }
}
