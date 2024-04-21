package com._16minutes.liquiprime.tasks

import com._16minutes.liquiprime.Liquiprime
import com._16minutes.liquiprime.assets.PSEUDO_DATABASE_FILE_NAME_PREFIX
import com._16minutes.liquiprime.assets.ProjectAssetProvider
import com._16minutes.liquiprime.settings.LiquiprimeExtension.PrimerExecutionSettings
import com._16minutes.liquiprime.settings.LiquiprimeRuntimeParameter
import com._16minutes.liquiprime.sql.LiquiprimeConnection
import com._16minutes.liquiprime.sql.LiquiprimeDriver
import com._16minutes.liquiprime.tasks.validators.DatabasePrimerOutputValidator
import com._16minutes.liquiprime.tasks.validators.DatabasePrimerOutputValidator.DatabasePrimingOutcome
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.nio.file.Files

class PrimeDatabasesSpec: DescribeSpec({
    describe("execute") {
        lateinit var projectAssetProvider: ProjectAssetProvider
        lateinit var databaseUrl: String

        beforeEach {
            projectAssetProvider = ProjectAssetProvider.getInitializedProvider()
            projectAssetProvider.primerFile.writeText("""
                CREATE DATABASE test;
                CREATE TABLE test.test_table ( 
                    column_1 INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    column_2 VARCHAR(100),
                    column_3 VARCHAR(100)
                );
            """.trimIndent())

            databaseUrl = String.format(
                LiquiprimeDriver.URL_TEMPLATE,
                Files.createTempFile(PSEUDO_DATABASE_FILE_NAME_PREFIX, "txt").toAbsolutePath().toString()
            )
        }

        fun validatePrimeDatabaseTaskRun(
            primerExecutionSettingsList: List<PrimerExecutionSettings>,
            primerTaskSystemProperties: Map<String, String>,
            primerTaskEnvironmentVariables: Map<String, String>,
            expectedDatabasePrimingOutcome: DatabasePrimingOutcome
        ) {
            val taskName = Liquiprime.TaskName.PRIME_DATABASES
            val liquiprimeExtensionStr = primerExecutionSettingsList.joinToString("\\n") {"""
                register("${it.name}") {
                    $it
                }
            """.trimIndent()
            }
            projectAssetProvider.buildFile.appendText("""
                liquiprime {
                    primerExecutionSettingsByActivity {
                        $liquiprimeExtensionStr
                    }
                }
            """.trimIndent())
            val taskArguments = mutableListOf(taskName.value)
            taskArguments.addAll(primerTaskSystemProperties.asSequence().map{ "-D${it.key}=${it.value}" })
            val isPrimingSuccessExpected =
                expectedDatabasePrimingOutcome == DatabasePrimingOutcome.SUCCESS
                || expectedDatabasePrimingOutcome == DatabasePrimingOutcome.SUCCESS_WITH_SWALLOWED_EXCEPTION
            val expectedTaskOutcome = if (isPrimingSuccessExpected) {
                TaskOutcome.SUCCESS
            } else {
                TaskOutcome.FAILED
            }

            val testRunner =
                GradleRunner
                    .create()
                    .withProjectDir(projectAssetProvider.projectDirectory)
                    .withArguments(taskArguments)
                    .withEnvironment(primerTaskEnvironmentVariables)
                    .withPluginClasspath()
                    .forwardOutput()
            val testResult = if (isPrimingSuccessExpected) {
                testRunner.build()
            } else {
                testRunner.buildAndFail()
            }

            testResult
                .task(":${taskName.value}")!!
                .outcome
                .shouldBe(expectedTaskOutcome)
            primerExecutionSettingsList.forEach {
                val outputValidator = DatabasePrimerOutputValidator(
                    projectAssetProvider.projectDirectory,
                    it,
                    primerTaskSystemProperties,
                    primerTaskEnvironmentVariables
                )

                outputValidator.validateDatabaseOutput(expectedDatabasePrimingOutcome)
                outputValidator.validateBuildOutput(expectedDatabasePrimingOutcome, testResult.output)
            }
        }

        describe("exception handling") {
            it("""outputs a message of the form LiquiprimePrimeDatabasesException.SETUP_FAILURE_MESSAGE_TEMPLATE
                | for a given 'activity' if an exception is thrown while setting up resources to prime the database
                | specified in the execution settings for that 'activity' in the plugin extension""".trimMargin()) {
                val primerExecutionSettings = PrimerExecutionSettings(
                    "test",
                    PrimerExecutionSettings.ConnectionSettings(databaseUrl = null, driverClassName = null),
                    PrimerExecutionSettings.PrimerSettings(listOf(projectAssetProvider.primerFile.absolutePath))
                )
                val primerTaskSystemProperties = mapOf("jdbc.drivers" to "com._16minutes.liquiprime.sql.LiquiprimeDriver")

                validatePrimeDatabaseTaskRun(
                    primerExecutionSettingsList = listOf(primerExecutionSettings),
                    primerTaskSystemProperties = primerTaskSystemProperties,
                    primerTaskEnvironmentVariables = emptyMap(),
                    expectedDatabasePrimingOutcome = DatabasePrimingOutcome.SETUP_FAILURE
                )
            }

            it("""given the implicit default connection executionExceptionSuppressionSettings, outputs a message of the form 
                | LiquiprimePrimeDatabasesException.EXECUTION_FAILURE_MESSAGE_TEMPLATE for a given 'activity' and
                | primer file path if an exception is thrown while executing the statements in that file while priming
                | the database specified in the execution settings for that activity in the plugin extension""".trimMargin()) {
                val databaseUrlWithProperties =
                    "$databaseUrl?${LiquiprimeDriver.PropertyName.DO_THROW_EXCEPTIONS_FOR_ALL_OPERATIONS.value}=${true};"

                val primerExecutionSettings = PrimerExecutionSettings(
                    "test",
                    PrimerExecutionSettings.ConnectionSettings(databaseUrl = databaseUrlWithProperties, driverClassName = null),
                    PrimerExecutionSettings.PrimerSettings(listOf(projectAssetProvider.primerFile.absolutePath))
                )
                val primerTaskSystemProperties = mapOf("jdbc.drivers" to "com._16minutes.liquiprime.sql.LiquiprimeDriver")

                validatePrimeDatabaseTaskRun(
                    primerExecutionSettingsList = listOf(primerExecutionSettings),
                    primerTaskSystemProperties = primerTaskSystemProperties,
                    primerTaskEnvironmentVariables = emptyMap(),
                    expectedDatabasePrimingOutcome = DatabasePrimingOutcome.EXECUTION_FAILURE
                )
            }

            it("""given explicit null connection executionExceptionSuppressionSettings, outputs a message of the 
                | form LiquiprimePrimeDatabasesException.EXECUTION_FAILURE_MESSAGE_TEMPLATE
                | for a given 'activity' and primer file path if an exception is thrown while executing
                | the statements in that file while priming the database specified in the execution settings
                | for that activity in the plugin extension""".trimMargin()) {
                val databaseUrlWithProperties =
                    "$databaseUrl?${LiquiprimeDriver.PropertyName.DO_THROW_EXCEPTIONS_FOR_ALL_OPERATIONS.value}=${true};"

                val primerExecutionSettings = PrimerExecutionSettings(
                    "test",
                    PrimerExecutionSettings.ConnectionSettings(
                        databaseUrl = databaseUrlWithProperties,
                        driverClassName = null,
                        executionExceptionSuppressionSettings = null
                    ),
                    PrimerExecutionSettings.PrimerSettings(listOf(projectAssetProvider.primerFile.absolutePath))
                )
                val primerTaskSystemProperties = mapOf("jdbc.drivers" to "com._16minutes.liquiprime.sql.LiquiprimeDriver")

                validatePrimeDatabaseTaskRun(
                    primerExecutionSettingsList = listOf(primerExecutionSettings),
                    primerTaskSystemProperties = primerTaskSystemProperties,
                    primerTaskEnvironmentVariables = emptyMap(),
                    expectedDatabasePrimingOutcome = DatabasePrimingOutcome.EXECUTION_FAILURE
                )
            }

            it("""given explicit executionExceptionSuppressionSettings with a null exceptionMessageFilter list, 
                | swallows any exception created during the execution of a given 'activity' and primer file path
                | if the exception is thrown while executing  the statements in that file which prime the database
                | specified in the execution settings for that activity in the plugin extension""".trimMargin()) {
                val databaseUrlWithProperties =
                    "$databaseUrl?${LiquiprimeDriver.PropertyName.DO_THROW_EXCEPTIONS_FOR_ALL_OPERATIONS.value}=${true};"

                val primerExecutionSettings = PrimerExecutionSettings(
                    "test",
                    PrimerExecutionSettings.ConnectionSettings(
                        databaseUrl = databaseUrlWithProperties,
                        driverClassName = null,
                        executionExceptionSuppressionSettings = PrimerExecutionSettings.ConnectionSettings.ExecutionExceptionSuppressionSettings(null)
                    ),
                    PrimerExecutionSettings.PrimerSettings(listOf(projectAssetProvider.primerFile.absolutePath))
                )
                val primerTaskSystemProperties = mapOf("jdbc.drivers" to "com._16minutes.liquiprime.sql.LiquiprimeDriver")

                validatePrimeDatabaseTaskRun(
                    primerExecutionSettingsList = listOf(primerExecutionSettings),
                    primerTaskSystemProperties = primerTaskSystemProperties,
                    primerTaskEnvironmentVariables = emptyMap(),
                    expectedDatabasePrimingOutcome = DatabasePrimingOutcome.SUCCESS_WITH_SWALLOWED_EXCEPTION
                )
            }

            it("""given explicit executionExceptionSuppressionSettings with an empty exceptionMessageFilter list, 
                | outputs a message of the form LiquiprimePrimeDatabasesException.EXECUTION_FAILURE_MESSAGE_TEMPLATE
                | for a given 'activity' and primer file path if an exception is thrown while executing
                | the statements in that file while priming the database specified in the execution settings
                | for that activity in the plugin extension""".trimMargin()) {
                val databaseUrlWithProperties =
                    "$databaseUrl?${LiquiprimeDriver.PropertyName.DO_THROW_EXCEPTIONS_FOR_ALL_OPERATIONS.value}=${true};"

                val primerExecutionSettings = PrimerExecutionSettings(
                    "test",
                    PrimerExecutionSettings.ConnectionSettings(
                        databaseUrl = databaseUrlWithProperties,
                        driverClassName = null,
                        executionExceptionSuppressionSettings = PrimerExecutionSettings.ConnectionSettings.ExecutionExceptionSuppressionSettings(emptyList())
                    ),
                    PrimerExecutionSettings.PrimerSettings(listOf(projectAssetProvider.primerFile.absolutePath))
                )
                val primerTaskSystemProperties = mapOf("jdbc.drivers" to "com._16minutes.liquiprime.sql.LiquiprimeDriver")

                validatePrimeDatabaseTaskRun(
                    primerExecutionSettingsList = listOf(primerExecutionSettings),
                    primerTaskSystemProperties = primerTaskSystemProperties,
                    primerTaskEnvironmentVariables = emptyMap(),
                    expectedDatabasePrimingOutcome = DatabasePrimingOutcome.EXECUTION_FAILURE
                )
            }

            it("""given explicit executionExceptionSuppressionSettings with a non-empty exceptionMessageFilter list, 
                | swallows any exception containing any of the elements in the list created during the execution of a
                | given 'activity' and primer file path if the exception is thrown while executing the statements in that
                | file which prime the database specified in the execution settings for that activity in the plugin extension""".trimMargin()) {
                val databaseUrlWithProperties =
                    "$databaseUrl?${LiquiprimeDriver.PropertyName.DO_THROW_EXCEPTIONS_FOR_ALL_OPERATIONS.value}=${true};"

                val expectedExceptionMessage = LiquiprimeConnection.ExceptionMessages.DO_THROW_EXCEPTIONS_FOR_ALL_OPERATIONS_PROPERTY_TRUTHINESS
                val primerExecutionSettings = PrimerExecutionSettings(
                    "test",
                    PrimerExecutionSettings.ConnectionSettings(
                        databaseUrl = databaseUrlWithProperties,
                        driverClassName = null,
                        executionExceptionSuppressionSettings = PrimerExecutionSettings.ConnectionSettings.ExecutionExceptionSuppressionSettings(listOf(expectedExceptionMessage))
                    ),
                    PrimerExecutionSettings.PrimerSettings(listOf(projectAssetProvider.primerFile.absolutePath))
                )
                val primerTaskSystemProperties = mapOf("jdbc.drivers" to "com._16minutes.liquiprime.sql.LiquiprimeDriver")

                validatePrimeDatabaseTaskRun(
                    primerExecutionSettingsList = listOf(primerExecutionSettings),
                    primerTaskSystemProperties = primerTaskSystemProperties,
                    primerTaskEnvironmentVariables = emptyMap(),
                    expectedDatabasePrimingOutcome = DatabasePrimingOutcome.SUCCESS_WITH_SWALLOWED_EXCEPTION
                )
            }
        }

        it("""primes databases as specified by each of the execution settings in the plugin extension,
            | using the first drivers that are respectively associated with JDBC URLs in 
            | the aforementioned settings that it can find in the liquibaseRuntime configuration, 
            | and the (implicitly specified) default connection auto-commit setting""".trimMargin()) {
            val primerExecutionSettings = PrimerExecutionSettings(
                "test",
                PrimerExecutionSettings.ConnectionSettings(databaseUrl = databaseUrl, driverClassName = null),
                PrimerExecutionSettings.PrimerSettings(listOf(projectAssetProvider.primerFile.absolutePath))
            )
            val primerTaskSystemProperties = mapOf("jdbc.drivers" to "com._16minutes.liquiprime.sql.LiquiprimeDriver")

            validatePrimeDatabaseTaskRun(
                primerExecutionSettingsList = listOf(primerExecutionSettings),
                primerTaskSystemProperties = primerTaskSystemProperties,
                primerTaskEnvironmentVariables = emptyMap(),
                expectedDatabasePrimingOutcome = DatabasePrimingOutcome.SUCCESS
            )
        }

        it("""primes databases as specified by each of the execution settings in the plugin extension,
            | using the first drivers that are respectively associated with JDBC URLs in 
            | the aforementioned settings that it can find in the liquibaseRuntime configuration, 
            | and an explicit connection auto-commit setting of false""".trimMargin()) {
            val primerExecutionSettings = PrimerExecutionSettings(
                "test",
                PrimerExecutionSettings.ConnectionSettings(databaseUrl = databaseUrl, driverClassName = null, doEnableAutoCommit = false),
                PrimerExecutionSettings.PrimerSettings(listOf(projectAssetProvider.primerFile.absolutePath))
            )
            val primerTaskSystemProperties = mapOf("jdbc.drivers" to "com._16minutes.liquiprime.sql.LiquiprimeDriver")

            validatePrimeDatabaseTaskRun(
                primerExecutionSettingsList = listOf(primerExecutionSettings),
                primerTaskSystemProperties = primerTaskSystemProperties,
                primerTaskEnvironmentVariables = emptyMap(),
                expectedDatabasePrimingOutcome = DatabasePrimingOutcome.SUCCESS
            )
        }

        it("""primes databases as specified by each of the execution settings in the plugin extension,
            | using the first drivers that are respectively associated with JDBC URLs in 
            | the aforementioned settings that it can find in the liquibaseRuntime configuration, 
            | and an explicit connection auto-commit setting of true""".trimMargin()) {
            val primerExecutionSettings = PrimerExecutionSettings(
                "test",
                PrimerExecutionSettings.ConnectionSettings(databaseUrl = databaseUrl, driverClassName = null, doEnableAutoCommit = true),
                PrimerExecutionSettings.PrimerSettings(listOf(projectAssetProvider.primerFile.absolutePath))
            )
            val primerTaskSystemProperties = mapOf("jdbc.drivers" to "com._16minutes.liquiprime.sql.LiquiprimeDriver")

            validatePrimeDatabaseTaskRun(
                primerExecutionSettingsList = listOf(primerExecutionSettings),
                primerTaskSystemProperties = primerTaskSystemProperties,
                primerTaskEnvironmentVariables = emptyMap(),
                expectedDatabasePrimingOutcome = DatabasePrimingOutcome.SUCCESS
            )
        }

        it("""primes databases as specified by each of the execution settings in the 
            | plugin extension, using the explicitly specified drivers in those settings
            | that it can find in the liquibaseRuntime configuration, and the (implicitly 
            | specified) default connection auto-commit setting""".trimMargin()) {
            val primerExecutionSettings = PrimerExecutionSettings(
                "test",
                PrimerExecutionSettings.ConnectionSettings(databaseUrl, LiquiprimeDriver::class.java.name),
                PrimerExecutionSettings.PrimerSettings(listOf(projectAssetProvider.primerFile.absolutePath))
            )
            val primerTaskSystemProperties = mapOf("jdbc.drivers" to "com._16minutes.liquiprime.sql.LiquiprimeDriver")

            validatePrimeDatabaseTaskRun(
                primerExecutionSettingsList = listOf(primerExecutionSettings),
                primerTaskSystemProperties = primerTaskSystemProperties,
                primerTaskEnvironmentVariables = emptyMap(),
                expectedDatabasePrimingOutcome = DatabasePrimingOutcome.SUCCESS
            )
        }

        it("""primes databases as specified by each of the execution settings in the 
            | plugin extension, using the explicitly specified drivers in those
            | settings that it can find in the liquibaseRuntime configuration, 
            | and explicitly null executionExceptionSuppressionSettings""".trimMargin()) {
            val primerExecutionSettings = PrimerExecutionSettings(
                "test",
                PrimerExecutionSettings.ConnectionSettings(databaseUrl, LiquiprimeDriver::class.java.name, null),
                PrimerExecutionSettings.PrimerSettings(listOf(projectAssetProvider.primerFile.absolutePath))
            )
            val primerTaskSystemProperties = mapOf("jdbc.drivers" to "com._16minutes.liquiprime.sql.LiquiprimeDriver")

            validatePrimeDatabaseTaskRun(
                primerExecutionSettingsList = listOf(primerExecutionSettings),
                primerTaskSystemProperties = primerTaskSystemProperties,
                primerTaskEnvironmentVariables = emptyMap(),
                expectedDatabasePrimingOutcome = DatabasePrimingOutcome.SUCCESS
            )
        }

        it("""primes databases as specified by each of the execution settings in the 
            | plugin extension, using the explicitly specified drivers in those
            | settings that it can find in the liquibaseRuntime configuration, 
            | and explicit executionExceptionSuppressionSettings""".trimMargin()) {
            val primerExecutionSettings = PrimerExecutionSettings(
                "test",
                PrimerExecutionSettings.ConnectionSettings(
                    databaseUrl,
                    LiquiprimeDriver::class.java.name,
                    PrimerExecutionSettings.ConnectionSettings.ExecutionExceptionSuppressionSettings(null)
                ),
                PrimerExecutionSettings.PrimerSettings(listOf(projectAssetProvider.primerFile.absolutePath))
            )
            val primerTaskSystemProperties = mapOf("jdbc.drivers" to "com._16minutes.liquiprime.sql.LiquiprimeDriver")

            validatePrimeDatabaseTaskRun(
                primerExecutionSettingsList = listOf(primerExecutionSettings),
                primerTaskSystemProperties = primerTaskSystemProperties,
                primerTaskEnvironmentVariables = emptyMap(),
                expectedDatabasePrimingOutcome = DatabasePrimingOutcome.SUCCESS
            )
        }

        it("""primes databases as specified by the connection-related system properties,
            | environment variables, and execution settings associated with a given 'activity'
            | in the corresponding order of preference""".trimMargin()) {
            val activityName = "test"

            val primerExecutionSettings = PrimerExecutionSettings(
                activityName,
                PrimerExecutionSettings.ConnectionSettings(databaseUrl = null, driverClassName = null),
                PrimerExecutionSettings.PrimerSettings(listOf(projectAssetProvider.primerFile.absolutePath))
            )

            val databaseUrlSystemPropertyName =
                String.format(LiquiprimeRuntimeParameter.DATABASE_URL.systemPropertyTemplate, activityName)
            val primerTaskSystemProperties = mapOf(
                databaseUrlSystemPropertyName to databaseUrl,
                "jdbc.drivers" to "com._16minutes.liquiprime.sql.LiquiprimeDriver"
            )

            val driverClassNameEnvironmentVariableName =
                String.format(LiquiprimeRuntimeParameter.DRIVER.environmentVariableTemplate, activityName)
            val primerTaskEnvironmentVariables =
                mapOf(
                    driverClassNameEnvironmentVariableName to LiquiprimeDriver::class.java.name,
                    databaseUrlSystemPropertyName to "jdbc:mysql://localhost:9/test",

                )

            validatePrimeDatabaseTaskRun(
                primerExecutionSettingsList = listOf(primerExecutionSettings),
                primerTaskSystemProperties = primerTaskSystemProperties,
                primerTaskEnvironmentVariables = primerTaskEnvironmentVariables,
                expectedDatabasePrimingOutcome = DatabasePrimingOutcome.SUCCESS
            )
        }
    }
})
