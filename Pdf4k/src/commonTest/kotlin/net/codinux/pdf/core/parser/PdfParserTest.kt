package net.codinux.pdf.core.parser

import assertk.assertThat
import assertk.assertions.*
import net.codinux.pdf.api.PdfDocument
import net.codinux.pdf.core.objects.PdfRef
import net.codinux.pdf.test.PdfTestData
import kotlin.test.Test

class PdfParserTest {

    @Test
    fun parseHeaderEagerly_Pdf_1_7() {
        val underTest = PdfParser(PdfTestData.EmptyPdf_v1_7)

        val result = underTest.parseDocumentEagerly()

        assertHeader_1_7(result)
    }

    @Test
    fun parseHeader_Pdf_1_7() {
        val underTest = PdfParser(PdfTestData.EmptyPdf_v1_7)

        val result = underTest.parseDocument()

        assertHeader_1_7(result)
    }

    private fun assertHeader_1_7(result: PdfDocument) {
        assertThat(result.pdfVersion).isEqualTo(1.7f)
    }

    @Test
    fun parseHeaderEagerly_Pdf_1_4() {
        val underTest = PdfParser(PdfTestData.EmptyPdf_v1_4_Uncompressed)

        val result = underTest.parseDocumentEagerly()

        assertHeader_1_4(result)
    }

    @Test
    fun parseHeader_Pdf_1_4() {
        val underTest = PdfParser(PdfTestData.EmptyPdf_v1_4_Uncompressed)

        val result = underTest.parseDocument()

        assertHeader_1_4(result)
    }

    private fun assertHeader_1_4(result: PdfDocument) {
        assertThat(result.pdfVersion).isEqualTo(1.4f)
    }


    @Test
    fun parseTrailerDictionaryEagerly_Pdf_1_7() {
        val underTest = PdfParser(PdfTestData.EmptyPdf_v1_7)

        val result = underTest.parseDocumentEagerly()

        assertTrailerDictionary_1_7(result)
    }

    @Test
    fun parseTrailerDictionary_Pdf_1_7() {
        val underTest = PdfParser(PdfTestData.EmptyPdf_v1_7)

        val result = underTest.parseDocument()

        assertTrailerDictionary_1_7(result)
    }

    private fun assertTrailerDictionary_1_7(result: PdfDocument) {
        // this PDF's trailer dictionary contains only /Root and /ID (= creationHash and lastModifiedHash)
        assertThat(result.catalog.language).isNull()

        assertThat(result.documentInfo).isNull()
        assertThat(result.isEncrypted).isFalse()

        assertThat(result.lowLevelDetails.creationHash).isEqualTo("66064282FD5B59CB6DAFD284A9EB3BAC")
        assertThat(result.lowLevelDetails.lastModifiedHash).isEqualTo("66064282FD5B59CB6DAFD284A9EB3BAC")
    }

    @Test
    fun parseTrailerDictionaryEagerly_Pdf_1_4() {
        val underTest = PdfParser(PdfTestData.EmptyPdf_v1_4_Uncompressed)

        val result = underTest.parseDocumentEagerly()

        assertTrailerDictionary_1_4(result)
    }

    @Test
    fun parseTrailerDictionary_Pdf_1_4() {
        val underTest = PdfParser(PdfTestData.EmptyPdf_v1_4_Uncompressed)

        val result = underTest.parseDocument()

        assertTrailerDictionary_1_4(result)
    }

    private fun assertTrailerDictionary_1_4(result: PdfDocument) {
        // this PDF's trailer dictionary contains only /Root and /ID (= creationHash and lastModifiedHash)
        assertThat(result.catalog.language).isNull()

        assertThat(result.documentInfo).isNull()
        assertThat(result.isEncrypted).isFalse()

        assertThat(result.lowLevelDetails.creationHash).isEqualTo("3E8D5A4F84D0B77955DA8E168A9055E7")
        assertThat(result.lowLevelDetails.lastModifiedHash).isEqualTo("3E8D5A4F84D0B77955DA8E168A9055E7")
    }


    @Test
    fun parseCrossRefTableEagerly_Pdf_1_4() {
        val underTest = PdfParser(PdfTestData.EmptyPdf_v1_4_Uncompressed)

        val result = underTest.parseDocumentEagerly()

        assertCrossRefStream_1_4(result)
    }

    @Test
    fun parseCrossRefTable_Pdf_1_4() {
        val underTest = PdfParser(PdfTestData.EmptyPdf_v1_4_Uncompressed)

        val result = underTest.parseDocument()

        assertCrossRefStream_1_4(result)
    }

    private fun assertCrossRefStream_1_4(result: PdfDocument) {
        assertThat(result.lowLevelDetails.xrefByteIndex).isEqualTo(130)
        assertThat(result.lowLevelDetails.usesCrossReferenceStream).isFalse()

        assertThat(result.referencesToByteOffset).hasSize(2)
        val references = result.referencesToByteOffset.entries.toList()
        assertThat(references[0].key).isEqualTo(PdfRef(1, 0))
        assertThat(references[0].value).isEqualTo(15)
        assertThat(references[1].key).isEqualTo(PdfRef(2, 0))
        assertThat(references[1].value).isEqualTo(78)
    }

    @Test
    fun parseCrossRefStreamEagerly_Pdf_1_7() {
        val underTest = PdfParser(PdfTestData.EmptyPdf_v1_7)

        val result = underTest.parseDocumentEagerly()

        assertCrossRefStream_1_7(result)
    }

    @Test
    fun parseCrossRefStream_Pdf_1_7() {
        val underTest = PdfParser(PdfTestData.EmptyPdf_v1_7)

        val result = underTest.parseDocument()

        assertCrossRefStream_1_7(result)
    }

    private fun assertCrossRefStream_1_7(result: PdfDocument) {
        assertThat(result.lowLevelDetails.xrefByteIndex).isEqualTo(226)
        assertThat(result.lowLevelDetails.usesCrossReferenceStream).isTrue()

        assertThat(result.referencesToByteOffset).hasSize(3)
        val references = result.referencesToByteOffset.entries.toList()
        assertThat(references[0].key).isEqualTo(PdfRef(1, 0))
        assertThat(references[0].value).isEqualTo(15)
        assertThat(references[1].key).isEqualTo(PdfRef(2, 0))
        assertThat(references[1].value).isEqualTo(3)
        assertThat(references[2].key).isEqualTo(PdfRef(3, 0))
        assertThat(references[2].value).isEqualTo(78)
    }

}