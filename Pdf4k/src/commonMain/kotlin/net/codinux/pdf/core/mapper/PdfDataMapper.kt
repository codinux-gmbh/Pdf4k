package net.codinux.pdf.core.mapper

import net.codinux.log.logger
import net.codinux.pdf.api.EmbeddedFile
import net.codinux.pdf.core.document.ReferenceResolver
import net.codinux.pdf.core.objects.*
import net.codinux.pdf.core.streams.StreamDecoder

open class PdfDataMapper(
    protected val resolver: ReferenceResolver,
    protected val decoder: StreamDecoder = StreamDecoder.Instance
) {

    protected val log by logger()


    open fun getEmbeddedFiles(catalog: PdfDict): List<EmbeddedFile> = try {
        // the default is storing embedded files in the AssociatedFiles array at document level
        val associatedFiles = resolver.lookupArray(catalog.get(PdfName.AF))
        if (associatedFiles != null) {
            readEmbeddedFilesArray(associatedFiles)
        } else { // additionally and only sometimes (which is not PDF/A-3 conformant) they are stored in Names -> EmbeddedFiles -> Names dictionary entries
            val embeddedFiles = resolver.lookupDict(resolver.lookupDict(catalog.get(PdfName.Names))?.get(PdfName.EmbeddedFiles))
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
        val embeddedFilesNames = resolver.lookupArray(embeddedFilesDict.get(PdfName.Names))

        return if (embeddedFilesNames != null) {
            readEmbeddedFilesArray(embeddedFilesNames)
        } else {
            // don't know how often this is the case, but Factur-X specification says sometimes embedded files are not directly in
            // EmbeddedFiles -> Names dict but indirectly in EmbeddedFiles -> Kids -> Names
            val kids = resolver.lookupArray(embeddedFilesDict.get("Kids"))
            kids?.items.orEmpty().filterIsInstance<PdfDict>().flatMap { readEmbeddedFilesDict(it) }
        }
    }

    protected open fun readEmbeddedFilesArray(embeddedFilesNames: PdfArray): List<EmbeddedFile> =
        embeddedFilesNames.items.mapNotNull { resolver.lookupDict(it) }.mapNotNull { entry ->
            try {
                mapEmbeddedFile(entry)
            } catch (e: Throwable) {
                log.error(e) { "Could not read embedded file entry $entry" }
                null
            }
        }

    protected open fun mapEmbeddedFile(fileSpecification: PdfDict): EmbeddedFile? {
        val embeddedFile = resolver.lookupDict(fileSpecification.get("EF"))
        val embeddedFileStream = resolver.lookupStream(embeddedFile?.get("F") ?: embeddedFile?.get("UF"))

        return if (embeddedFile == null || embeddedFileStream == null) {
            null
        } else {
            val filename = decodeText(fileSpecification, "F")
            val unicodeFilename = decodeText(fileSpecification, "UF")
            val description = decodeText(fileSpecification, "Desc")
            val relationship = decodeText(fileSpecification, "AFRelationship")

            val dict = embeddedFileStream.dict
            val params = resolver.lookupDict(dict.get("Params"))
            val mimeType = decodeText(dict, "Subtype")
            val md5Hash = resolver.lookupString(params?.get("CheckSum"))?.value
            val creationDate = decodeDate(params, PdfName.CreationDate)
            val modificationDate = decodeDate(params, PdfName.ModDate)

            val isCompressed = embeddedFileStream.dict.get(PdfName.Filter) != null
            // dict -> /Length: The number of bytes in this stream. If the stream is compressed then this is the number of compressed bytes
            val streamLength = (resolver.lookupNumber(dict.get(PdfName.Length))
                ?: resolver.lookupNumber(dict.get(PdfName.Size))) // 'Length' is correct, some use 'Size'
                ?.value?.toInt()
            // /Params -> /Size: (Optional) The size of the uncompressed embedded file, in bytes
            val size = if (isCompressed) resolver.lookupNumber(params?.get(PdfName.Size))?.value?.toInt() else streamLength

            EmbeddedFile(
                unicodeFilename ?: filename ?: "", // A PDF reader shall use the value of the UF key, when present, instead of the F key.
                size,
                description,
                mimeType,
                md5Hash,
                relationship,
                creationDate,
                modificationDate,

                isCompressed,
                embeddedFileStream,
                decoder
            )
        }
    }


    open fun decodeText(dict: PdfDict, key: String): String? {
        val value = dict.get(key)

        return when (value) {
            is PdfString -> value.value
            is PdfHexString -> value.value
            is PdfName -> value.name
            else -> null
        }
    }

    open fun decodeDate(dict: PdfDict?, key: PdfName): String? =
        dict?.let { decodeText(dict, key.name) } // TODO: map to Instant

}