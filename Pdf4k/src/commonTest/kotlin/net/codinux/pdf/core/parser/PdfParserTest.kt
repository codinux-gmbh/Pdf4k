package net.codinux.pdf.core.parser

import assertk.assertThat
import assertk.assertions.*
import net.codinux.pdf.core.objects.PdfArray
import net.codinux.pdf.core.objects.PdfHexString
import net.codinux.pdf.core.objects.PdfRef
import net.codinux.pdf.test.PdfTestData
import kotlin.test.Test

class PdfParserTest {

    @Test
    fun parseHeader_Pdf_1_7() {
        val underTest = PdfParser(PdfTestData.Empty)

        val result = underTest.parseDocument()

        assertThat(result.header).isNotNull()
        assertThat(result.header!!.major).isEqualTo(1)
        assertThat(result.header!!.minor).isEqualTo(7)
    }

    @Test
    fun parseHeader_Pdf_1_4() {
        val underTest = PdfParser(PdfTestData.Empty_v1_4_Uncompressed)

        val result = underTest.parseDocument()

        assertThat(result.header).isNotNull()
        assertThat(result.header!!.major).isEqualTo(1)
        assertThat(result.header!!.minor).isEqualTo(4)
    }


    @Test
    fun parseTrailerDictionary_Pdf_1_4() {
        val underTest = PdfParser(PdfTestData.Empty_v1_4_Uncompressed)

        val result = underTest.parseDocument()

        assertThat(result.trailerInfo).isNotNull()
        // this PDF's trailer dictionary contains only /Root and /ID
        assertThat(result.trailerInfo!!.root is PdfRef).isTrue()
        assertThat((result.trailerInfo!!.root as PdfRef).objectNumber).isEqualTo(1)

        assertThat(result.trailerInfo!!.info).isNull()
        assertThat(result.trailerInfo!!.encrypt).isNull()

        assertThat(result.trailerInfo!!.id is PdfArray).isTrue()
        val idItems = (result.trailerInfo!!.id as PdfArray).items
        assertThat(idItems).hasSize(2)
        val creationHash = idItems[0]
        val lastModifiedHash = idItems[1]
        assertThat(creationHash is PdfHexString).isTrue()
        assertThat((creationHash as PdfHexString).value).isEqualTo("3E8D5A4F84D0B77955DA8E168A9055E7")
        assertThat(lastModifiedHash is PdfHexString).isTrue()
        assertThat((lastModifiedHash as PdfHexString).value).isEqualTo("3E8D5A4F84D0B77955DA8E168A9055E7")
    }

}