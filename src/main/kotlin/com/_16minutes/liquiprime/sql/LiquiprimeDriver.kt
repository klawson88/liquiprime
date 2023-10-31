package com._16minutes.liquiprime.sql

import com._16minutes.liquiprime.sql.LiquiprimeConnection.LiquiprimeConnectionProperties
import java.io.File
import java.sql.*
import java.util.*
import java.util.logging.Logger
import java.util.regex.Pattern


class LiquiprimeDriver: Driver {
    enum class PropertyName(val value: String) {
        DO_THROW_EXCEPTIONS_FOR_ALL_OPERATIONS("doThrowExceptionsForAllOperations")
    }
    companion object {
        const val URL_REGEX_LOCATION_COMPONENT_CAPTURING_GROUP_NAME = "outputFilePath"
        const val URL_REGEX = "^jdbc:liquiprime:(?<$URL_REGEX_LOCATION_COMPONENT_CAPTURING_GROUP_NAME>.*)(\\?(\\w+=\\w+;)+)?$"
        const val URL_TEMPLATE = "jdbc:liquiprime:%s"

        val URL_PATTERN: Pattern = Pattern.compile(URL_REGEX)

        init {
            try {
                DriverManager.registerDriver(LiquiprimeDriver())
            } catch (e: SQLException) {
                throw RuntimeException("Unable to register LiquiprimeDriver", e)
            }
        }
    }

    object ExceptionMessages {
        const val NULL_URL = "URL is null"
    }

    override fun connect(p0: String?, p1: Properties?): Connection? {
        return if (p0 != null) {
            val urlPatternMatcher = URL_PATTERN.matcher(p0)

            if (urlPatternMatcher.matches()) {
                val effectiveProperties = Properties()

                val databaseUrlComponents = p0.split("?")
                if (databaseUrlComponents.size > 1) {
                    val driverUrlProperties = databaseUrlComponents[1]

                    driverUrlProperties
                        .split(";")
                        .forEach {
                            if (it.isNotBlank()) {
                                val keyAndValueArr = it.split("=")
                                effectiveProperties.setProperty(keyAndValueArr[0], keyAndValueArr[1])
                            }
                        }
                }

                p1?.forEach { effectiveProperties.setProperty(it.key.toString(), it.value.toString()) }

                val doThrowExceptionsForAllOperations =
                    effectiveProperties[PropertyName.DO_THROW_EXCEPTIONS_FOR_ALL_OPERATIONS.value]
                        .toString()
                        .toBooleanStrictOrNull()

                val connectionProperties = LiquiprimeConnectionProperties(doThrowExceptionsForAllOperations ?: false)
                
                val outputDestination = urlPatternMatcher.group(URL_REGEX_LOCATION_COMPONENT_CAPTURING_GROUP_NAME)
                
                LiquiprimeConnection(File(outputDestination), connectionProperties)
            } else {
                null
            }
        } else {
            throw SQLException(ExceptionMessages.NULL_URL)
        }
    }

    override fun acceptsURL(p0: String?): Boolean {
        return if (p0 != null) {
            Pattern.matches(URL_PATTERN.toString(), p0)
        } else {
            throw SQLException(ExceptionMessages.NULL_URL)
        }
    }

    override fun getPropertyInfo(p0: String?, p1: Properties?): Array<DriverPropertyInfo> {
        TODO("Not yet implemented")
    }

    override fun getMajorVersion(): Int {
        TODO("Not yet implemented")
    }

    override fun getMinorVersion(): Int {
        TODO("Not yet implemented")
    }

    override fun jdbcCompliant(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getParentLogger(): Logger {
        TODO("Not yet implemented")
    }
}
