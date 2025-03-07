package net.codinux.pdf.api

import net.codinux.pdf.core.document.PdfStructure
import net.codinux.pdf.core.objects.PdfCrossRefEntry
import net.codinux.pdf.core.objects.PdfHexString
import net.codinux.pdf.core.objects.PdfString

open class PdfLowLevelDetails(structure: PdfStructure, undeletedObjectReferences: List<PdfCrossRefEntry>) {

    val xrefByteIndex = structure.xrefByteIndex!!

    val usesCrossReferenceStream = structure.crossReferenceSection!!.isCrossReferenceStream

    val largestObjectNumber = structure.largestObjectNumber

    val countUndeletedObjects = undeletedObjectReferences.size

    val creationHash = structure.trailerInfo?.id?.get(0)?.let { when (it) {
        is PdfHexString -> it.value
        is PdfString -> it.value
        else -> null
    } }

    val lastModifiedHash = structure.trailerInfo?.id?.get(1)?.let { when (it) {
        is PdfHexString -> it.value
        is PdfString -> it.value
        else -> null
    } }

}