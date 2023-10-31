package com._16minutes.liquiprime.settings

import com._16minutes.liquiprime.env.EnvironmentVariableLoader
import com._16minutes.liquiprime.env.StandardEnvironmentVariableLoader
import com._16minutes.liquiprime.properties.StandardSystemPropertyLoader
import com._16minutes.liquiprime.properties.SystemPropertyLoader

class LiquiprimeRuntimeParameter(
    val systemPropertyTemplate: String,
    val environmentVariableTemplate: String,
    private val systemPropertyLoader: SystemPropertyLoader = StandardSystemPropertyLoader(),
    private val environmentVariableLoader: EnvironmentVariableLoader = StandardEnvironmentVariableLoader()
) {
    companion object {
        val DRIVER_PROPERTIES_FILE = LiquiprimeRuntimeParameter(
            "liquiprime.%s.driverPropertiesFile",
            "LIQUIPRIME_%s_DRIVER_PROPERTIES_FILE"
        )

        val DRIVER = LiquiprimeRuntimeParameter(
            "liquiprime.%s.driver",
            "LIQUIPRIME_%s_driver"
        )

        val DATABASE_URL = LiquiprimeRuntimeParameter(
            "liquiprime.%s.command.url",
            "LIQUIPRIME_%s_COMMAND_URL"
        )
    }

    /**
     * Get the value of the runtime parameter represented by the invoking object,
     * for a given activity, in accordance with the Liquibase configuration
     * location order of preference (https://tinyurl.com/yc2x3kn8).
     */
    fun getValueFor(activityName: String): String? {
        val activitySystemPropertyName = String.format(systemPropertyTemplate, activityName)
        val activitySystemProperty = systemPropertyLoader.load(activitySystemPropertyName)

        if (activitySystemProperty == null) {
            val activityEnvironmentVariableName = String.format(environmentVariableTemplate, activityName)
            return environmentVariableLoader.get(activityEnvironmentVariableName)
        } else {
            return activitySystemProperty
        }
    }
}
