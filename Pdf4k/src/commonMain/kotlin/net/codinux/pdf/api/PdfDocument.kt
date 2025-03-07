package net.codinux.pdf.api

import net.codinux.pdf.core.document.PdfStructure
import net.codinux.pdf.core.document.ReferenceResolver
import net.codinux.pdf.core.objects.PdfObject
import net.codinux.pdf.core.objects.PdfRef
import net.codinux.pdf.core.parser.PdfObjectParser
import net.codinux.pdf.core.structures.PdfCatalog

open class PdfDocument(structure: PdfStructure, objectParser: PdfObjectParser) {

    val pdfVersion: Float

    val catalog: PdfCatalog

    val documentInfo: PdfObject? // TODO: map object

    val isEncrypted = structure.trailerInfo?.encrypt != null

    val lowLevelDetails: PdfLowLevelDetails


    protected val referenceResolver: ReferenceResolver

    val referencesToByteOffset: Map<PdfRef, Int>
        get() = referenceResolver.referencesToByteOffset


    init {
        val header = structure.header
        require(header != null) { "A PDF file must start with header '%PDF-' followed by PDF version" }
        pdfVersion = "${header.major}.${header.minor}".toFloat()

        val trailerInfo = structure.trailerInfo
        val root = trailerInfo?.root
        require(trailerInfo != null && root != null) { "A PDF file must contain a Trailer dictionary (either in 'trailer' section or in Cross-Reference Stream) that contains a /Root entry"}

        val xrefSection = structure.crossReferenceSection
        require(xrefSection != null) { "A PDF file must contain a cross-reference section, either as XRef stream or table" }
        val undeletedXRefEntries = xrefSection.getSections().flatten().filterNot { it.deleted }
        referenceResolver = ReferenceResolver(objectParser, structure, undeletedXRefEntries)

        this.catalog = referenceResolver.lookupDict(root) as PdfCatalog
        this.documentInfo = trailerInfo.info

        lowLevelDetails = PdfLowLevelDetails(structure, undeletedXRefEntries)
    }


    open fun lookupDict(obj: PdfObject) = referenceResolver.lookupDict(obj)

    open fun lookupDict(ref: PdfRef) = referenceResolver.lookupDict(ref)

}