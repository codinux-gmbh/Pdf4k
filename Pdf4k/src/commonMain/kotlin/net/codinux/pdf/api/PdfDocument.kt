package net.codinux.pdf.api

import net.codinux.pdf.core.document.PdfStructure
import net.codinux.pdf.core.document.ReferenceResolver
import net.codinux.pdf.core.mapper.PdfDataMapper
import net.codinux.pdf.core.objects.PdfName
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

    protected val dataMapper: PdfDataMapper

    val referencesToByteOffset: Map<PdfRef, Int>
        get() = referenceResolver.referencesToByteOffset

    val embeddedFiles: List<EmbeddedFile> by lazy { extractEmbeddedFiles() }


    init {
        val header = structure.header
        require(header != null) { "A PDF file must start with header '%PDF-' followed by PDF version" }
        val headerPdfVersion = "${header.major}.${header.minor}".toFloat()

        val trailerInfo = structure.trailerInfo
        val root = trailerInfo?.root
        require(trailerInfo != null && root != null) { "A PDF file must contain a Trailer dictionary (either in 'trailer' section or in Cross-Reference Stream) that contains a /Root entry"}

        val xrefSection = structure.crossReferenceSection
        require(xrefSection != null) { "A PDF file must contain a cross-reference section, either as XRef stream or table" }
        val undeletedXRefEntries = xrefSection.getSections().flatten().filterNot { it.deleted }
        referenceResolver = ReferenceResolver(objectParser, structure, undeletedXRefEntries)
        dataMapper = PdfDataMapper(referenceResolver)

        this.catalog = referenceResolver.lookupDict(root) as PdfCatalog
        this.documentInfo = trailerInfo.info ?: catalog.getAs(PdfName.Info) // sometimes the /Info dictionary resides in /Catalog dict

        // (Optional; PDF 1.4) The version of the PDF specification to which the document conforms (for example, 1.4)
        // if later than the version specified in the file’s header (see 7.5.2, "File header"). If the header specifies
        // a later version, or if this entry is absent, the document shall conform to the version specified in the header.
        // This entry enables a PDF processor to update the version using an incremental update; see 7.5.6, "Incremental updates".
        val catalogPdfVersion = catalog.getAs<PdfName>(PdfName.Version)?.name?.toFloat()
        this.pdfVersion = if (catalogPdfVersion != null && catalogPdfVersion > headerPdfVersion) catalogPdfVersion else headerPdfVersion

        lowLevelDetails = PdfLowLevelDetails(structure, undeletedXRefEntries, headerPdfVersion, catalogPdfVersion)
    }


    open fun lookupDict(obj: PdfObject) = referenceResolver.lookupDict(obj)

    open fun lookupDict(ref: PdfRef) = referenceResolver.lookupDict(ref)

    protected open fun extractEmbeddedFiles() = dataMapper.getEmbeddedFiles(catalog)

}