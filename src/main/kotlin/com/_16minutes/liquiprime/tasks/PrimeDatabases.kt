package com._16minutes.liquiprime.tasks

import com._16minutes.liquiprime.settings.LiquiprimeExtension
import com._16minutes.liquiprime.settings.LiquiprimeRuntimeParameter
import com._16minutes.liquiprime.tasks.exceptions.LiquiprimePrimeDatabasesException
import com._16minutes.liquiprime.util.createConnection
import com._16minutes.liquiprime.util.parseQueryLanguageFileStatements
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import java.io.File
import java.nio.file.Paths
import java.sql.Connection
import java.sql.SQLException
import java.util.*
import javax.inject.Inject


open class PrimeDatabases @Inject constructor (
    private val liquiprimeExtension: LiquiprimeExtension,
    private val workerExecutor: WorkerExecutor
): DefaultTask() {
    interface PrimeDatabaseWorkActionParameters: WorkParameters {
        val projectDirectoryPath: Property<String>
        val activityName: Property<String>
        val connectionSettings: Property<LiquiprimeExtension.PrimerExecutionSettings.ConnectionSettings>
        val primerSettings: Property<LiquiprimeExtension.PrimerExecutionSettings.PrimerSettings>
    }

    abstract class PrimeDatabaseWorkAction: WorkAction<PrimeDatabaseWorkActionParameters> {
        private fun executePrimerFileStatements(connection: Connection, primerFile: File) {
            try {
                primerFile.inputStream().use {
                    parseQueryLanguageFileStatements(it).forEach { statement ->
                        val statementObj = connection.createStatement()
                        statementObj.execute(statement)
                    }
                }

                if (!connection.autoCommit) {
                    connection.commit()
                }
            } catch (exception: SQLException) {
                if (!connection.autoCommit) {
                    connection.rollback()
                }
                throw exception
            }
        }
        override fun execute() {
            val activityName = parameters.activityName.get()
            var currentPrimerFilePath: String? = null
            var doesConnectionExist = false

            try {
                val connectionSettings = parameters.connectionSettings.get()

                val databaseUrlRuntimeParameter =  LiquiprimeRuntimeParameter.DATABASE_URL.getValueFor(activityName)
                val effectiveDatabaseUrl = databaseUrlRuntimeParameter ?: connectionSettings.databaseUrl

                val driverClassNameRuntimeParameter = LiquiprimeRuntimeParameter.DRIVER.getValueFor(activityName)
                val effectiveDriverClassName = driverClassNameRuntimeParameter ?: connectionSettings.driverClassName

                val driverProperties = Properties()
                LiquiprimeRuntimeParameter.DRIVER_PROPERTIES_FILE.getValueFor(activityName)?.let {
                    val driverPropertiesInputStream = File(it).inputStream()
                    driverProperties.load(driverPropertiesInputStream)
                }

                createConnection(effectiveDatabaseUrl, effectiveDriverClassName, driverProperties).use { connection ->
                    doesConnectionExist = true
                    connection.autoCommit = connectionSettings.doEnableAutoCommit

                    val primerSettings = parameters.primerSettings.get()
                    primerSettings.primerFilePaths.forEach { primerFilePath ->
                        val canonicalPrimerFilePath =
                            Paths.get(parameters.projectDirectoryPath.get()).resolve(primerFilePath).normalize()
                        currentPrimerFilePath = canonicalPrimerFilePath.toString()

                        executePrimerFileStatements(connection, canonicalPrimerFilePath.toFile())
                    }
                }
            } catch(exception: Exception) {
                val executionExceptionSuppressionSettings = parameters.connectionSettings.get().executionExceptionSuppressionSettings

                if (!doesConnectionExist
                    || executionExceptionSuppressionSettings == null
                    || (executionExceptionSuppressionSettings.exceptionMessageFilters != null
                        && executionExceptionSuppressionSettings.exceptionMessageFilters?.none{exception.message?.contains(it) == true} == true)) {
                    val exceptionMessage =
                        LiquiprimePrimeDatabasesException.createMessage(activityName, currentPrimerFilePath)

                    throw LiquiprimePrimeDatabasesException(exceptionMessage, exception)
                }
            }
        }
    }

    object DependencyConstants {
        const val LIQUIBASE_GRADLE_RUNTIME_CONFIGURATION_NAME = "liquibaseRuntime"
    }

    private fun primeDatabases() {
        val workQueue = workerExecutor.classLoaderIsolation {
            val liquibaseGradleRuntimeConfiguration =
                project.configurations.findByName(DependencyConstants.LIQUIBASE_GRADLE_RUNTIME_CONFIGURATION_NAME)

            if (liquibaseGradleRuntimeConfiguration != null) {
                it.classpath.from(liquibaseGradleRuntimeConfiguration)
            }
        }

        liquiprimeExtension.primerExecutionSettingsByActivity.forEach { primerSettings ->
            workQueue.submit(PrimeDatabaseWorkAction::class.java) {
                it.projectDirectoryPath.set(project.projectDir.absolutePath)
                it.activityName.set(primerSettings.name)
                it.connectionSettings.set(primerSettings.connectionSettings)
                it.primerSettings.set(primerSettings.primerSettings)
            }
        }
    }

    @TaskAction
    fun execute() {
        primeDatabases()
    }
}
