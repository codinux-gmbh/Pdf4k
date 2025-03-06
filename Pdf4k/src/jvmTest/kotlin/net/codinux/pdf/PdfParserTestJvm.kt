package net.codinux.pdf

import assertk.assertThat
import assertk.assertions.*
import net.codinux.invoicing.testfiles.EInvoiceTestFiles
import net.codinux.pdf.core.objects.*
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

        assertThat(result.header).isNotNull()
        assertThat(result.header!!.major).isEqualTo(1)
        assertThat(result.header!!.minor).isIn(3, 4, 6)

        assertThat(result.crossReferenceSection).isNotNull()
        val sections = result.crossReferenceSection!!.getSections()
        assertThat(sections).isNotEmpty()
        sections.forEach { section ->
            assertThat(section).isNotEmpty()
        }

        assertThat(result.trailerInfo).isNotNull()
        assertThat(result.trailerInfo!!.root is PdfRef).isTrue()
        assertThat((result.trailerInfo!!.root as PdfRef).objectNumber).isGreaterThanOrEqualTo(1)

        val infoDict = result.trailerInfo!!.info
        assertThat(infoDict).isNotNull()
        assertThat(infoDict is PdfDict || infoDict is PdfRef).isTrue()

        assertThat(result.trailerInfo!!.encrypt).isNull()

        assertThat(result.trailerInfo!!.id is PdfArray).isTrue()
        val idItems = (result.trailerInfo!!.id as PdfArray).items
        assertThat(idItems).hasSize(2)
        val creationHash = idItems[0]
        val lastModifiedHash = idItems[1]
        assertThat(creationHash is PdfString || creationHash is PdfHexString).isTrue()
        assertThat(lastModifiedHash is PdfString || lastModifiedHash is PdfHexString).isTrue()
    }


    companion object {

        @JvmStatic
        fun eInvoicePdfs() = EInvoiceTestFiles.listAllPdfs()
            .map { Named.of(it.relativeTo(it.parent.parent.parent).pathString, it) }

    }
}