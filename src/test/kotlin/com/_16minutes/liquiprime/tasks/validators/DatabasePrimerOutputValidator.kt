package com._16minutes.liquiprime.tasks.validators

import com._16minutes.liquiprime.settings.LiquiprimeExtension
import com._16minutes.liquiprime.settings.LiquiprimeRuntimeParameter
import com._16minutes.liquiprime.sql.LiquiprimeDriver
import com._16minutes.liquiprime.tasks.exceptions.LiquiprimePrimeDatabasesException
import io.kotest.assertions.fail
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.io.File
import java.nio.file.Paths
import java.util.regex.Pattern

class DatabasePrimerOutputValidator(
    private val projectDirectory: File,
    private val primerExecutionSettings: LiquiprimeExtension.PrimerExecutionSettings,
    private val primerSystemProperties: Map<String, String>,
    private val primerEnvironmentVariables: Map<String, String>
) {
    enum class DatabasePrimingOutcome(val validationOutputLineTemplate: String?) {
        SUCCESS(null),
        SUCCESS_WITH_SWALLOWED_EXCEPTION(null),
        SETUP_FAILURE(LiquiprimePrimeDatabasesException.SETUP_FAILURE_MESSAGE_TEMPLATE),
        EXECUTION_FAILURE(LiquiprimePrimeDatabasesException.EXECUTION_FAILURE_MESSAGE_TEMPLATE)
    }

    private fun getEffectiveDatabaseUrl(
        primerExecutionSettings: LiquiprimeExtension.PrimerExecutionSettings,
        primerSystemProperties: Map<String, String>,
        primerEnvironmentVariables: Map<String, String>
    ): String {
        val activityName = primerExecutionSettings.name

        val databaseUrlSystemPropertyName =
            String.format(LiquiprimeRuntimeParameter.DATABASE_URL.systemPropertyTemplate, activityName)
        val databaseUrlSystemProperty = primerSystemProperties[databaseUrlSystemPropertyName]

        val databaseUrlEnvironmentVariableName =
            String.format(LiquiprimeRuntimeParameter.DATABASE_URL.environmentVariableTemplate, activityName)
        val databaseUrlEnvironmentVariable = primerEnvironmentVariables[databaseUrlEnvironmentVariableName]

        return listOf(
            databaseUrlSystemProperty,
            databaseUrlEnvironmentVariable,
            primerExecutionSettings.connectionSettings.databaseUrl
        ).firstNotNullOf{
            it
        }
    }


    private fun validateSuccessOutcome() {
        val activityName = primerExecutionSettings.name

        val effectiveDatabaseUrl =
            getEffectiveDatabaseUrl(primerExecutionSettings, primerSystemProperties, primerEnvironmentVariables)

        if (effectiveDatabaseUrl != null) {
            val databaseUrlMatcher = LiquiprimeDriver.URL_PATTERN.matcher(effectiveDatabaseUrl)

            if (databaseUrlMatcher.matches()) {
                val urlLocationComponent =
                    databaseUrlMatcher.group(LiquiprimeDriver.URL_REGEX_LOCATION_COMPONENT_CAPTURING_GROUP_NAME)

                val actualDatabaseContents = File(urlLocationComponent).readText()

                val expectedDatabaseContents =
                    primerExecutionSettings
                        .primerSettings
                        .primerFilePaths
                        .asSequence()
                        .map{ Paths.get(projectDirectory.absolutePath).resolve(it).toFile() }
                        .map{ it.readText() }
                        .joinToString("\\n")

                actualDatabaseContents.shouldBe(expectedDatabaseContents)
            } else {
                fail("""Priming of the database which the activity $activityName pertains to could not be
                        | validated because validation for the database is not currently supported""".trimMargin())
            }
        } else {
            fail("""Priming of the database which the activity $activityName pertains to could not
                    | be validated because the effective URL of the database is null""".trimMargin())
        }
    }

    fun validateDatabaseOutput(databasePrimingOutcome: DatabasePrimingOutcome) {
        when (databasePrimingOutcome) {
            DatabasePrimingOutcome.SUCCESS -> {
                validateSuccessOutcome()
            }
            else -> {}
        }
    }

    fun validateBuildOutput(databasePrimingOutcome: DatabasePrimingOutcome, buildOutput: String) {
        when (databasePrimingOutcome) {
            DatabasePrimingOutcome.SETUP_FAILURE -> {
                val expectedMessage = String.format(
                    databasePrimingOutcome.validationOutputLineTemplate!!,
                    primerExecutionSettings.name
                )

                buildOutput.shouldContain(expectedMessage)
            }
            DatabasePrimingOutcome.EXECUTION_FAILURE -> {
                val absoluteProjectDirectoryPath = projectDirectory.absolutePath
                val canonicalPrimerFilePathRegex =
                    primerExecutionSettings.primerSettings.primerFilePaths
                        .asSequence()
                        .map {
                            val canonicalPrimerFilePath =
                                Paths.get(absoluteProjectDirectoryPath).resolve(it).normalize()

                            Pattern.quote(canonicalPrimerFilePath.toString())
                        }.joinToString("|")

                val expectedMessageRegex = String.format(
                    databasePrimingOutcome.validationOutputLineTemplate!!,
                    canonicalPrimerFilePathRegex,
                    primerExecutionSettings.name
                )

                buildOutput.shouldContain(Regex(expectedMessageRegex))
            }
            else -> {}
        }
    }
}
