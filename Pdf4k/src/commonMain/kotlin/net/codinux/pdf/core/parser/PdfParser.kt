package net.codinux.pdf.core.parser

import net.codinux.pdf.core.MissingKeywordError
import net.codinux.pdf.core.MissingPdfHeaderError
import net.codinux.pdf.core.ReparseError
import net.codinux.pdf.core.StalledParserError
import net.codinux.pdf.core.document.PdfContext
import net.codinux.pdf.core.document.PdfHeader
import net.codinux.pdf.core.document.PdfTrailer
import net.codinux.pdf.core.document.TrailerInfo
import net.codinux.pdf.core.objects.*
import net.codinux.pdf.core.streams.StreamDecoder
import net.codinux.pdf.core.syntax.CharCodes
import net.codinux.pdf.core.syntax.Keywords

@OptIn(ExperimentalUnsignedTypes::class)
open class PdfParser(
    pdfBytes: UByteArray,
    protected val throwOnInvalidObject: Boolean = false,
    capNumbers: Boolean = false,
    protected val streamDecoder: StreamDecoder = StreamDecoder.Instance
) : PdfObjectParser(ByteStream(pdfBytes), PdfContext(), capNumbers) {

    protected var alreadyParsed = false


    open fun parseDocument(): PdfContext {
        if (alreadyParsed) {
            throw ReparseError("PdfParser", "parseDocument")
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

        skipWhitespaceAndComments()

        // TODO: Log a warning if this fails...
        matchKeyword(Keywords.Endobj)

        val rawStreamType = if (pdfObject is PdfRawStream) pdfObject.dict.getAs<PdfName>(PdfName.Type)?.name else null
        if (rawStreamType == "ObjStm") {
            val indirectObjects = PdfObjectStreamParser(pdfObject as PdfRawStream, streamDecoder).parseIndirectObjects(referencePool)
            context.addIndirectObjects(indirectObjects)
        } else if (rawStreamType == "XRef") {
            val xrefStream = pdfObject as PdfRawStream

            // non-classic PDFs - that are PDF 1.5+ PDFs with cross-reference stream - store the Trailer info in
            // XRef stream instead of a separate Trailer dictionary at end of PDF file
            mapTrailerInfo(xrefStream.dict)

            val xrefStreamParser = PdfXRefStreamParser(xrefStream, streamDecoder)
            context.crossReferenceSection = xrefStreamParser.parseXrefStream(referencePool)
        } else {
            context.addIndirectObject(ref, pdfObject)
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
            // an Xref table entry in format <object byte address (= firstInt)> <generation number (= secondInt)> <n or f>
            if (byte == CharCodes.n || byte == CharCodes.f) {
                val ref = PdfRef.getOrCreate(referencePool, objectNumber, secondInt)
                if (bytes.next() == CharCodes.n) {
                    xref.addEntryThatIsInUse(ref, firstInt)
                } else {
                    // this.context.delete(ref)
                    xref.addDeletedEntry(ref, firstInt)
                }

                objectNumber += 1
            }
            // subsection indicator in format <first object's object number (= firstInt)> <count objects with consecutive numbers in this section (= secondInt)>
            else {
                objectNumber = firstInt
            }

            skipWhitespaceAndComments()
        }

        context.crossReferenceSection = xref

        return xref
    }

    protected open fun maybeParseTrailerDict() {
        skipWhitespaceAndComments()
        if (matchKeyword(Keywords.Trailer) == false) {
            return
        }
        skipWhitespaceAndComments()

        // only 'classic' PDFs - that is pre PDF 1.5 PDFs without cross-reference streams - are required to have a Trailer dictionary
        val dict = parseDict()
        mapTrailerInfo(dict)
    }

    protected open fun mapTrailerInfo(dict: PdfDict) {
        context.trailerInfo = TrailerInfo(
            size = dict.getAs<PdfNumber>(PdfName.Size)?.value?.toInt() ?: 0,
            root = dict.get(PdfName.Root) ?: context.trailerInfo?.root,
            encrypt = dict.get(PdfName.Encrypt) ?: context.trailerInfo?.encrypt,
            info = dict.get(PdfName.Info) ?: context.trailerInfo?.info,
            id = dict.getAs(PdfName.ID) ?: context.trailerInfo?.id,
            previousCrossReferenceSectionByteOffset = dict.getAs<PdfNumber>(PdfName.Prev)?.value?.toInt(),
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
        if (bytes.hasNext()) { // don't know why pdf-lib tries to find "%%EOF" two times, usually there's only one "%%EOF" at end of a PDF file
            matchKeyword(Keywords.EOF)
        }

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