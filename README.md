# Pdf4k

PDF metadata reader written in pure Kotlin available for all Kotlin multiplatform targets.

Not supposed to be used in production, has only very limited functionality:

- Reads PDF structure
- Get PDF metadata like title, author, ...
- Get embedded files (e.g. to extract e-invoices)


Started as a clone of the very well structured and written [pdf-lib](https://github.com/Hopding/pdf-lib). So give them a star if you like this library.


## Setup

### Gradle

```
implementation("net.codinux.pdf:pdf4k:0.5.0")
```


## Usage

For examples see [examples](./examples) folder.

### Read PDF

For code see [ReadPdfExamples](./examples/src/commonMain/kotlin/net/codinux/pdf/examples/ReadPdfExamples.kt).

```kotlin
val pdfBytes = PdfTestData.EmptyPdf_v1_7 // get bytes of PDF file

val parser = PdfParser(pdfBytes)

// parses only basic PDF structure (and is therefor very fast), all other objects when needed the first time
val document = parser.parseDocument()
// or if you like to read all PDF objects upfront use parseDocumentEagerly()
// val document = parser.parseDocumentEagerly()

println("PDF version is ${document.pdfVersion}")
println("Is encrypted? ${document.isEncrypted}")

val embeddedFiles = document.embeddedFiles
println("PDF contains ${embeddedFiles.size} embedded files")
embeddedFiles.forEachIndexed { index, file ->
    println("[${index + 1}] ${file.filename}, ${file.mimeType} ${file.description}, " +
            "${file.size ?: file.fileContent.size} bytes") // fileContent gets decompressed lazily on first call
}
```