package net.codinux.pdf.core.objects

open class PdfCrossRefEntry(
    val ref: PdfRef,
    val offset: Int,
    val deleted: Boolean
) {
    override fun toString() = "$ref -> $offset${if (deleted) " (deleted)" else ""}"
}