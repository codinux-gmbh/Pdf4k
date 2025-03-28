package net.codinux.pdf.core.parser

import net.codinux.log.logger
import net.codinux.pdf.core.NumberParsingError
import net.codinux.pdf.core.syntax.CharCodes

@OptIn(ExperimentalUnsignedTypes::class)
open class BaseParser(
    protected val bytes: ByteStream,
    protected val capNumbers: Boolean = false
) {

    companion object {
        val NumericPrefixes = listOf(CharCodes.Period, CharCodes.Plus, CharCodes.Minus)

        val Delimiters = listOf(CharCodes.LeftParenthesis, CharCodes.RightParenthesis, CharCodes.LessThan, CharCodes.GreaterThan,
            CharCodes.LeftSquareBracket, CharCodes.RightSquareBracket, CharCodes.LeftCurly, CharCodes.RightCurly,
            CharCodes.ForwardSlash, CharCodes.Percent)
    }


    protected val log by logger()


    protected open fun parseRawInt(): Int {
        var number = 0
        var hasParsedDigits = false

        while (bytes.hasNext()) {
            val byte = bytes.peek()
            if (isDigit(byte) == false) {
                break
            }

            number = number * 10 + toDigit(bytes.next())
            hasParsedDigits = true
        }

        if (hasParsedDigits == false /* || value.isFinite() == false */) {
            throw NumberParsingError(bytes.position(), number.toString())
        }

        return number
    }

    // TODO: Maybe handle exponential format?
    // TODO: Compare performance of string concatenation to charFromCode(...bytes)
    protected open fun parseRawNumber(): Number {
        var value = ""

        // Parse integer-part, the leading (+ | - | . | 0-9)
        while (bytes.hasNext()) {
            val byte = bytes.peek()
            if (isNumeric(byte) == false) {
                break
            }

            value += charFromCode(bytes.next())

            if (byte == CharCodes.Period) {
                break
            }
        }

        // Parse decimal-part, the trailing (0-9)
        while (bytes.hasNext()) {
            val byte = bytes.peek()
            if (isDigit(byte) == false) {
                break
            }

            value += charFromCode(bytes.next())
        }

        if (value.isBlank() || value.toDoubleOrNull() == null) { // original: || !isFinite(numberValue)
            throw NumberParsingError(bytes.position(), value)
        }

        val number = if (value.contains('.')) value.toDouble()
                    else {
                        val long = value.toLong()
                        if (long < Int.MAX_VALUE) long.toInt() else long
                    }

//        if (numberValue > Number.MAX_SAFE_INTEGER) {
//            if (this.capNumbers) {
//                const msg = `Parsed number that is too large for some PDF readers: ${value}, using Number.MAX_SAFE_INTEGER instead.`;
//                console.warn(msg);
//                return Number.MAX_SAFE_INTEGER;
//            } else {
//                const msg = `Parsed number that is too large for some PDF readers: ${value}, not capping.`;
//                console.warn(msg);
//            }
//        }

        return number
    }

    protected open fun matchKeyword(keyword: UByteArray): Boolean {
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
        if (bytes.done() || bytes.peek() != CharCodes.Percent) {
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


    protected open fun isDigit(byte: UByte): Boolean = charFromCode(byte).isDigit()

    protected open fun isNumericPrefix(byte: UByte): Boolean = byte in NumericPrefixes

    protected open fun isNumeric(byte: UByte): Boolean = isDigit(byte) || isNumericPrefix(byte)

    protected open fun isWhitespace(byte: UByte): Boolean = charFromCode(byte).isWhitespace()

    protected open fun isDelimiter(byte: UByte): Boolean = byte in Delimiters

    // TODO: this is the same code as in TextDecoder
    protected open fun charFromCode(byte: UByte): Char = byte.toInt().toChar()

    protected open fun toDigit(byte: UByte): Int = charFromCode(byte).digitToInt()

}