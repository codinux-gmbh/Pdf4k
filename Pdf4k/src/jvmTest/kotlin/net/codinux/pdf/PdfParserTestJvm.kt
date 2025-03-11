package net.codinux.pdf

import assertk.assertThat
import assertk.assertions.*
import net.codinux.invoicing.testfiles.EInvoiceTestFiles
import net.codinux.pdf.core.parser.PdfParser
import org.junit.jupiter.api.Named
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Path
import kotlin.io.path.*

@OptIn(ExperimentalUnsignedTypes::class)
class PdfParserTestJvm {

    @ParameterizedTest
    @MethodSource("eInvoicePdfs")
    fun canReadEInvoicingFile(invoicePdf: Path) {
        val underTest = PdfParser(invoicePdf.readBytes().toUByteArray())

        val result = underTest.parseDocument()

        assertThat(result.pdfVersion).isIn(1.3f, 1.4f, 1.6f)

        assertThat(result.referencesToByteOffset).isNotEmpty()

        assertThat(result.catalog.language).isNull()

        assertThat(result.documentInfo).isNotNull()
        assertThat(result.isEncrypted).isFalse()

        assertThat(result.lowLevelDetails.creationHash).isNotNull().isNotEmpty()
        assertThat(result.lowLevelDetails.lastModifiedHash).isNotNull().isNotEmpty()

        assertThat(result.embeddedFiles).isNotEmpty()
//        val invoiceXml = result.embeddedFiles.firstOrNull { it.mimeType  }

        result.embeddedFiles.forEach { file ->
            assertThat(file.filename).isNotEmpty()
            assertThat(file.data).isNotEmpty()
//            assertThat(file.data).hasSize(file.size ?: -1)
        }

        val invoiceXml = result.embeddedFiles.firstOrNull { it.mimeType?.endsWith("xml") == true }
        assertThat(invoiceXml).isNotNull()
        assertThat(invoiceXml!!.data).isNotEmpty()
//        assertThat(invoiceXml.data).hasSize(invoiceXml.size ?: -1)

        val xml = invoiceXml.data.map { it.toByte() }.toByteArray().decodeToString()
        assertThat(xml).contains("<?xml ")
    }


    companion object {

        @JvmStatic
        fun eInvoicePdfs() = EInvoiceTestFiles.listAllPdfs()
            .map { Named.of(it.relativeTo(it.parent.parent.parent).pathString, it) }

    }
}