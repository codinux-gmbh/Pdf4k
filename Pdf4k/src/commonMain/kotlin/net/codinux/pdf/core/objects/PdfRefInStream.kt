package net.codinux.pdf.core.objects

open class PdfRefInStream(
    objectNumber: Int,
    val objectNumberOfContainingStream: Int,
    val indexInStreamObject: Int
) : PdfRef(objectNumber, 0) {
    override fun toString() = "${super.toString()} in Stream $objectNumberOfContainingStream"
}