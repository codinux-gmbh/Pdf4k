package net.codinux.pdf.core.document

import net.codinux.pdf.core.objects.*
import net.codinux.pdf.core.parser.PdfObjectParser

open class ReferenceResolver(
    protected val objectParser: PdfObjectParser,
    structure: PdfStructure,
    undeletedCrossReferenceEntries: List<PdfCrossRefEntry>
) {

    val referencesToByteOffset: Map<PdfRef, Int> = undeletedCrossReferenceEntries.associate { it.ref to it.offset }

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

    open fun lookupObjectStream(ref: PdfRef): PdfObjectStream? = lookup(ref) as? PdfObjectStream

    open fun lookup(ref: PdfRef): PdfObject? {
        val resolvedObject = indirectObjects[ref]
        if (resolvedObject != null) {
            return resolvedObject
        }

        val byteOffset = referencesToByteOffset[ref]

        return if (byteOffset != null) {
            lookupObjectAtByteOffset(ref, byteOffset)
        } else {
            null
        }
    }

    protected open fun lookupObjectAtByteOffset(ref: PdfRef, byteOffset: Int): PdfObject? {
        val refWithMatchingObjectNumber = referencesToByteOffset.entries.first { it.key.objectNumber == ref.objectNumber }.key

        return if (refWithMatchingObjectNumber is PdfRefInStream) { // an object in an object stream, mostly compressed with many other objects
            val containedInObjectNumber = refWithMatchingObjectNumber.objectNumberOfContainingStream
            // The generation number of an object stream and of any compressed object shall be zero.
            val containingObject = lookupObjectStream(PdfRef(containedInObjectNumber, 0))

            if (containingObject != null) {
                containingObject.containingObjects[ref]
            } else {
                null
            }
        } else { // un uncompressed object
            val parsedObject = objectParser.parseObjectAtOffset(byteOffset)
            indirectObjects[ref] = parsedObject
            if (parsedObject is PdfObjectStream) {
                indirectObjects.putAll(parsedObject.containingObjects)
            }

            parsedObject
        }
    }

}