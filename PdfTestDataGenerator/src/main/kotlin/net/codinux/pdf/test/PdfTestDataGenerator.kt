package net.codinux.pdf.test

import org.apache.pdfbox.pdfwriter.compress.CompressParameters
import org.apache.pdfbox.pdmodel.PDDocument
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.writeText


fun main() {
    val outputDir = Path("Pdf4k/src/commonTest/kotlin/net/codinux/pdf/test/").absolute()

    PdfTestDataGenerator().generateTestData(outputDir)
}


class PdfTestDataGenerator {

    fun generateTestData(outputDir: Path) {
        val emptyPdf = generateEmptyPdf()
        val emptyPdf_v1_4 = generateEmptyPdf(1.4f) // PdfBox will not write "1.3" to output so i use 1.4

        generateTestDataFile(outputDir, emptyPdf, emptyPdf_v1_4)
    }

    private fun generateTestDataFile(outputDir: Path, emptyPdf: PDDocument, emptyPdf_v1_4: PDDocument) {
        val file = """
            package net.codinux.pdf.test
            
            object PdfTestData {
            
                val Empty = ${getPdfBytes(emptyPdf)}
                
                val Empty_v1_4_Uncompressed = ${getPdfBytes(emptyPdf_v1_4)}
                
            }""".trimIndent()

        Files.createDirectories(outputDir)

        outputDir.resolve("PdfTestData.kt").writeText(file)
    }

    private fun getPdfBytes(pdf: PDDocument): String {
        val pdfBytes = ByteArrayOutputStream().use {
            // disable compression otherwise PdfBox writes at least version 1.6 to output
            pdf.save(it, if (pdf.version < 1.6f) CompressParameters.NO_COMPRESSION else CompressParameters.DEFAULT_COMPRESSION)

            it.toByteArray()
        }

        return "byteArrayOf(${pdfBytes.joinToString(", ")})"
    }

    private fun generateEmptyPdf(version: Float = 1.7f): PDDocument {
        val fullyInitializedDocument = PDDocument()

        val fullyInitializedCosDocument = fullyInitializedDocument.document
        fullyInitializedCosDocument.version = version

        return PDDocument(fullyInitializedCosDocument)
    }
}