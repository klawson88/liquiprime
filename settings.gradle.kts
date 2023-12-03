plugins {
    // Apply the Foojay Toolchains Convention Plugin, which transitively applies,
    // then directly configures, the Foojay Toolchains Plugin. The latter plugin
    // defines a toolchain repository for projects based on Foojay Disco API
    id("org.gradle.toolchains.foojay-resolver-convention").version("0.7.0")
}

rootProject.name = "liquiprime"
