package net.codinux.pdf.core.parser

import net.codinux.log.logger
import net.codinux.pdf.core.objects.*
import net.codinux.pdf.core.streams.StreamDecoder

open class PdfXRefStreamParser(protected val rawStream: PdfRawStream, protected val decoder: StreamDecoder = StreamDecoder.Instance) {

    protected var dict = rawStream.dict

    protected val log by logger()


    open fun parseXrefStream(referencePool: MutableMap<String, PdfRef>): PdfCrossRefSection {
        val indices = dict.get(PdfName.Index) // /Index and its items shall be a direct object (same with /W)

        val subsections = if (indices !is PdfArray) {
            // The number one greater than the highest object number used in this section, or in any section for which
            // this is an update. It is equivalent to the Size entry in a trailer dictionary.
            val size = dict.getAs<PdfNumber>(PdfName.Size)?.value?.toInt() // TODO: what if dict does not contain required Size entry or if it's an indirect object?
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

        val byteWidths = mutableListOf(0, 0, 0) // in case W is not set; use the same default value as in getValueOfByteWidth() then
        dict.getAs<PdfArray>("W")?.let { widths -> // W is required per spec, but to be on the safe side
            for (index in 0..<widths.size) {
                widths.getAs<PdfNumber>(index)?.value?.toInt()?.let {
                    byteWidths[index] = it
                }
            }
        }

        val entries = parseEntries(subsections, byteWidths, referencePool)

        return PdfCrossRefSection(true, entries.toMutableList())
    }

    protected open fun parseEntries(subsections: List<PdfCrossReferenceSubsection>, byteWidths: MutableList<Int>, referencePool: MutableMap<String, PdfRef>): List<MutableList<PdfCrossRefEntry>> {
        val bytes = ByteStream.fromPdfRawStream(rawStream, decoder)
        val (typeFieldWidth, offsetFieldWidth, genFieldWidth) = byteWidths

        return subsections.indices.map { index ->
            val (firstObjectNumber, length) = subsections[index]

            (0..<length).mapNotNull { objectIndex ->
                // see Table 18 (in 7.5.8.3 Cross-reference stream data) on p. 67-8 of PDF 1.7 reference
                val type = getValueOfByteWidth(bytes, typeFieldWidth, 1) // When the `type` field width is absent, it defaults to 1
                val offset = getValueOfByteWidth(bytes, offsetFieldWidth, 0) // for type == 0 actually "The object number of the next free object" and for type == 2 "The object number of the object stream in which this object is stored"
                val generationNumber = getValueOfByteWidth(bytes, genFieldWidth, 0) // for type == 2: "The index of this object within the object stream. This index value will be between zero and the value of N minus 1 from the associated object stream dictionary". The generation number of the object stream shall be implicitly 0

                val isDeleted = type == 0
                val isCompressed = type == 2
                val objectNumber = firstObjectNumber + objectIndex

                if (isDeleted) {
                    null // skip deleted entries
                } else if (isCompressed) {
                    PdfCrossRefEntry(PdfRefInStream(objectNumber, offset, generationNumber), offset, isDeleted, isCompressed)
                } else {
                    PdfCrossRefEntry(PdfRef.getOrCreate(referencePool, objectNumber, generationNumber), offset, isDeleted, isCompressed)
                }

            }.toMutableList()
        }
    }

    protected open fun getValueOfByteWidth(bytes: ByteStream, countBytes: Int, defaultValue: Int): Int =
        if (countBytes == 0) {
            defaultValue
        } else {
        var value = 0
        (0..<countBytes).forEach {
            value = (value shl 8) or bytes.next().toInt()
        }

        value
    }

}