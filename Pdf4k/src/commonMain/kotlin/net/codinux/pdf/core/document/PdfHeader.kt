package net.codinux.pdf.core.document

open class PdfHeader(
    val major: Int,
    val minor: Int,
) {
    override fun toString() = "$major.$minor"
}