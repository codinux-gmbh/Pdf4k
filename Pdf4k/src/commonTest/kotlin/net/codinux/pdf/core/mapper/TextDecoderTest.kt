package net.codinux.pdf.core.mapper

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

@OptIn(ExperimentalUnsignedTypes::class)
class TextDecoderTest {

    private val underTest = TextDecoder()


    @Test
    fun decodePdfDocEncodingWithEscapedOctalCodes() {
        val bytes = byteArrayOf(69, 78, 49, 54, 57, 51, 49, 92, 49, 51, 55, 69, 108, 101, 107, 116, 114, 111, 110, 92, 49, 51, 55, 65, 117, 102, 109, 97, 115, 115, 92, 48, 53, 54, 112, 110, 103)

        assertThat(decodeText(bytes)).isEqualTo("EN16931_Elektron_Aufmass.png")
    }


    private fun decodeText(bytes: ByteArray): String =
        decodeText(bytes.toUByteArray())

    private fun decodeText(bytes: UByteArray): String =
        underTest.decodeText(bytes)

}