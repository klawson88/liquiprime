plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    kotlin("jvm").version("1.9.10")

    // Apply the Java Gradle plugin to add support for Gradle plugin development
    `java-gradle-plugin`
}

group = "io.github.klawson88"
version = "1.0"

gradlePlugin {
    plugins {
        create("liquiprime") {
            val pluginNamePackageFormat = name.replace("-", "_")

            id = "io.github.klawson88.liquiprime"
            implementationClass = "com._16minutes.liquiprime.Liquiprime"
            displayName = "Liquiprime"
            description = """A Gradle plugin which provides the ability to perform operations on databases
                | before Liquibase changelogs are executed on them through liquibase-gradle-plugin""".trimMargin()
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("io.kotest:kotest-runner-junit5:5.7.2") {
        because("it (Kotest) is the framework with which the plugin will be tested")
    }

    testImplementation("io.kotest:kotest-assertions-core:5.7.2") {
        because("""it (Kotest assertions library) is the library that defines
            | the assertions to be used in tests of the plugin""".trimMargin())
    }

    testImplementation("io.mockk:mockk:1.13.7") {
        because("it (Mockk) is the mocking library to be used in the tests of the plugin")
    }
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform ()
}
