package net.codinux.pdf.core.document

class PdfTrailer(
    val lastXRefOffset: Int
) {
    override fun toString() = "startxref\\n$lastXRefOffset\\n%%EOF"
}