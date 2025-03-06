package net.codinux.pdf.core.document

import net.codinux.pdf.core.objects.PdfCrossRefSection
import net.codinux.pdf.core.objects.PdfObject
import net.codinux.pdf.core.objects.PdfRef

open class PdfContext(
    var header: PdfHeader? = null,
    var crossReferenceSection: PdfCrossRefSection? = null,
    var trailerInfo: TrailerInfo? = null,
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