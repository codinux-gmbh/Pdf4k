package net.codinux.pdf.core.objects

open class PdfCrossRefEntry(
    val ref: PdfRef,
    /**
     * The byte offset of the object, starting from the beginning of the file.
     *
     * Or, if this entry is compressed (see [isCompressed]):
     * The object number of the object stream in which this object is stored.
     */
    val offset: Int,
    val deleted: Boolean,
    val isCompressed: Boolean? = null
) {
    override fun toString() = "$ref -> $offset${if (deleted) " (deleted)" else ""}"
}