package net.codinux.pdf.core.document

import net.codinux.pdf.core.objects.PdfCrossRefEntry
import net.codinux.pdf.core.objects.PdfDict
import net.codinux.pdf.core.objects.PdfObject
import net.codinux.pdf.core.objects.PdfRef
import net.codinux.pdf.core.parser.PdfObjectParser

open class ReferenceResolver(protected val objectParser: PdfObjectParser, structure: PdfStructure, undeletedCrossReferenceEntries: List<PdfCrossRefEntry>) {

    val referencesToByteOffset: Map<PdfRef, Int> = undeletedCrossReferenceEntries.associate { it.ref to it.offset }

    protected val compressedReferences: List<PdfRef> = undeletedCrossReferenceEntries.filter { it.isCompressed == true }.map { it.ref }

    protected val indirectObjects: MutableMap<PdfRef, PdfObject> = structure.getIndirectObjects()


    open fun lookupDict(obj: PdfObject): PdfDict? = when (obj) {
        is PdfDict -> obj
        is PdfRef -> lookupDict(obj)
        else -> null
    }

    open fun lookupDict(ref: PdfRef): PdfDict? = lookup(ref) as? PdfDict

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