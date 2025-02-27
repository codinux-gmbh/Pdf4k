package net.codinux.pdf.core

import net.codinux.pdf.core.parser.ByteStream


open class NumberParsingError(val position: ByteStream.Position, val number: Int)
    : IllegalStateException("Failed to parse number at $position: $number")

open class PdfParsingError(val position: ByteStream.Position, details: String)
    : IllegalStateException("Failed to parse PDF document at $position: $details")

open class MissingPdfHeaderError(position: ByteStream.Position) : PdfParsingError(position, "No PDF header found")

open class MissingKeywordError(position: ByteStream.Position, keyword: ByteArray)
    : PdfParsingError(position, "Did not find expected keyword '${keyword.joinToString("") { it.toInt().toChar().toString() } }'")
