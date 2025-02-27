package net.codinux.pdf.core.parser

import net.codinux.pdf.core.NumberParsingError
import net.codinux.pdf.core.syntax.CharCodes

open class BaseParser(
    protected val bytes: ByteStream,
    protected val capNumbers: Boolean = false
) {

    protected open fun parseRawInt(): Int {
        var number = 0

        while (bytes.hasNext()) {
            val byte = bytes.peek()
            if (isDigit(byte) == false) {
                break
            }
            number = number * 10 + toDigit(bytes.next())
        }

        if (number == 0 /* || value.isFinite() == false */) {
            throw NumberParsingError(bytes.position(), number)
        }

        return number
    }

    protected open fun matchKeyword(keyword: ByteArray): Boolean {
        val initialOffset = bytes.offset()

        return keyword.all { keywordByte ->
            if (bytes.done() || bytes.next() != keywordByte) {
                bytes.moveTo(initialOffset)
                false
            } else {
                true
            }
        }
    }


    protected open fun skipWhitespace() {
        while (bytes.hasNext() && isWhitespace(bytes.peek())) {
            bytes.next()
        }
    }

    protected open fun skipComment(): Boolean {
        if (bytes.peek() != CharCodes.Percent) {
            return false
        }

        while (bytes.hasNext()) {
            val byte = bytes.peek()
            if (byte == CharCodes.Newline || byte == CharCodes.CarriageReturn) {
                return true
            }

            bytes.next()
        }

        return true
    }

    protected open fun skipWhitespaceAndComments() {
        skipWhitespace()

        while (skipComment()) {
            skipWhitespace()
        }
    }


    protected open fun isDigit(byte: Byte): Boolean = asChar(byte).isDigit()

    protected open fun isWhitespace(byte: Byte): Boolean = asChar(byte).isWhitespace()

    protected open fun asChar(byte: Byte): Char = byte.toInt().toChar()

    protected open fun toDigit(byte: Byte): Int = asChar(byte).digitToInt()

}