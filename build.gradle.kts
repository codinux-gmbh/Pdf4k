
allprojects {
    group = "net.codinux.pdf"
    version = "0.5.1-SNAPSHOT"


    ext["sourceCodeRepositoryBaseUrl"] = "github.com/codinux-gmbh/Pdf4k"

    ext["projectDescription"] = "A very basic implementation for parsing PDF files for all Kotlin Multiplatform targets"


    repositories {
        mavenCentral()
        mavenLocal() // TODO: remove as soon as eInvoice-Testfiles version 1.0.0 is out
    }
}