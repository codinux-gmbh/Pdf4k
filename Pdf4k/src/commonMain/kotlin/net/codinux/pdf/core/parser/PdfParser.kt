package net.codinux.pdf.core.parser

import net.codinux.pdf.api.PdfDocument
import net.codinux.pdf.core.*
import net.codinux.pdf.core.document.PdfStructure
import net.codinux.pdf.core.document.PdfHeader
import net.codinux.pdf.core.document.PdfTrailer
import net.codinux.pdf.core.document.TrailerInfo
import net.codinux.pdf.core.extensions.lastIndexOf
import net.codinux.pdf.core.mapper.TextDecoder
import net.codinux.pdf.core.objects.*
import net.codinux.pdf.core.streams.StreamDecoder
import net.codinux.pdf.core.syntax.CharCodes
import net.codinux.pdf.core.syntax.Keywords

@OptIn(ExperimentalUnsignedTypes::class)
open class PdfParser(
    pdfBytes: UByteArray,
    protected val throwOnInvalidObject: Boolean = false,
    capNumbers: Boolean = false,
    protected val streamDecoder: StreamDecoder = StreamDecoder.Instance,
    textDecoder: TextDecoder = TextDecoder.Instance
) : PdfObjectParser(ByteStream(pdfBytes), capNumbers, textDecoder) {

    /**
     * Parses a PDF file byte by byte, therefore also objects that may are not needed for your requirements. Can be
     * quite time-consuming for large PDFs.
     */
    open fun parseDocument(): PdfDocument {
        val context = PdfStructure()
        context.header = parseHeader()

        var previousOffset: Int = -1
        while (bytes.hasNext()) {
            parseDocumentSection(context)

            val offset = bytes.offset()
            if (offset == previousOffset) {
                throw StalledParserError(bytes.position())
            }
            previousOffset = offset
        }

        return PdfDocument(context, this)
    }

    /**
     * Tries to parse only the most elementary bytes of a PDF.
     */
    open fun parseDocumentEfficiently(): PdfDocument {
        val context = PdfStructure()
        context.header = parseHeader()

        /**
         * The trailer of a PDF file enables a PDF processor to quickly find the cross-reference table and certain
         * special objects. PDF processors should read a PDF file from its end. The last line of the file shall contain
         * only the end-of-file marker, %%EOF. The two preceding lines shall contain, one per line and in order,
         * the keyword **startxref** and the byte offset in the decoded stream from the beginning of the PDF file to
         * the beginning of the **xref** keyword in the last cross-reference section. The **startxref** line shall be
         * preceded by the trailer dictionary, consisting of the keyword **trailer**.
         */

        val startXRefKeywordIndex = bytes.getBytes().lastIndexOf(Keywords.StartXref)
        if (startXRefKeywordIndex != null) { // TODO: otherwise throw an exception
            bytes.moveTo(startXRefKeywordIndex + Keywords.StartXref.size)
            skipWhitespaceAndComments()

            val xrefByteOffset = parseRawInt()
            bytes.moveTo(xrefByteOffset)
            context.xrefByteIndex = xrefByteOffset

            if (matchKeyword(Keywords.Xref)) { // Cross Ref Table
                bytes.moveTo(bytes.offset() - Keywords.Xref.size) // we matched "xref", so set bytes position back to "xref" start so that it can parse Cross Ref Section
                maybeParseCrossRefSection(context)

                val trailerDictStartIndex = bytes.getBytes().lastIndexOf(Keywords.Trailer)
                if (trailerDictStartIndex != null) {
                    bytes.moveTo(trailerDictStartIndex)
                    maybeParseTrailerDict(context)
                }
            } else { // Cross Ref Stream
                parseIndirectObject(context) // parses cross-ref stream and its containing Trailer dict
            }
        }

        return PdfDocument(context, this)
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


    protected open fun parseIndirectObject(context: PdfStructure): PdfRef {
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
            context.trailerInfo = mapTrailerInfo(xrefStream.dict, context)

            val xrefStreamParser = PdfXRefStreamParser(xrefStream, streamDecoder)
            context.crossReferenceSection = xrefStreamParser.parseXrefStream(referencePool)
        } else {
            context.addIndirectObject(ref, pdfObject)
        }

        return ref
    }

    protected open fun parseIndirectObjects(context: PdfStructure) {
        skipWhitespaceAndComments()

        while (bytes.hasNext() && isDigit(bytes.peek())) {
            val initialOffset = bytes.offset()

            try {
                parseIndirectObject(context)
            } catch (e: Throwable) {
                log.error(e) { "Could not parse indirect object" }
                bytes.moveTo(initialOffset)
                tryToParseInvalidIndirectObject(context)
            }

            skipWhitespaceAndComments()

            // TODO: Can this be done only when needed, to avoid harming performance?
            skipJibberish()
        }
    }

    // TODO: Improve and clean this up
    protected open fun tryToParseInvalidIndirectObject(context: PdfStructure): PdfRef {
        val startPos = bytes.position()

        val message = "Trying to parse invalid object: $startPos"
        if (throwOnInvalidObject) {
            throw IllegalStateException(message)
        }
        log.warn { message }

        val ref = parseIndirectObjectHeader()

        log.warn { "Invalid object ref: $ref" }

        skipWhitespaceAndComments()
        val start = bytes.offset()

        var failed = true
        while (bytes.hasNext()) {
            if (matchKeyword(Keywords.Endobj)) {
                failed = false
                break
            }

            bytes.next()
        }

        if (failed) {
            throw PdfInvalidObjectParsingError(startPos)
        }

        val end = bytes.offset() - Keywords.Endobj.size

        val `object` = PdfInvalidObject(bytes.slice(start, end))
        context.addIndirectObject(ref, `object`)

        return ref
    }

    protected open fun maybeParseCrossRefSection(context: PdfStructure): PdfCrossRefSection? {
        skipWhitespaceAndComments()
        if (matchKeyword(Keywords.Xref) == false) {
            return null
        }
        context.xrefByteIndex = bytes.offset() - Keywords.Xref.size
        skipWhitespaceAndComments()

        var objectNumber = -1
        val xref = PdfCrossRefSection(false)

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

    protected open fun maybeParseTrailerDict(context: PdfStructure): TrailerInfo? {
        skipWhitespaceAndComments()
        if (matchKeyword(Keywords.Trailer) == false) {
            return null
        }
        skipWhitespaceAndComments()

        // only 'classic' PDFs - that is pre PDF 1.5 PDFs without cross-reference streams - are required to have a Trailer dictionary
        val dict = parseDict()
        context.trailerInfo = mapTrailerInfo(dict, context)

        return context.trailerInfo
    }

    protected open fun mapTrailerInfo(dict: PdfDict, context: PdfStructure) = TrailerInfo(
        size = dict.getAs<PdfNumber>(PdfName.Size)?.value?.toInt() ?: 0, // /Size is required to be a direct object
        root = dict.get(PdfName.Root) ?: context.trailerInfo?.root, // /Root is required to be an indirect object
        encrypt = dict.get(PdfName.Encrypt) ?: context.trailerInfo?.encrypt, // can be direct or indirect
        info = dict.get(PdfName.Info) ?: context.trailerInfo?.info, // /Info was required to be an indirect object but isn't anymore (actually it's deprecated in PDF 2.0)
        id = dict.getAs(PdfName.ID) ?: context.trailerInfo?.id, // can be direct or indirect
        previousCrossReferenceSectionByteOffset = dict.getAs<PdfNumber>(PdfName.Prev)?.value?.toInt(), // shall be a direct object
    )

    protected open fun maybeParseTrailer(context: PdfStructure): PdfTrailer? {
        skipWhitespaceAndComments()
        if (matchKeyword(Keywords.StartXref) == false) {
            return null
        }
        skipWhitespaceAndComments()

        val offset = parseRawInt()
        context.xrefByteIndex = offset

        skipWhitespace()
        matchKeyword(Keywords.EOF)
        skipWhitespaceAndComments()
        if (bytes.hasNext()) { // don't know why pdf-lib tries to find "%%EOF" two times, usually there's only one "%%EOF" at end of a PDF file
            matchKeyword(Keywords.EOF)
        }

        return PdfTrailer(offset)
    }

    protected open fun parseDocumentSection(context: PdfStructure) {
        parseIndirectObjects(context)
        maybeParseCrossRefSection(context)
        maybeParseTrailerDict(context)
        maybeParseTrailer(context)

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