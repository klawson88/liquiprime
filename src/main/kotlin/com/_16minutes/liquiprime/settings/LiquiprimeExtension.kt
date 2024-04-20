package com._16minutes.liquiprime.settings

import org.gradle.api.NamedDomainObjectContainer
import java.io.Serializable

open class LiquiprimeExtension(val primerExecutionSettingsByActivity: NamedDomainObjectContainer<PrimerExecutionSettings>) {
    open class PrimerExecutionSettings(
        val name: String,
        val connectionSettings: ConnectionSettings,
        val primerSettings: PrimerSettings
    ) {
        class ConnectionSettings(
            var databaseUrl: String?,
            var driverClassName: String?,
            var doEnableAutoCommit: Boolean = Defaults.DO_ENABLE_AUTO_COMMIT
        ): Serializable {
            companion object {
                private const val serialVersionUID = 2L
            }

            object Defaults {
                const val DO_ENABLE_AUTO_COMMIT = false
            }
            constructor(): this(null, null)

            override fun toString(): String {
                val strBuilder = StringBuilder()

                if (databaseUrl != null) {
                    strBuilder.appendLine("""databaseUrl = "$databaseUrl"""")
                }

                if (driverClassName != null) {
                    strBuilder.appendLine("""driverClassName = "$driverClassName"""")
                }

                strBuilder.appendLine("""doEnableAutoCommit = $doEnableAutoCommit""")

                return strBuilder.toString()
            }
        }

        class PrimerSettings(var primerFilePaths: List<String>): Serializable {
            companion object {
                private const val serialVersionUID = 1L
            }
            object Defaults {
                val PRIMER_FILE_PATHS = mutableListOf<String>()
            }

            constructor(): this(Defaults.PRIMER_FILE_PATHS)

            override fun toString(): String {
                val strBuilder = StringBuilder()

                if (primerFilePaths.isNotEmpty()) {
                    val primerFilePathsStr = primerFilePaths.joinToString(", ") { """"$it"""" }
                    strBuilder.appendLine("primerFilePaths = listOf($primerFilePathsStr)")
                }

                return strBuilder.toString()
            }
        }

        constructor(name: String): this(name, ConnectionSettings(), PrimerSettings())

        fun connectionSettings(configure: ConnectionSettings.() -> Unit) {
            connectionSettings.configure()
        }

        fun primerSettings(configure: PrimerSettings.() -> Unit) {
            primerSettings.configure()
        }

        override fun toString(): String {
            val settingsStrBuilder = StringBuilder()

            val connectionSettingsStr = connectionSettings.toString()

            if (connectionSettingsStr.isNotEmpty()) {
                settingsStrBuilder.appendLine("""
                    connectionSettings {
                        $connectionSettingsStr
                    }
                """.trimIndent())
            }

            val primerSettingsStr = primerSettings.toString()

            if (primerSettingsStr.isNotEmpty()) {
                settingsStrBuilder.appendLine("""
                    primerSettings {
                        $primerSettingsStr
                    }
                """.trimIndent())
            }

            return settingsStrBuilder.toString()
        }
    }

    fun primerExecutionSettingsByActivity(configure: NamedDomainObjectContainer<PrimerExecutionSettings>.() -> Unit) {
        primerExecutionSettingsByActivity.configure()
    }
}
