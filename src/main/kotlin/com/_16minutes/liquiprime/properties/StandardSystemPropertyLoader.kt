package com._16minutes.liquiprime.properties

class StandardSystemPropertyLoader: SystemPropertyLoader {
    override fun load(key: String): String? {
        return System.getProperty(key)
    }
}
