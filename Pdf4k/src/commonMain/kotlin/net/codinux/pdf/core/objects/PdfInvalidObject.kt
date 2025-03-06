package net.codinux.pdf.core.objects

@OptIn(ExperimentalUnsignedTypes::class)
open class PdfInvalidObject(val data: UByteArray) : PdfObject {
    override fun toString() = "Invalid PDF object: ${data.joinToString()}"
}