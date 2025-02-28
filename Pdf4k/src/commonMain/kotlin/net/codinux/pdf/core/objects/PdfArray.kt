package net.codinux.pdf.core.objects

class PdfArray(val items: List<PdfObject>) : PdfObject {
    override fun toString() = items.joinToString()
}