package net.codinux.pdf.core.document

import net.codinux.log.logger
import net.codinux.pdf.api.EmbeddedFile
import net.codinux.pdf.core.objects.*
import net.codinux.pdf.core.parser.ByteStream
import net.codinux.pdf.core.parser.PdfObjectParser
import net.codinux.pdf.core.streams.StreamDecoder

open class ReferenceResolver(
    protected val objectParser: PdfObjectParser,
    structure: PdfStructure,
    undeletedCrossReferenceEntries: List<PdfCrossRefEntry>
) {

    val referencesToByteOffset: Map<PdfRef, Int> = undeletedCrossReferenceEntries.associate { it.ref to it.offset }

    protected val compressedReferences: List<PdfRef> = undeletedCrossReferenceEntries.filter { it.isCompressed == true }.map { it.ref }

    protected val indirectObjects: MutableMap<PdfRef, PdfObject> = structure.getIndirectObjects()

    protected val log by logger()


    open fun lookupDict(obj: PdfObject?): PdfDict? = when (obj) {
        is PdfDict -> obj
        is PdfRef -> lookupDict(obj)
        else -> null
    }

    open fun lookupDict(ref: PdfRef): PdfDict? = lookup(ref) as? PdfDict

    open fun lookupArray(obj: PdfObject?): PdfArray? = when (obj) {
        is PdfArray -> obj
        is PdfRef -> lookupArray(obj)
        else -> null
    }

    open fun lookupArray(ref: PdfRef): PdfArray? = lookup(ref) as? PdfArray

    open fun lookupStream(obj: PdfObject?): PdfRawStream? = when (obj) {
        is PdfRawStream -> obj
        is PdfRef -> lookupStream(obj)
        else -> null
    }

    open fun lookupStream(ref: PdfRef): PdfRawStream? = lookup(ref) as? PdfRawStream

    open fun lookup(ref: PdfRef): PdfObject? {
        val resolvedObject = indirectObjects[ref]
        if (resolvedObject != null) {
            return resolvedObject
        }

        val byteOffset = referencesToByteOffset[ref]
        if (byteOffset == null) {
            return null
        }

        val isCompressedObject = compressedReferences.contains(ref) // TODO: handle compressed objects

        val parsedObject = objectParser.parseObjectAtOffset(byteOffset)
        indirectObjects[ref] = parsedObject
        return parsedObject
    }

}