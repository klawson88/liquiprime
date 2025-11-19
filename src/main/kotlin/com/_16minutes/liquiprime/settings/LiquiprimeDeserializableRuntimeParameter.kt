package com._16minutes.liquiprime.settings

import com._16minutes.liquiprime.env.EnvironmentVariableLoader
import com._16minutes.liquiprime.env.StandardEnvironmentVariableLoader
import com._16minutes.liquiprime.properties.StandardSystemPropertyLoader
import com._16minutes.liquiprime.properties.SystemPropertyLoader

class LiquiprimeDeserializableRuntimeParameter<T>(
    systemPropertyNameOrTemplate: String,
    environmentVariableNameOrTemplate: String,
    private val deserializer: (String) -> T,
    systemPropertyLoader: SystemPropertyLoader = StandardSystemPropertyLoader(),
    environmentVariableLoader: EnvironmentVariableLoader = StandardEnvironmentVariableLoader()
): LiquiprimeRuntimeParameter(
    systemPropertyNameOrTemplate,
    environmentVariableNameOrTemplate,
    systemPropertyLoader,
    environmentVariableLoader,
) {
    companion object {
        val TARGET_ACTIVITIES = LiquiprimeDeserializableRuntimeParameter(
            "liquiprime.targetActivities",
            "LIQUIPRIME_TARGET_ACTIVITIES",
            { value -> value.split(',').toHashSet() }
        )
    }

    fun getDeserializedValueFor(activity: String): T?  {
        return this.getValueFor(activity)?.let {
            this.deserializer(it)
        }
    }

    fun getDeserializedValue(): T?  {
        return this.getValue()?.let {
            this.deserializer(it)
        }
    }
}