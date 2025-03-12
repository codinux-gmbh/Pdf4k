package net.codinux.pdf.core.mapper

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.js.JsName
import kotlin.test.Test

@OptIn(ExperimentalUnsignedTypes::class)
class TextDecoderTest {

    private val underTest = TextDecoder()


    @Test
    fun decodePdfDocEncodingWithEscapedOctalCodes() {
        val bytes = byteArrayOf(69, 78, 49, 54, 57, 51, 49, 92, 49, 51, 55, 69, 108, 101, 107, 116, 114, 111, 110, 92, 49, 51, 55, 65, 117, 102, 109, 97, 115, 115, 92, 48, 53, 54, 112, 110, 103)

        assertThat(decodeText(bytes)).isEqualTo("EN16931_Elektron_Aufmass.png")
    }

    @Test
    @JsName("can_interpret_UTF_16BE_strings_with_escaped_octal_codes")
    fun `can interpret UTF-16BE strings with escaped octal codes`() {
//        val literal = "\\376\\377\\000\\105\\000\\147\\000\\147\\000\\040\\330\\074\\337\\163"
        val literal = listOf(254, 255, 0, 69, 0, 103, 0, 103, 0, 32, 216, 60, 223, 115)

        assertThat(decodeText(literal)).isEqualTo("Egg \uD83C\uDF73")
    }


    private fun decodeText(bytes: List<Int>): String =
        decodeText(bytes.map { it.toUByte() }.toUByteArray())

    private fun decodeText(string: String): String =
        decodeText(string.toCharArray().map { it.code.toUByte() }.toUByteArray())

    private fun decodeText(bytes: ByteArray): String =
        decodeText(bytes.toUByteArray())

    private fun decodeText(bytes: UByteArray): String =
        underTest.decodeText(bytes)

}