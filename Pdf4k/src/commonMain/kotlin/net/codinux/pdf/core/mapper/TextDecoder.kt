package net.codinux.pdf.core.mapper

import net.codinux.log.logger
import net.codinux.pdf.core.syntax.CharCodes

@OptIn(ExperimentalUnsignedTypes::class)
open class TextDecoder {

    companion object {
        val Instance = TextDecoder()

        // Mapping from PDFDocEncoding to Unicode code point
        val PdfDocEncodingToUnicode = buildMap<UByte, UByte> {
            // Initialize the code points which are the same
            (0..255).forEach { codePoint ->
                put(codePoint.toUByte(), codePoint.toUByte())
            }

            // Set differences (see "Table D.2 – PDFDocEncoding Character Set" of the PDF spec)
            put(0x16.toUByte(), '\u0017'.code.toUByte()) // SYNCRONOUS IDLE
            put(0x18.toUByte(), '\u02D8'.code.toUByte()) // BREVE
            put(0x19.toUByte(), '\u02C7'.code.toUByte()) // CARON
            put(0x1a.toUByte(), '\u02C6'.code.toUByte()) // MODIFIER LETTER CIRCUMFLEX ACCENT
            put(0x1b.toUByte(), '\u02D9'.code.toUByte()) // DOT ABOVE
            put(0x1c.toUByte(), '\u02DD'.code.toUByte()) // DOUBLE ACUTE ACCENT
            put(0x1d.toUByte(), '\u02DB'.code.toUByte()) // OGONEK
            put(0x1e.toUByte(), '\u02DA'.code.toUByte()) // RING ABOVE
            put(0x1f.toUByte(), '\u02DC'.code.toUByte()) // SMALL TILDE
            put(0x7f.toUByte(), '\uFFFD'.code.toUByte()) // REPLACEMENT CHARACTER (box with questionmark)
            put(0x80.toUByte(), '\u2022'.code.toUByte()) // BULLET
            put(0x81.toUByte(), '\u2020'.code.toUByte()) // DAGGER
            put(0x82.toUByte(), '\u2021'.code.toUByte()) // DOUBLE DAGGER
            put(0x83.toUByte(), '\u2026'.code.toUByte()) // HORIZONTAL ELLIPSIS
            put(0x84.toUByte(), '\u2014'.code.toUByte()) // EM DASH
            put(0x85.toUByte(), '\u2013'.code.toUByte()) // EN DASH
            put(0x86.toUByte(), '\u0192'.code.toUByte()) // LATIN SMALL LETTER SCRIPT F
            put(0x87.toUByte(), '\u2044'.code.toUByte()) // FRACTION SLASH (solidus)
            put(0x88.toUByte(), '\u2039'.code.toUByte()) // SINGLE LEFT-POINTING ANGLE QUOTATION MARK
            put(0x89.toUByte(), '\u203A'.code.toUByte()) // SINGLE RIGHT-POINTING ANGLE QUOTATION MARK
            put(0x8a.toUByte(), '\u2212'.code.toUByte()) // MINUS SIGN
            put(0x8b.toUByte(), '\u2030'.code.toUByte()) // PER MILLE SIGN
            put(0x8c.toUByte(), '\u201E'.code.toUByte()) // DOUBLE LOW-9 QUOTATION MARK (quotedblbase)
            put(0x8d.toUByte(), '\u201C'.code.toUByte()) // LEFT DOUBLE QUOTATION MARK (quotedblleft)
            put(0x8e.toUByte(), '\u201D'.code.toUByte()) // RIGHT DOUBLE QUOTATION MARK (quotedblright)
            put(0x8f.toUByte(), '\u2018'.code.toUByte()) // LEFT SINGLE QUOTATION MARK (quoteleft)
            put(0x90.toUByte(), '\u2019'.code.toUByte()) // RIGHT SINGLE QUOTATION MARK (quoteright)
            put(0x91.toUByte(), '\u201A'.code.toUByte()) // SINGLE LOW-9 QUOTATION MARK (quotesinglbase)
            put(0x92.toUByte(), '\u2122'.code.toUByte()) // TRADE MARK SIGN
            put(0x93.toUByte(), '\uFB01'.code.toUByte()) // LATIN SMALL LIGATURE FI
            put(0x94.toUByte(), '\uFB02'.code.toUByte()) // LATIN SMALL LIGATURE FL
            put(0x95.toUByte(), '\u0141'.code.toUByte()) // LATIN CAPITAL LETTER L WITH STROKE
            put(0x96.toUByte(), '\u0152'.code.toUByte()) // LATIN CAPITAL LIGATURE OE
            put(0x97.toUByte(), '\u0160'.code.toUByte()) // LATIN CAPITAL LETTER S WITH CARON
            put(0x98.toUByte(), '\u0178'.code.toUByte()) // LATIN CAPITAL LETTER Y WITH DIAERESIS
            put(0x99.toUByte(), '\u017D'.code.toUByte()) // LATIN CAPITAL LETTER Z WITH CARON
            put(0x9a.toUByte(), '\u0131'.code.toUByte()) // LATIN SMALL LETTER DOTLESS I
            put(0x9b.toUByte(), '\u0142'.code.toUByte()) // LATIN SMALL LETTER L WITH STROKE
            put(0x9c.toUByte(), '\u0153'.code.toUByte()) // LATIN SMALL LIGATURE OE
            put(0x9d.toUByte(), '\u0161'.code.toUByte()) // LATIN SMALL LETTER S WITH CARON
            put(0x9e.toUByte(), '\u017E'.code.toUByte()) // LATIN SMALL LETTER Z WITH CARON
            put(0x9f.toUByte(), '\uFFFD'.code.toUByte()) // REPLACEMENT CHARACTER (box with questionmark)
            put(0xa0.toUByte(), '\u20AC'.code.toUByte()) // EURO SIGN
            put(0xad.toUByte(), '\uFFFD'.code.toUByte()) // REPLACEMENT CHARACTER (box with questionmark)
        }
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

    /**
     * A string object in a PDF can have three different encodings:
     * - PDFDocEncoding
     * - UTF-8
     * - UTF-16BE
     *
     * With encoding is used can be distinguished by:
     *
     * For text strings encoded in UTF-8, the first three bytes shall be 239 followed by 187, followed by 191.
     * These three bytes represent the Unicode byte order marker indicating that the string is encoded in the
     * UTF-8 encoding scheme specified in Unicode.
     *
     * For text strings encoded in UTF-16BE, the first two bytes shall be 254 followed by 255. These two
     * bytes represent the Unicode byte order marker, ZERO WIDTH NO-BREAK SPACE (U+FEFF), indicating
     * that the string is encoded in the UTF-16BE (big-endian) encoding scheme specified in Unicode.
     */
    open fun decodeText(text: UByteArray): String {
        val unescaped = unescapeBytes(text)

        return if (isUtf16Be(unescaped)) {
            decodeUtf16Be(unescaped)
        } else if (isUtf8(unescaped)) {
            decodeUtf8(unescaped)
        } else {
            decodePdfDocEncoding(unescaped)
        }
    }


    protected open fun decodePdfDocEncoding(text: UByteArray): String {
        return text.map { PdfDocEncodingToUnicode[it]!! }.toUByteArray().toByteArray().decodeToString()
    }

    protected open fun isUtf8(text: UByteArray): Boolean =
        text.size >= 3 && text[0] == 239.toUByte() && text[1] == 187.toUByte() && text[2] == 191.toUByte()

    protected open fun decodeUtf8(text: UByteArray): String =
        text.toByteArray().decodeToString(startIndex = 3)

    protected open fun isUtf16Be(text: UByteArray): Boolean =
        text.size >= 2 && text[0] == 254.toUByte() && text[1] == 255.toUByte()

    protected open fun decodeUtf16Be(text: UByteArray): String =
        text.toByteArray().decodeToString(startIndex = 2) // TODO: decode UTF-16


    /**
     * Within a literal string, the REVERSE SOLIDUS is used as an escape character. The character
     * immediately following the REVERSE SOLIDUS determines its precise interpretation as shown in "Table
     * 3 — Escape sequences in literal strings". If the character following the REVERSE SOLIDUS is not one of
     * those shown in "Table 3 — Escape sequences in literal strings", the REVERSE SOLIDUS shall be
     * ignored.
     */
    protected open fun unescapeBytes(text: UByteArray): UByteArray =
        if (text.none { it == CharCodes.BackSlash }) {
            text
        } else {
            val unescaped = mutableListOf<UByte>()
            var index = 0

            while (index < text.size) {
                val byte = text[index]
                if (byte != CharCodes.BackSlash || index == text.size - 1) {
                    unescaped.add(byte)
                    index++
                } else { // a Backslash = escape character for below characters or three octal numbers (otherwise simply a backslash)
                    val nextByte = text[index + 1]
                    when (nextByte) {
                        CharCodes.n -> unescaped.add(CharCodes.Newline)
                        CharCodes.r -> unescaped.add(CharCodes.CarriageReturn)
                        CharCodes.t -> unescaped.add(CharCodes.Tab)
                        CharCodes.b -> unescaped.add(CharCodes.Backspace)
                        CharCodes.f -> unescaped.add(CharCodes.FormFeed)
                        CharCodes.CarriageReturn, CharCodes.Backspace,
                        CharCodes.LeftParenthesis, CharCodes.RightParenthesis -> unescaped.add(nextByte)
                        else -> {
                            if (isOctal(nextByte) && index < text.size - 3 && isOctal(text[index + 2]) && isOctal(text[index + 3])) { // a Char encoded as Octal number
                                val char = (charFromCode(nextByte).toString() + charFromCode(text[index + 2]) + charFromCode(text[index + 3])).toInt(8).toUByte()
                                unescaped.add(char)
                                index += 2 // we consumed to additional bytes to below's two bytes
                            } else { // otherwise simply add the backlash
                                unescaped.add(byte)
                                index -= 1 // below index is increased by two, but actually we consumed only one byte
                            }
                        }
                    }

                    index += 2 // we assume a single escaped characters and my correct this in else branch
                }
            }

            unescaped.toUByteArray()
        }


    // TODO: this is the same code as in BaseParser
    open fun charFromCode(byte: UByte): Char = byte.toInt().toChar()

    protected open fun isOctal(byte: UByte): Boolean =
        byte >= CharCodes.Zero && byte <= CharCodes.Seven

}