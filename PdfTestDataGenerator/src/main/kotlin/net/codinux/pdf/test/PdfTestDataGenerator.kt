package net.codinux.pdf.test

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

        generateTestDataFile(outputDir, emptyPdf)
    }

    private fun generateTestDataFile(outputDir: Path, emptyPdf: PDDocument) {
        val file = """
            package net.codinux.pdf.test
            
            object PdfTestData {
            
                val Empty = ${getPdfBytes(emptyPdf)}
                
            }""".trimIndent()

        Files.createDirectories(outputDir)

        outputDir.resolve("PdfTestData.kt").writeText(file)
    }

    private fun getPdfBytes(pdf: PDDocument): String {
        val pdfBytes = ByteArrayOutputStream().use {
            pdf.save(it)

            it.toByteArray()
        }

        return "byteArrayOf(${pdfBytes.joinToString(", ")})"
    }

    private fun generateEmptyPdf() = PDDocument()
}