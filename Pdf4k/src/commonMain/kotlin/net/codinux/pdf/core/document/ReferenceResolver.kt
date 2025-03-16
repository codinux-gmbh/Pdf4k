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


    // i left the lookupDict() as it's the most common data type
    open fun lookupDict(obj: PdfObject?): PdfDict? = when (obj) {
        is PdfRef -> lookup(obj) as? PdfDict
        else -> obj as? PdfDict
    }

    @Suppress("UNCHECKED_CAST")
    open fun <T> lookup(obj: PdfObject?): T? = when (obj) {
        is PdfRef -> lookup(obj) as? T
        else -> obj as? T
    }

    @Suppress("UNCHECKED_CAST")
    open fun <T : PdfObject> lookup(ref: PdfRef): T? {
        val resolvedObject = indirectObjects[ref]
        if (resolvedObject != null) {
            return resolvedObject as? T
        }

        val byteOffset = referencesToByteOffset[ref]

        return if (byteOffset != null) {
            lookupObjectAtByteOffset(ref, byteOffset) as? T
        } else {
            null
        }
    }

    protected open fun lookupObjectAtByteOffset(ref: PdfRef, byteOffset: Int): PdfObject? {
        val refWithMatchingObjectNumber = referencesToByteOffset.entries.first { it.key.objectNumber == ref.objectNumber }.key

        return if (refWithMatchingObjectNumber is PdfRefInStream) { // an object in an object stream, mostly compressed with many other objects
            val containedInObjectNumber = refWithMatchingObjectNumber.objectNumberOfContainingStream
            // The generation number of an object stream and of any compressed object shall be zero.
            val containingObject = lookup<PdfObjectStream>(PdfRef(containedInObjectNumber, 0))

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