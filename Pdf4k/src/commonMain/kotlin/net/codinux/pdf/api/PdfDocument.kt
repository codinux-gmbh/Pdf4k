package net.codinux.pdf.api

import net.codinux.pdf.core.document.PdfStructure
import net.codinux.pdf.core.objects.PdfObject
import net.codinux.pdf.core.objects.PdfRef

open class PdfDocument(structure: PdfStructure) {

    val pdfVersion: Float

    val catalog: PdfObject // TODO: map to PdfCatalog

    val documentInfo: PdfObject? // TODO: map object

    val isEncrypted = structure.trailerInfo?.encrypt != null

    val lowLevelDetails: PdfLowLevelDetails

    val referencesToByteOffset: Map<PdfRef, Int>

    val compressedReferences: List<PdfRef>

    protected val indirectObjects: MutableMap<PdfRef, PdfObject> = structure.getIndirectObjects()


    init {
        val header = structure.header
        require(header != null) { "A PDF file must start with header '%PDF-' followed by PDF version" }
        pdfVersion = "${header.major}.${header.minor}".toFloat()

        val trailerInfo = structure.trailerInfo
        val root = trailerInfo?.root
        require(trailerInfo != null && root != null) { "A PDF file must contain a Trailer dictionary (either in 'trailer' section or in Cross-Reference Stream) that contains a /Root entry"}
        this.catalog = root
        this.documentInfo = trailerInfo.info

        val xrefSection = structure.crossReferenceSection
        require(xrefSection != null) { "A PDF file must contain a cross-reference section, either as XRef stream or table" }
        val undeletedXRefEntries = xrefSection.getSections().flatten().filterNot { it.deleted }
        referencesToByteOffset = undeletedXRefEntries.associate { it.ref to it.offset }
        compressedReferences = undeletedXRefEntries.filter { it.isCompressed == true }.map { it.ref }

        lowLevelDetails = PdfLowLevelDetails(structure, undeletedXRefEntries)
    }

}