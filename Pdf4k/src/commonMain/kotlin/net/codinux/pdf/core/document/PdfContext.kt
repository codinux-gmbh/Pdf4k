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

    protected var largestObjectNumber = 0

    fun assign(ref: PdfRef, pdfObject: PdfObject) {
        indirectObjects[ref] = pdfObject

        if (ref.objectNumber > this.largestObjectNumber) {
            this.largestObjectNumber= ref.objectNumber
        }
    }
}