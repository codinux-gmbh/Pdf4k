pluginManagement {
    val kotlinVersion: String by settings

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        kotlin("jvm") version kotlinVersion apply false
        kotlin("multiplatform") version kotlinVersion apply false

        kotlin("plugin.allopen") version kotlinVersion apply false
        kotlin("plugin.noarg") version kotlinVersion apply false

        kotlin("plugin.serialization") version kotlinVersion apply false
    }
}


plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}


rootProject.name = "Pdf4kProject"


include("Pdf4k")
