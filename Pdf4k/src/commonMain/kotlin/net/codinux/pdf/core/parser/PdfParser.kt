package net.codinux.pdf.core.parser

import net.codinux.pdf.core.MissingKeywordError
import net.codinux.pdf.core.MissingPdfHeaderError
import net.codinux.pdf.core.document.PdfContext
import net.codinux.pdf.core.document.PdfHeader
import net.codinux.pdf.core.objects.PdfRef
import net.codinux.pdf.core.syntax.CharCodes
import net.codinux.pdf.core.syntax.Keywords

open class PdfParser(
    pdfBytes: ByteArray,
    protected val objectsPerTick: Int = Int.MAX_VALUE,
    protected val throwOnInvalidObject: Boolean = false,
    capNumbers: Boolean = false
) : PdfObjectParser(ByteStream(pdfBytes), PdfContext(), capNumbers) {

    protected var alreadyParsed = false
    protected var parsedObjects = 0

    protected val referencePool = mutableMapOf<String, PdfRef>()


    open fun parseDocument(): PdfContext {
        if (alreadyParsed) {
            throw IllegalStateException("Document already parsed")
        }
        alreadyParsed = true

        context.header = parseHeader()

        return context
    }


    protected open fun parseHeader(): PdfHeader? {
        while (bytes.hasNext()) {
            if (matchKeyword(Keywords.Header)) {
                val major = parseRawInt()
                bytes.assertNext(CharCodes.Period)
                val minor = parseRawInt()
                val header = PdfHeader(major, minor)
                skipBinaryHeaderComment()
                return header
            }

            bytes.next()
        }

        throw MissingPdfHeaderError(bytes.position())
    }

    protected open fun parseIndirectObjectHeader(): PdfRef {
        skipWhitespaceAndComments()
        val objectNumber = parseRawInt()

        skipWhitespaceAndComments()
        val generationNumber = parseRawInt()

        skipWhitespaceAndComments()
        if (matchKeyword(Keywords.obj) == false) {
            throw MissingKeywordError(bytes.position(), Keywords.obj)
        }

        return PdfRef.getOrCreate(referencePool, objectNumber, generationNumber)
    }


    /**
     * Skips the binary comment following a PDF header. The specification
     * defines this binary comment (section 7.5.2 File Header) as a sequence of 4
     * or more bytes that are 128 or greater, and which are preceded by a "%".
     *
     * This would imply that to strip out this binary comment, we could check for
     * a sequence of bytes starting with "%", and remove all subsequent bytes that
     * are 128 or greater. This works for many documents that properly comply with
     * the spec. But in the wild, there are PDFs that omit the leading "%", and
     * include bytes that are less than 128 (e.g. 0 or 1). So in order to parse
     * these headers correctly, we just throw out all bytes leading up to the
     * first indirect object header.
     */
    protected open fun skipBinaryHeaderComment() {
        skipWhitespaceAndComments()

        try {
            val initialOffset = bytes.offset()
            parseIndirectObjectHeader()
            bytes.moveTo(initialOffset)
        } catch (e: Throwable) {
            bytes.next()
            skipWhitespaceAndComments()
        }
    }

}