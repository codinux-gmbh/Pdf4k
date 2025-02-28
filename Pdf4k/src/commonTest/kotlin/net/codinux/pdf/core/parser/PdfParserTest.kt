package net.codinux.pdf.core.parser

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
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

}