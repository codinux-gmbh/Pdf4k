package net.codinux.pdf.api

import net.codinux.pdf.core.document.PdfStructure
import net.codinux.pdf.core.objects.PdfCrossRefEntry
import net.codinux.pdf.core.objects.PdfHexString
import net.codinux.pdf.core.objects.PdfString

open class PdfLowLevelDetails(
    structure: PdfStructure,
    undeletedObjectReferences: List<PdfCrossRefEntry>,

    /**
     * The PDF version as specified by PDF header, which for incremental PDFs may is
     * overwritten by PDF version specified in PDF catalog, see [catalogPdfVersion].
     *
     * For the resulting PDF version see [PdfDocument.pdfVersion].
     */
    val headerPdfVersion: Float,
    /**
     * For incremental PDFs in PDF catalog may a different (newer) version is specified
     * as in PDF header. This value reflects the PDF version specified in PDF catalog (if set).
     *
     * For the resulting PDF version see [PdfDocument.pdfVersion].
     */
    val catalogPdfVersion: Float?
) {

    val xrefByteIndex = structure.xrefByteIndex!!

    val usesCrossReferenceStream = structure.crossReferenceSection!!.isCrossReferenceStream

    val largestObjectNumber = structure.largestObjectNumber

    val countUndeletedObjects = undeletedObjectReferences.size

    val creationHash = structure.trailerInfo?.id?.get(0)?.let { when (it) {
        is PdfHexString -> it.asHex
        is PdfString -> it.value
        else -> null
    } }

    val lastModifiedHash = structure.trailerInfo?.id?.get(1)?.let { when (it) {
        is PdfHexString -> it.asHex
        is PdfString -> it.value
        else -> null
    } }

}