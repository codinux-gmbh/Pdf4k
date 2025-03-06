@file:OptIn(ExperimentalUnsignedTypes::class)

package net.codinux.pdf.core

import net.codinux.pdf.core.parser.ByteStream
import kotlin.reflect.KClass


open class Pdf4kError(message: String) : IllegalStateException(message)

open class UnexpectedObjectTypeError(expected: List<KClass<*>>, actual: KClass<*>?)
    : Pdf4kError("Expected instance of ${expected.joinToString()} but got ${actual ?: "null"}")

open class UnsupportedEncodingError(encoding: String) : Pdf4kError("$encoding stream encoding not supported")

open class ReparseError(className: String, methodName: String)
    : Pdf4kError("Cannot call ${className}.$methodName more than once")


/*        Parser Errors           */

open class NumberParsingError(val position: ByteStream.Position, val number: String)
    : Pdf4kError("Failed to parse number at $position: $number")

open class PdfParsingError(val position: ByteStream.Position, details: String)
    : Pdf4kError("Failed to parse PDF document at $position: $details")


open class NextByteAssertionError(position: ByteStream.Position, val expectedByte: UByte, val actualByte: UByte)
    : PdfParsingError(position, "Expected next byte to be $expectedByte but it was actually $actualByte")

open class PdfObjectParsingError(position: ByteStream.Position, byte: UByte)
    : PdfParsingError(position, "ailed to parse PDF object starting with the following byte: $byte")

open class PdfInvalidObjectParsingError(position: ByteStream.Position) : PdfParsingError(position, "Failed to parse invalid PDF object")

open class PdfStreamParsingError(pos: ByteStream.Position) : PdfParsingError(pos, "Failed to parse PDF stream")

open class UnbalancedParenthesisError(position: ByteStream.Position) : PdfParsingError(position, "Failed to parse PDF literal string due to unbalanced parenthesis")

open class StalledParserError(position: ByteStream.Position) : PdfParsingError(position, "Parser stalled")


open class MissingPdfHeaderError(position: ByteStream.Position) : PdfParsingError(position, "No PDF header found")

open class MissingKeywordError(position: ByteStream.Position, keyword: UByteArray)
    : PdfParsingError(position, "Did not find expected keyword '${keyword.joinToString("") { it.toInt().toChar().toString() } }'")
