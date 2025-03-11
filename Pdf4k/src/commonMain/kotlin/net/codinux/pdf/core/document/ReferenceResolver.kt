package net.codinux.pdf.core.document

import net.codinux.log.logger
import net.codinux.pdf.api.EmbeddedFile
import net.codinux.pdf.core.objects.*
import net.codinux.pdf.core.parser.ByteStream
import net.codinux.pdf.core.parser.PdfObjectParser
import net.codinux.pdf.core.streams.StreamDecoder

open class ReferenceResolver(
    protected val objectParser: PdfObjectParser,
    structure: PdfStructure,
    undeletedCrossReferenceEntries: List<PdfCrossRefEntry>,
    protected val decoder: StreamDecoder = StreamDecoder.Instance
) {

    val referencesToByteOffset: Map<PdfRef, Int> = undeletedCrossReferenceEntries.associate { it.ref to it.offset }

    protected val compressedReferences: List<PdfRef> = undeletedCrossReferenceEntries.filter { it.isCompressed == true }.map { it.ref }

    protected val indirectObjects: MutableMap<PdfRef, PdfObject> = structure.getIndirectObjects()

    protected val log by logger()


    open fun lookupDict(obj: PdfObject?): PdfDict? = when (obj) {
        is PdfDict -> obj
        is PdfRef -> lookupDict(obj)
        else -> null
    }

    open fun lookupDict(ref: PdfRef): PdfDict? = lookup(ref) as? PdfDict

    open fun lookupArray(obj: PdfObject?): PdfArray? = when (obj) {
        is PdfArray -> obj
        is PdfRef -> lookupArray(obj)
        else -> null
    }

    open fun lookupArray(ref: PdfRef): PdfArray? = lookup(ref) as? PdfArray

    open fun lookupStream(obj: PdfObject?): PdfRawStream? = when (obj) {
        is PdfRawStream -> obj
        is PdfRef -> lookupStream(obj)
        else -> null
    }

    open fun lookupStream(ref: PdfRef): PdfRawStream? = lookup(ref) as? PdfRawStream

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


    open fun getEmbeddedFiles(catalog: PdfDict): List<EmbeddedFile> = try {
        // the default is storing embedded files in the AssociatedFiles array at document level
        val associatedFiles = lookupArray(catalog.get(PdfName.AF))
        if (associatedFiles != null) {
            readEmbeddedFilesArray(associatedFiles)
        } else { // additionally and only sometimes (which is not PDF/A-3 conformant) they are stored in Names -> EmbeddedFiles -> Names dictionary entries
            val embeddedFiles = lookupDict(lookupDict(catalog.get(PdfName.Names))?.get(PdfName.EmbeddedFiles))
            if (embeddedFiles != null) {
                readEmbeddedFilesDict(embeddedFiles)
            } else {
                emptyList()
            }
        }
    } catch (e: Throwable) {
        log.error(e) { "Could not read embedded files" }
        emptyList()
    }

    protected open fun readEmbeddedFilesDict(embeddedFilesDict: PdfDict): List<EmbeddedFile> {
        val embeddedFilesNames = lookupArray(embeddedFilesDict.get(PdfName.Names))

        return if (embeddedFilesNames != null) {
            readEmbeddedFilesArray(embeddedFilesNames)
        } else {
            // don't know how often this is the case, but Factur-X specification says sometimes embedded files are not directly in
            // EmbeddedFiles -> Names dict but indirectly in EmbeddedFiles -> Kids -> Names
            val kids = lookupArray(embeddedFilesDict.get("Kids"))
            kids?.items.orEmpty().filterIsInstance<PdfDict>().flatMap { readEmbeddedFilesDict(it) }
        }
    }

    protected open fun readEmbeddedFilesArray(embeddedFilesNames: PdfArray): List<EmbeddedFile> =
        embeddedFilesNames.items.mapNotNull { lookupDict(it) }.mapNotNull { entry ->
            try {
                mapEmbeddedFile(entry)
            } catch (e: Throwable) {
                log.error(e) { "Could not read embedded file entry $entry" }
                null
            }
        }

    protected open fun mapEmbeddedFile(fileSpecification: PdfDict): EmbeddedFile? {
        val embeddedFile = lookupDict(fileSpecification.get("EF"))
        val embeddedFileStream = lookupStream(embeddedFile?.get("F") ?: embeddedFile?.get("UF"))

        return if (embeddedFile == null || embeddedFileStream == null) {
            null
        } else {
            val filename = decodeText(fileSpecification, "F")
            val unicodeFilename = decodeText(fileSpecification, "UF")
            val description = decodeText(fileSpecification, "Desc")
            val relationship = decodeText(fileSpecification, "AFRelationship")

            val dict = embeddedFileStream.dict
            val params = lookupDict(dict.get("Params"))
            val size = params?.getAs<PdfNumber>(PdfName.Size)
                ?: dict.getAs<PdfNumber>(PdfName.Length)
                ?: dict.getAs<PdfNumber>(PdfName.Size) // 'Length' is correct, some use 'Size'
            val mimeType = decodeText(dict, "Subtype")
            val md5Hash = params?.getAs<PdfString>("CheckSum")?.value
            val creationDate = decodeDate(params, PdfName.CreationDate)
            val modificationDate = decodeDate(params, PdfName.ModDate)

            EmbeddedFile(
                unicodeFilename ?: filename ?: "", // A PDF reader shall use the value of the UF key, when present, instead of the F key.
                readBytesFromStream(embeddedFileStream),
                size?.value?.toInt(),
                description,
                mimeType,
                md5Hash,
                relationship,
                creationDate,
                modificationDate
            )
        }
    }


    protected open fun readBytesFromStream(embeddedFileStream: PdfRawStream): UByteArray {
        val isCompressed = embeddedFileStream.dict.get(PdfName.Filter) != null

        return if (isCompressed == false) {
            embeddedFileStream.contents
        } else {
            ByteStream.fromPdfRawStream(embeddedFileStream, decoder).getBytes()
        }
    }

    protected open fun decodeText(dict: PdfDict, key: String): String? {
        val value = dict.get(key)

        return when (value) {
            is PdfString -> value.value
            is PdfHexString -> value.value
            is PdfName -> value.name
            else -> null
        }
    }

    protected open fun decodeDate(dict: PdfDict?, key: PdfName): String? =
        dict?.let { decodeText(dict, key.name) } // TODO: map to Instant

}