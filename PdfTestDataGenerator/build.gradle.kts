plugins {
    kotlin("jvm")
}
kotlin {
    jvmToolchain(11)
}


val pdfBoxVersion = "3.0.4"

dependencies {
    implementation("org.apache.pdfbox:pdfbox:$pdfBoxVersion")
    implementation("org.apache.pdfbox:xmpbox:$pdfBoxVersion")
    implementation("org.apache.pdfbox:preflight:$pdfBoxVersion")

    testImplementation(kotlin("test"))
}


tasks.test {
    useJUnitPlatform()
}