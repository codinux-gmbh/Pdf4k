package net.codinux.pdf.core.document

import net.codinux.pdf.core.objects.PdfCrossRefSection
import net.codinux.pdf.core.objects.PdfObject
import net.codinux.pdf.core.objects.PdfRef

open class PdfStructure(
    var header: PdfHeader? = null,
    var crossReferenceSection: PdfCrossRefSection? = null,
    var trailerInfo: TrailerInfo? = null,

    /**
     * The byte index in PDF file where the Cross Reference Section (XRef table or stream) starts.
     */
    var xrefByteIndex: Int? = null,
    /**
     * If PDF file uses modern stream (`true`) or classical table (`false`) for Cross Reference Section.
     */
    var usesCrossReferenceStream: Boolean? = null,
) {

    protected val indirectObjects = mutableMapOf<PdfRef, PdfObject>()

    open var largestObjectNumber = 0
        protected set


    open fun addIndirectObject(ref: PdfRef, pdfObject: PdfObject) {
        indirectObjects[ref] = pdfObject

        if (ref.objectNumber > this.largestObjectNumber) {
            this.largestObjectNumber= ref.objectNumber
        }
    }

    open fun addIndirectObjects(indirectObjects: List<Pair<PdfRef, PdfObject>>) {
        indirectObjects.forEach { (ref, pdfObject) ->
            addIndirectObject(ref, pdfObject)
        }
    }

}