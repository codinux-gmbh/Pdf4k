package net.codinux.pdf.core.document

import net.codinux.pdf.core.objects.*
import net.codinux.pdf.core.parser.PdfObjectParser

open class ReferenceResolver(
    protected val objectParser: PdfObjectParser,
    structure: PdfStructure,
    undeletedCrossReferenceEntries: List<PdfCrossRefEntry>
) {

    val referencesToByteOffset: Map<PdfRef, Int> = undeletedCrossReferenceEntries.associate { it.ref to it.offset }

    protected val compressedReferences: List<PdfRef> = undeletedCrossReferenceEntries.filter { it.isCompressed == true }.map { it.ref }

    protected val indirectObjects: MutableMap<PdfRef, PdfObject> = structure.getIndirectObjects()


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

    open fun lookupNumber(obj: PdfObject?): PdfNumber? = when (obj) {
        is PdfNumber -> obj
        is PdfRef -> lookupNumber(obj)
        else -> null
    }

    open fun lookupNumber(ref: PdfRef): PdfNumber? = lookup(ref) as? PdfNumber

    open fun lookupString(obj: PdfObject?): PdfString? = when (obj) {
        is PdfString -> obj
        is PdfRef -> lookupString(obj)
        else -> null
    }

    open fun lookupString(ref: PdfRef): PdfString? = lookup(ref) as? PdfString

    open fun lookupHexString(obj: PdfObject?): PdfHexString? = when (obj) {
        is PdfHexString -> obj
        is PdfRef -> lookupHexString(obj)
        else -> null
    }

    open fun lookupHexString(ref: PdfRef): PdfHexString? = lookup(ref) as? PdfHexString

    open fun lookupName(obj: PdfObject?): PdfName? = when (obj) {
        is PdfName -> obj
        is PdfRef -> lookupName(obj)
        else -> null
    }

    open fun lookupName(ref: PdfRef): PdfName? = lookup(ref) as? PdfName

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