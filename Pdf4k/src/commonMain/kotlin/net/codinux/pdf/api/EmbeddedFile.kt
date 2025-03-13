package net.codinux.pdf.api

import net.codinux.pdf.core.objects.PdfRawStream
import net.codinux.pdf.core.parser.ByteStream
import net.codinux.pdf.core.streams.StreamDecoder

@OptIn(ExperimentalUnsignedTypes::class)
open class EmbeddedFile(
    val filename: String,
    val size: Int? = null,
    val description: String? = null,
    val mimeType: String? = null,
    val md5Hash: String? = null,
    val relationship: String? = null, // TODO: map to enum AssociatedFileRelationship
    val creationDate: String? = null, // TODO: map to Instant
    val modificationDate: String? = null, // TODO: map to Instant

    protected val isCompressed: Boolean,
    protected val embeddedFileStream: PdfRawStream,
    protected val decoder: StreamDecoder = StreamDecoder.Instance
) {

    val fileContent: ByteArray by lazy {
        if (isCompressed == false) {
            embeddedFileStream.contents
        } else {
            ByteStream.fromPdfRawStream(embeddedFileStream, decoder).getBytes()
        }.toByteArray()
    }

    val fileContentAsString: String by lazy { fileContent.decodeToString() }


    override fun toString() = "$filename${mimeType?.let { " ($it)" } ?: ""}, ${size ?: fileContent.size} bytes"
}