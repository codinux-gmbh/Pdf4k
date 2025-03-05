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
        val numberOfObjectsInStream = dict.getAs<PdfArray>("N")?.items

        val subsections = if (numberOfObjectsInStream == null) {
            val size = dict.getAs<PdfNumber>(PdfName.Size)?.value?.toInt() // TODO: what if dict does not contain a Size entry?
            listOf(PdfCrossReferenceSubsection(0, size ?: 0))
        } else {
            (0..<numberOfObjectsInStream.size step 2).mapNotNull { index ->
                val firstObjectNumber = (numberOfObjectsInStream[index] as? PdfNumber)?.value?.toInt() // TODO: what if object is not a PdfNumber?
                val length = (numberOfObjectsInStream[index + 1] as? PdfNumber)?.value?.toInt()
                if (firstObjectNumber != null && length != null) {
                    PdfCrossReferenceSubsection(firstObjectNumber, length)
                } else {
                    log.warn { "Expecting Cross Reference Array to contain PdfNumbers at indices $index and ${index + 1} but were ${numberOfObjectsInStream[index]} and ${numberOfObjectsInStream[index + 1]}" }
                    null
                }
            }
        }

        val byteWidths = mutableListOf(-1, -1, -1)
        dict.getAs<PdfArray>("W")?.let { widths -> // W is required per spec, but to be on the safe side
            for (index in 0..<widths.size) {
                widths.lookup<PdfNumber>(index)?.value?.toInt()?.let {
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

            (0..<length).map { objectIndex ->
                var type = getValueOfByteWidth(bytes, typeFieldWidth)
                val offset = getValueOfByteWidth(bytes, offsetFieldWidth)
                val generationNumber = getValueOfByteWidth(bytes, genFieldWidth)

                // When the `type` field is absent, it defaults to 1
                if (typeFieldWidth == 0) {
                    type = 1
                }

                val objectNumber = firstObjectNumber + objectIndex
                PdfCrossRefEntry(PdfRef.getOrCreate(referencePool, objectNumber, generationNumber), offset, type == 0, type == 2)
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