plugins {
    kotlin("multiplatform")
}


kotlin {
    jvmToolchain(11)

    jvm()


    js {
        browser()

        nodejs()
    }

    wasmJs {
        browser()
    }


    linuxX64()
    mingwX64()

    iosX64()
    iosArm64()
    iosSimulatorArm64()
    macosX64()
    macosArm64()
    watchosArm64()
    watchosSimulatorArm64()
    tvosArm64()
    tvosSimulatorArm64()

    applyDefaultHierarchyTemplate()


    val klfVersion: String by project

    val assertKVersion: String by project
    val invoiceTestFilesVersion: String by project
    val logbackVersion: String by project

    sourceSets {
        commonMain.dependencies {
            implementation(project(":Pdf4k"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))

            implementation("com.willowtreeapps.assertk:assertk:$assertKVersion")
        }
    }
}