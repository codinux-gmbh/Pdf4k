package net.codinux.pdf.core.parser

import net.codinux.pdf.core.MissingKeywordError
import net.codinux.pdf.core.MissingPdfHeaderError
import net.codinux.pdf.core.StalledParserError
import net.codinux.pdf.core.document.PdfContext
import net.codinux.pdf.core.document.PdfHeader
import net.codinux.pdf.core.document.PdfTrailer
import net.codinux.pdf.core.document.TrailerInfo
import net.codinux.pdf.core.objects.PdfCrossRefSection
import net.codinux.pdf.core.objects.PdfName
import net.codinux.pdf.core.objects.PdfRawStream
import net.codinux.pdf.core.objects.PdfRef
import net.codinux.pdf.core.syntax.CharCodes
import net.codinux.pdf.core.syntax.Keywords

open class PdfParser(
    pdfBytes: ByteArray,
    protected val throwOnInvalidObject: Boolean = false,
    capNumbers: Boolean = false
) : PdfObjectParser(ByteStream(pdfBytes), PdfContext(), capNumbers) {

    protected var alreadyParsed = false


    open fun parseDocument(): PdfContext {
        if (alreadyParsed) {
            throw IllegalStateException("Document already parsed")
        }
        alreadyParsed = true

        context.header = parseHeader()

        var previousOffset: Int = -1
        while (bytes.hasNext()) {
            parseDocumentSection()

            val offset = bytes.offset()
            if (offset == previousOffset) {
                throw StalledParserError(bytes.position())
            }
            previousOffset = offset
        }

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
        if (matchKeyword(Keywords.Obj) == false) {
            throw MissingKeywordError(bytes.position(), Keywords.Obj)
        }

        return PdfRef.getOrCreate(referencePool, objectNumber, generationNumber)
    }

    protected open fun matchIndirectObjectHeader(): Boolean {
        val initialOffset = bytes.offset()

        try {
            parseIndirectObjectHeader()
            return true
        } catch (e: Throwable) {
            bytes.moveTo(initialOffset)
            return false
        }
    }


    protected open fun parseIndirectObject(): PdfRef {
        val ref = parseIndirectObjectHeader()

        skipWhitespaceAndComments()
        val pdfObject = parseObject()

        // TODO: Log a warning if this fails...
        matchKeyword(Keywords.Endobj)

        if (pdfObject is PdfRawStream && pdfObject.dict.get("Type") == PdfName.getOrCreate(namePool, "ObjStm")) {
            // TODO
        } else if (pdfObject is PdfRawStream && pdfObject.dict.get("Type") == PdfName.getOrCreate(namePool, "XRef")) {
            // TODO
        } else {
            context.assign(ref, pdfObject)
        }

        return ref
    }

    protected open fun parseIndirectObjects() {
        skipWhitespaceAndComments()

        while (bytes.hasNext() && isDigit(bytes.peek())) {
            val initialOffset = bytes.offset()

            try {
                parseIndirectObject()
            } catch (e: Throwable) {
                log.error(e) { "Could not parse indirect object" }
                bytes.moveTo(initialOffset)
                // TODO
//                tryToParseInvalidIndirectObject()
            }

            skipWhitespaceAndComments()

            // TODO: Can this be done only when needed, to avoid harming performance?
            skipJibberish()
        }
    }

    protected open fun maybeParseCrossRefSection(): PdfCrossRefSection? {
        skipWhitespaceAndComments()
        if (matchKeyword(Keywords.Xref) == false) {
            return null
        }
        skipWhitespaceAndComments()

        var objectNumber = -1
        val xref = PdfCrossRefSection()

        while (bytes.hasNext() && isDigit(bytes.peek())) {
            val firstInt = parseRawInt()
            skipWhitespaceAndComments()

            val secondInt = parseRawInt()
            skipWhitespaceAndComments()

            val byte = bytes.peek()
            if (byte == CharCodes.n || byte == CharCodes.f) {
                val ref = PdfRef.getOrCreate(referencePool, objectNumber, secondInt)
                if (bytes.next() == CharCodes.n) {
//                    xref.addEntry(ref, firstInt) // TODO
                } else {
                    // this.context.delete(ref)
//                    xref.addDeletedEntry(ref, firstInt) // TODO
                }

                objectNumber += 1
            } else {
                objectNumber = firstInt
            }

            skipWhitespaceAndComments()
        }

        return xref
    }

    protected open fun maybeParseTrailerDict() {
        skipWhitespaceAndComments()
        if (matchKeyword(Keywords.Trailer) == false) {
            return
        }
        skipWhitespaceAndComments()

        val dict = parseDict()
        context.trailerInfo = TrailerInfo(
            root = dict.get("Root") ?: context.trailerInfo?.root,
            encrypt = dict.get("Encrypt") ?: context.trailerInfo?.encrypt,
            info = dict.get("Info") ?: context.trailerInfo?.info,
            id = dict.get("ID") ?: context.trailerInfo?.id,
        )
    }

    protected open fun maybeParseTrailer(): PdfTrailer? {
        skipWhitespaceAndComments()
        if (matchKeyword(Keywords.StartXref) == false) {
            return null
        }
        skipWhitespaceAndComments()

        val offset = parseRawInt()

        skipWhitespace()
        matchKeyword(Keywords.EOF)
        skipWhitespaceAndComments()
        matchKeyword(Keywords.EOF)

        return PdfTrailer(offset)
    }

    protected open fun parseDocumentSection() {
        parseIndirectObjects()
        maybeParseCrossRefSection()
        maybeParseTrailerDict()
        maybeParseTrailer()

        // TODO: Can this be done only when needed, to avoid harming performance?
        skipJibberish()
    }

    /**
     * This operation is not necessary for valid PDF files. But some invalid PDFs
     * contain jibberish in between indirect objects. This method is designed to
     * skip past that jibberish, should it exist, until it reaches the next
     * indirect object header, an xref table section, or the file trailer.
     */
    protected open fun skipJibberish() {
        skipWhitespaceAndComments()

        while (bytes.hasNext()) {
            val initialOffset = bytes.offset()
            val byte = bytes.peek()
            val isAlphaNumeric = byte >= CharCodes.Space && byte <= CharCodes.Tilde

            if (isAlphaNumeric) {
                if (matchKeyword(Keywords.Xref) || matchKeyword(Keywords.Trailer) ||
                    matchKeyword(Keywords.StartXref) || matchIndirectObjectHeader()) {
                    bytes.moveTo(initialOffset)
                    break
                }
            }

            bytes.next()
        }
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