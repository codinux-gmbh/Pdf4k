package net.codinux.pdf.examples

import net.codinux.pdf.core.parser.PdfParser

class ReadPdfExamples {

    fun read() {
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
    }

}