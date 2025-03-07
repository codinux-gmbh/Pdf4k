package net.codinux.pdf.core.parser

import net.codinux.pdf.core.PdfObjectParsingError
import net.codinux.pdf.core.PdfStreamParsingError
import net.codinux.pdf.core.UnbalancedParenthesisError
import net.codinux.pdf.core.objects.*
import net.codinux.pdf.core.structures.PdfCatalog
import net.codinux.pdf.core.structures.PdfPageLeaf
import net.codinux.pdf.core.structures.PdfPageTree
import net.codinux.pdf.core.syntax.CharCodes
import net.codinux.pdf.core.syntax.Keywords

open class PdfObjectParser(
    bytes: ByteStream,
    capNumbers: Boolean = false
) : BaseParser(bytes, capNumbers) {

    protected val referencePool = mutableMapOf<String, PdfRef>()

    protected val namePool = mutableMapOf<String, PdfName>()


    protected open fun parseObject(): PdfObject {
        skipWhitespaceAndComments()

        when {
            matchKeyword(Keywords.True) -> return PdfBool.True
            matchKeyword(Keywords.False) -> return PdfBool.False
            matchKeyword(Keywords.Null) -> return PdfNull.Null
        }

        val byte = bytes.peek()

        if (byte == CharCodes.LessThan && bytes.peekAhead(1) == CharCodes.LessThan) {
            return parseDictOrStream()
        }

        return when (byte) {
            CharCodes.LessThan -> parseHexString()
            CharCodes.LeftParenthesis -> parseString()
            CharCodes.ForwardSlash -> parseName()
            CharCodes.LeftSquareBracket -> parseArray()
            else -> {
                if (isNumeric(byte)) parseNumberOrRef()
                else throw PdfObjectParsingError(bytes.position(), byte)
            }
        }
    }


    protected open fun parseNumberOrRef(): PdfObject { // PdfNumber | PdfRef
        val firstNum = parseRawNumber()
        skipWhitespaceAndComments()

        val lookaheadStart = bytes.offset()
        if (isDigit(bytes.peek())) {
            val secondNum = parseRawNumber()
            skipWhitespaceAndComments()

            if (bytes.peek() == CharCodes.R) {
                bytes.assertNext(CharCodes.R)
                return PdfRef.getOrCreate(referencePool, firstNum.toInt(), secondNum.toInt())
            }
        }

        bytes.moveTo(lookaheadStart)
        return PdfNumber(firstNum)
    }

    // TODO: Maybe update PDFHexString.of() logic to remove whitespace and validate input?
    protected open fun parseHexString(): PdfHexString {
        var value = ""

        bytes.assertNext(CharCodes.LessThan)
        while (bytes.hasNext() && bytes.peek() != CharCodes.GreaterThan) {
            value += charFromCode(bytes.next())
        }
        bytes.assertNext(CharCodes.GreaterThan)

        return PdfHexString(value)
    }

    protected open fun parseString(): PdfString {
        var nestingLevel = 0
        var isEscaped = false
        var value = ""

        while (bytes.hasNext()) {
            val byte = bytes.next()
            value += charFromCode(byte)

            // Check for unescaped parenthesis
            if (isEscaped == false) {
                when (byte) {
                    CharCodes.LeftParenthesis -> nestingLevel += 1
                    CharCodes.RightParenthesis -> nestingLevel -= 1
                }
            }

            // Track whether current character is being escaped or not
            if (byte == CharCodes.BackSlash) {
                isEscaped = !!!isEscaped
            } else if (isEscaped) {
                isEscaped = false
            }

            // Once (if) the unescaped parenthesis balance out, return their contents
            if (nestingLevel == 0) {
                // Remove the outer parens so they aren't part of the contents
                return PdfString(value.substring(1, value.length - 1))
            }
        }

        throw UnbalancedParenthesisError(bytes.position())
    }

    // TODO: Compare performance of string concatenation to charFromCode(...bytes)
    // TODO: Maybe preallocate small Uint8Array if can use charFromCode?
    protected open fun parseName(): PdfName {
        bytes.assertNext(CharCodes.ForwardSlash)

        var name = ""
        while (bytes.hasNext()) {
            val byte = bytes.peek()
            if (isWhitespace(byte) || isDelimiter(byte)) {
                break
            }

            name += charFromCode(byte)
            bytes.next()
        }

        return PdfName.getOrCreate(namePool, name)
    }

    protected open fun parseArray(): PdfArray {
        bytes.assertNext(CharCodes.LeftSquareBracket)
        skipWhitespaceAndComments()

        val items = mutableListOf<PdfObject>()
        while (bytes.peek() != CharCodes.RightSquareBracket) {
            items.add(parseObject())
            skipWhitespaceAndComments()
        }
        bytes.assertNext(CharCodes.RightSquareBracket)

        return PdfArray(items)
    }

    protected open fun parseDict(): PdfDict {
        bytes.assertNext(CharCodes.LessThan)
        bytes.assertNext(CharCodes.LessThan)
        skipWhitespaceAndComments()

        val items = mutableMapOf<PdfName, PdfObject>()
        var type: String? = null

        while (bytes.hasNext() && bytes.peek() != CharCodes.GreaterThan && bytes.peekAhead(1) != CharCodes.GreaterThan) {
            val key = parseName()
            val value = parseObject()
            items[key] = value
            skipWhitespaceAndComments()

            if (key.name == "Type" && value is PdfName) {
                type = value.name
            }
        }

        skipWhitespaceAndComments()
        bytes.assertNext(CharCodes.GreaterThan)
        bytes.assertNext(CharCodes.GreaterThan)

        return when (type) {
            "Catalog" -> PdfCatalog(items)
            "Pages" -> PdfPageTree(items)
            "Page" -> PdfPageLeaf(items)
            else -> PdfDict(items)
        }
    }

    protected open fun parseDictOrStream(): PdfObject { // PdfDict | PdfStream
        val startPos = bytes.position()

        val dict = parseDict()

        skipWhitespaceAndComments()

        if (matchKeyword(Keywords.StreamEOF1) == false && matchKeyword(Keywords.StreamEOF2) == false &&
            matchKeyword(Keywords.StreamEOF3) == false && matchKeyword(Keywords.StreamEOF4) == false &&
            matchKeyword(Keywords.Stream) == false) {
            return dict
        }

        val start = bytes.offset()
        var end: Int = -1

        val length = dict.get("Length")
        if (length is PdfNumber) {
            end = start + length.value.toInt()
            bytes.moveTo(end)
            skipWhitespaceAndComments()

            if (matchKeyword(Keywords.Endstream) == false) {
                bytes.moveTo(start)
                end = findEndOfStreamFallback(startPos)
            }
        } else {
            end = findEndOfStreamFallback(startPos)
        }

        val contents = bytes.slice(start, end)

        return PdfRawStream(dict, contents)
    }

    protected open fun findEndOfStreamFallback(startPos: ByteStream.Position): Int {
        // Move to end of stream, while handling nested streams
        var nestingLevel = 1
        var end = bytes.offset()

        while (bytes.hasNext()) {
            end = bytes.offset()

            if (matchKeyword(Keywords.Stream)) {
                nestingLevel += 1
            } else if (matchKeyword(Keywords.EOF1endstream) || matchKeyword(Keywords.EOF2endstream) ||
                matchKeyword(Keywords.EOF3endstream) || matchKeyword(Keywords.Endstream)) {
                nestingLevel -= 1
            } else {
                bytes.next()
            }

            if (nestingLevel == 0) {
                break
            }
        }

        if (nestingLevel != 0) {
            throw PdfStreamParsingError(startPos)
        }

        return end
    }

}