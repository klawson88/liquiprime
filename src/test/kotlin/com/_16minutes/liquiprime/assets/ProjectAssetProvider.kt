package com._16minutes.liquiprime.assets

import com._16minutes.liquiprime.util.SYSTEM_TEMP_DIRECTORY_PATHNAME
import java.io.File

import java.nio.file.Paths
import java.util.*

class ProjectAssetProvider(assetGroupId: String) {
    companion object {
        private const val PROJECT_DIRECTORY_PREFIX = "liquiprimeIntegrationTestExecutionProject-"
        const val PROJECT_NAME = "integration-test"
        private const val SETTINGS_FILE_NAME = "settings.gradle.kts"
        private const val BUILD_FILE_NAME = "build.gradle.kts"
        private const val PRIMER_FILE_NAME = "database-primer.sql"
        private val INITIAL_SETTINGS_FILE_TEXT = """
            rootProject.name = "$PROJECT_NAME"
        """.trimIndent()
        private val INITIAL_BUILD_FILE_TEXT = """
            import com._16minutes.liquiprime.settings.LiquiprimeExtension
            
            plugins {
                id("io.github.klawson88.liquiprime")
            }
        """.trimIndent()

        /**
         *
         */
        fun getInitializedProvider():ProjectAssetProvider {
            val provider = ProjectAssetProvider()
            provider.projectDirectory.mkdir()
            provider.settingsFile.writeText(INITIAL_SETTINGS_FILE_TEXT)
            provider.buildFile.writeText(INITIAL_BUILD_FILE_TEXT)

            return provider
        }
    }

    val projectDirectory =
        Paths
            .get(SYSTEM_TEMP_DIRECTORY_PATHNAME, "${PROJECT_DIRECTORY_PREFIX}$assetGroupId")
            .toFile()
    val settingsFile: File = Paths.get(projectDirectory.canonicalPath, SETTINGS_FILE_NAME).toFile()
    val buildFile: File = Paths.get(projectDirectory.canonicalPath, BUILD_FILE_NAME).toFile()
    val primerFile: File = Paths.get(projectDirectory.canonicalPath, PRIMER_FILE_NAME).toFile()

    constructor(): this(UUID.randomUUID().toString())
}
