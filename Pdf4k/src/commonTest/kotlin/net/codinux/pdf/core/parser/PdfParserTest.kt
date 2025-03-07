package net.codinux.pdf.core.parser

import assertk.assertThat
import assertk.assertions.*
import net.codinux.pdf.core.document.PdfStructure
import net.codinux.pdf.core.objects.PdfArray
import net.codinux.pdf.core.objects.PdfHexString
import net.codinux.pdf.core.objects.PdfRef
import net.codinux.pdf.test.PdfTestData
import kotlin.test.Test

@OptIn(ExperimentalUnsignedTypes::class)
class PdfParserTest {

    @Test
    fun parseHeader_Pdf_1_7() {
        val underTest = PdfParser(PdfTestData.Empty)

        val result = underTest.parseDocument()

        assertHeader_1_7(result)
    }

    @Test
    fun parseHeaderEfficiently_Pdf_1_7() {
        val underTest = PdfParser(PdfTestData.Empty)

        val result = underTest.parseDocumentEfficiently()

        assertHeader_1_7(result)
    }

    private fun assertHeader_1_7(result: PdfStructure) {
        assertThat(result.header).isNotNull()
        assertThat(result.header!!.major).isEqualTo(1)
        assertThat(result.header!!.minor).isEqualTo(7)
    }

    @Test
    fun parseHeader_Pdf_1_4() {
        val underTest = PdfParser(PdfTestData.Empty_v1_4_Uncompressed)

        val result = underTest.parseDocument()

        assertHeader_1_4(result)
    }

    @Test
    fun parseHeaderEfficiently_Pdf_1_4() {
        val underTest = PdfParser(PdfTestData.Empty_v1_4_Uncompressed)

        val result = underTest.parseDocumentEfficiently()

        assertHeader_1_4(result)
    }

    private fun assertHeader_1_4(result: PdfStructure) {
        assertThat(result.header).isNotNull()
        assertThat(result.header!!.major).isEqualTo(1)
        assertThat(result.header!!.minor).isEqualTo(4)
    }


    @Test
    fun parseTrailerDictionary_Pdf_1_7() {
        val underTest = PdfParser(PdfTestData.Empty)

        val result = underTest.parseDocument()

        assertTrailerDictionary_1_7(result)
    }

    @Test
    fun parseTrailerDictionaryEfficiently_Pdf_1_7() {
        val underTest = PdfParser(PdfTestData.Empty)

        val result = underTest.parseDocumentEfficiently()

        assertTrailerDictionary_1_7(result)
    }

    private fun assertTrailerDictionary_1_7(result: PdfStructure) {
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
        assertThat((creationHash as PdfHexString).value).isEqualTo("66064282FD5B59CB6DAFD284A9EB3BAC")
        assertThat(lastModifiedHash is PdfHexString).isTrue()
        assertThat((lastModifiedHash as PdfHexString).value).isEqualTo("66064282FD5B59CB6DAFD284A9EB3BAC")
    }

    @Test
    fun parseTrailerDictionary_Pdf_1_4() {
        val underTest = PdfParser(PdfTestData.Empty_v1_4_Uncompressed)

        val result = underTest.parseDocument()

        assertTrailerDictionary_1_4(result)
    }

    @Test
    fun parseTrailerDictionaryEfficiently_Pdf_1_4() {
        val underTest = PdfParser(PdfTestData.Empty_v1_4_Uncompressed)

        val result = underTest.parseDocumentEfficiently()

        assertTrailerDictionary_1_4(result)
    }

    private fun assertTrailerDictionary_1_4(result: PdfStructure) {
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


    @Test
    fun parseCrossRefTable_Pdf_1_4() {
        val underTest = PdfParser(PdfTestData.Empty_v1_4_Uncompressed)

        val result = underTest.parseDocument()

        assertCrossRefStream_1_4(result)
    }

    @Test
    fun parseCrossRefTableEfficiently_Pdf_1_4() {
        val underTest = PdfParser(PdfTestData.Empty_v1_4_Uncompressed)

        val result = underTest.parseDocumentEfficiently()

        assertCrossRefStream_1_4(result)
    }

    private fun assertCrossRefStream_1_4(result: PdfStructure) {
        assertThat(result.usesCrossReferenceStream).isNotNull().isFalse()
        assertThat(result.xrefByteIndex).isEqualTo(130)

        assertThat(result.crossReferenceSection).isNotNull()
        val sections = result.crossReferenceSection!!.getSections()
        assertThat(sections).hasSize(1)
        val singleSection = sections[0]
        assertThat(singleSection).hasSize(3)
        assertThat(singleSection[0].offset).isEqualTo(0)
        assertThat(singleSection[1].offset).isEqualTo(15)
        assertThat(singleSection[2].offset).isEqualTo(78)
    }

    @Test
    fun parseCrossRefStream_Pdf_1_7() {
        val underTest = PdfParser(PdfTestData.Empty)

        val result = underTest.parseDocument()

        assertCrossRefStream_1_7(result)
    }

    @Test
    fun parseCrossRefStreamEfficiently_Pdf_1_7() {
        val underTest = PdfParser(PdfTestData.Empty)

        val result = underTest.parseDocumentEfficiently()

        assertCrossRefStream_1_7(result)
    }

    private fun assertCrossRefStream_1_7(result: PdfStructure) {
        assertThat(result.usesCrossReferenceStream).isNotNull().isTrue()
        assertThat(result.xrefByteIndex).isEqualTo(226)

        assertThat(result.crossReferenceSection).isNotNull()
        val sections = result.crossReferenceSection!!.getSections()
        assertThat(sections).hasSize(1)
        val singleSection = sections[0]
        assertThat(singleSection).hasSize(3)
        assertThat(singleSection[0].offset).isEqualTo(15)
        assertThat(singleSection[1].offset).isEqualTo(3)
        assertThat(singleSection[2].offset).isEqualTo(78)
    }

}