package net.codinux.pdf.core.parser

import net.codinux.log.logger
import net.codinux.pdf.core.document.TrailerInfo
import net.codinux.pdf.core.objects.*
import net.codinux.pdf.core.streams.StreamDecoder

open class PdfXRefStreamParser(protected val rawStream: PdfRawStream, protected val decoder: StreamDecoder = StreamDecoder.Instance) {

    protected var dict = rawStream.dict

    protected val log by logger()


    open fun parseTrailerInfoAndXrefStream(referencePool: MutableMap<String, PdfRef>): Pair<TrailerInfo, PdfCrossRefSection> {
        val trailerInfo = TrailerInfo(
            root = dict.get(PdfName.Root),
            encrypt = dict.get(PdfName.Encrypt),
            info = dict.get(PdfName.Info),
            id = dict.get(PdfName.ID),
        )

        return Pair(trailerInfo, parseXrefStream(referencePool))
    }

    protected open fun parseXrefStream(referencePool: MutableMap<String, PdfRef>): PdfCrossRefSection {
        val indices = dict.get(PdfName.Index)

        val subsections = if (indices !is PdfArray) {
            // The number one greater than the highest object number used in this section, or in any section for which
            // this is an update. It is equivalent to the Size entry in a trailer dictionary.
            val size = dict.getAs<PdfNumber>(PdfName.Size)?.value?.toInt() // TODO: what if dict does not contain required Size entry?
            listOf(PdfCrossReferenceSubsection(0, size ?: 0))
        } else {
            if (indices.size == 0 || indices.size % 2 != 0) {
                throw IllegalStateException("XRef /Index array may not be empty and must have an even number of entries")
            }
            (0..<indices.size step 2).mapNotNull { index ->
                val firstObjectNumber = indices.getAs<PdfNumber>(index)?.value?.toInt() // TODO: what if object is not a PdfNumber?
                val length = indices.getAs<PdfNumber>(index + 1)?.value?.toInt()
                if (firstObjectNumber != null && length != null) {
                    PdfCrossReferenceSubsection(firstObjectNumber, length)
                } else {
                    log.warn { "Expecting Cross Reference Array to contain PdfNumbers at indices $index and ${index + 1} but were ${indices[index]} and ${indices[index + 1]}" }
                    null
                }
            }
        }

        val byteWidths = mutableListOf(-1, -1, -1)
        dict.getAs<PdfArray>("W")?.let { widths -> // W is required per spec, but to be on the safe side
            for (index in 0..<widths.size) {
                widths.getAs<PdfNumber>(index)?.value?.toInt()?.let {
                    byteWidths[index] = it
                }
            }
        }

        val entries = parseEntries(subsections, byteWidths, referencePool)

        return PdfCrossRefSection(entries.toMutableList())
    }

    protected open fun parseEntries(subsections: List<PdfCrossReferenceSubsection>, byteWidths: MutableList<Int>, referencePool: MutableMap<String, PdfRef>): List<MutableList<PdfCrossRefEntry>> {
        val bytes = ByteStream.fromPdfRawStream(rawStream, decoder)
        val (typeFieldWidth, offsetFieldWidth, genFieldWidth) = byteWidths

        return subsections.indices.map { index ->
            val (firstObjectNumber, length) = subsections[index]

            (0..<length).mapNotNull { objectIndex ->
                val type = if (typeFieldWidth == 0) {
                    1 // When the `type` field width is absent, it defaults to 1
                } else {
                    getValueOfByteWidth(bytes, typeFieldWidth)
                }

                val offset = getValueOfByteWidth(bytes, offsetFieldWidth)
                val generationNumber = getValueOfByteWidth(bytes, genFieldWidth)
                val isDeleted = type == 0

                if (isDeleted) {
                    null // skip deleted entries
                } else {
                    val objectNumber = firstObjectNumber + objectIndex
                    PdfCrossRefEntry(PdfRef.getOrCreate(referencePool, objectNumber, generationNumber), offset, isDeleted, type == 2)
                }

            }.toMutableList()
        }
    }

    protected open fun getValueOfByteWidth(bytes: ByteStream, countBytes: Int): Int {
        var value = 0
        (0..<countBytes).forEach {
            value = (value shl 8) or bytes.next().toInt()
        }

        return value
    }

}