package net.codinux.pdf.core.mapper

import net.codinux.log.logger
import net.codinux.pdf.core.syntax.CharCodes

@OptIn(ExperimentalUnsignedTypes::class)
open class TextDecoder {

    companion object {
        val Instance = TextDecoder()
    }


    protected val log by logger()


    /**
     * For PdfName we need to decode '#' followed by two hexadecimal digits to char, see PdfName spec:
     *
     * When writing a name in a PDF file, a SOLIDUS (2Fh) (/) shall be used to introduce a name. The
     * SOLIDUS is not part of the name but is a prefix indicating that what follows is a sequence of characters
     * representing the name in the PDF file and shall follow these rules:
     *
     * a) A NUMBER SIGN (23h) (#) in a name shall be written by using its 2-digit hexadecimal code (23),
     * preceded by the NUMBER SIGN.
     *
     * b) Any character in a name that is a regular character (other than NUMBER SIGN) shall be written as itself
     * or by using its 2-digit hexadecimal code, preceded by the NUMBER SIGN.
     *
     * c) Any character that is not a regular character shall be written using its 2-digit hexadecimal code,
     * preceded by the NUMBER SIGN only.
     */
    open fun decodeName(text: UByteArray): String {
        val chars = mutableListOf<Char>()

        var index = 0
        while (index < text.size) {
            val byte = text[index]
            if (byte != CharCodes.Hash) {
                chars.add(charFromCode(byte))
                index++
            } else {
                if (index + 2 < text.size) {
                    val hexString = charFromCode(text[index + 1]).toString() + charFromCode(text[index + 2])
                    val codePoint = hexString.toInt(16)
                    val char = codePoint.toChar()
                    chars.add(char)
                } else {
                    log.warn { "After a hash (#) two hexadecimal digits are expected in string ${text.map { charFromCode(it) }.joinToString("")}" }
                }

                index += 3
            }
        }

        return chars.toCharArray().concatToString()
    }

    // TODO: this is the same code as in BaseParser
    open fun charFromCode(byte: UByte): Char = byte.toInt().toChar()

}