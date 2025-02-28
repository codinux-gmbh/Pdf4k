package net.codinux.pdf.core

import net.codinux.pdf.core.parser.ByteStream


open class NumberParsingError(val position: ByteStream.Position, val number: String)
    : IllegalStateException("Failed to parse number at $position: $number")

open class PdfParsingError(val position: ByteStream.Position, details: String)
    : IllegalStateException("Failed to parse PDF document at $position: $details")


open class NextByteAssertionError(position: ByteStream.Position, val expectedByte: Byte, val actualByte: Byte)
    : PdfParsingError(position, "Expected next byte to be $expectedByte but it was actually $actualByte")

open class PdfObjectParsingError(position: ByteStream.Position, byte: Byte)
    : PdfParsingError(position, "ailed to parse PDF object starting with the following byte: $byte")

open class PdfStreamParsingError(pos: ByteStream.Position) : PdfParsingError(pos, "Failed to parse PDF stream")

open class UnbalancedParenthesisError(position: ByteStream.Position) : PdfParsingError(position, "Failed to parse PDF literal string due to unbalanced parenthesis")

open class StalledParserError(position: ByteStream.Position) : PdfParsingError(position, "Parser stalled")


open class MissingPdfHeaderError(position: ByteStream.Position) : PdfParsingError(position, "No PDF header found")

open class MissingKeywordError(position: ByteStream.Position, keyword: ByteArray)
    : PdfParsingError(position, "Did not find expected keyword '${keyword.joinToString("") { it.toInt().toChar().toString() } }'")
