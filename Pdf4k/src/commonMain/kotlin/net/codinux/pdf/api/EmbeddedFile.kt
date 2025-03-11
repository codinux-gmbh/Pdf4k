package net.codinux.pdf.api

open class EmbeddedFile(
    val filename: String,
    val data: UByteArray,
    val size: Int? = null,
    val description: String? = null,
    val mimeType: String? = null,
    val md5Hash: String? = null,
    val relationship: String? = null, // TODO: map to enum AssociatedFileRelationship
    val creationDate: String? = null, // TODO: map to Instant
    val modificationDate: String? = null, // TODO: map to Instant
) {
    override fun toString() = "$filename${mimeType?.let { " ($it)" } ?: ""}, ${size ?: data.size} bytes"
}