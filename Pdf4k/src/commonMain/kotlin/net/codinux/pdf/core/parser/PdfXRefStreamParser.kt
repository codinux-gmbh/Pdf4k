package net.codinux.pdf.core.parser

import net.codinux.pdf.core.ReparseError
import net.codinux.pdf.core.document.TrailerInfo
import net.codinux.pdf.core.objects.PdfCrossRefEntry
import net.codinux.pdf.core.objects.PdfName
import net.codinux.pdf.core.objects.PdfRawStream
import net.codinux.pdf.core.streams.StreamDecoder

open class PdfXRefStreamParser(protected val rawStream: PdfRawStream, protected val decoder: StreamDecoder = StreamDecoder.Instance) {

    protected var alreadyParsed = false

    protected var dict = rawStream.dict

    protected val bytes = ByteStream(rawStream.contents)


    open fun parseTrailerInfoAndXrefStream(): Pair<TrailerInfo, List<PdfCrossRefEntry>> {
        if (alreadyParsed) {
            throw ReparseError("PDFXRefStreamParser", "parseTrailerInfoAndXrefStream")
        }
        alreadyParsed = true

        val trailerInfo = TrailerInfo(
            root = dict.get(PdfName.Root),
            encrypt = dict.get(PdfName.Encrypt),
            info = dict.get(PdfName.Info),
            id = dict.get(PdfName.ID),
        )

        val entries = parseEntries()

        return Pair(trailerInfo, entries)
    }

    protected open fun parseEntries(): List<PdfCrossRefEntry> {
        return emptyList() // TODO
    }

}