package net.codinux.pdf.core.objects

data class PdfCrossReferenceSubsection(
    val firstObjectNumber: Int,
    val length: Int
) {
    override fun toString() = "$length objects starting with $firstObjectNumber"
}