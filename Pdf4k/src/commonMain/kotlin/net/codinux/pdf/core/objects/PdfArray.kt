package net.codinux.pdf.core.objects

class PdfArray(val items: List<PdfObject>) : PdfObject {

    val size = items.size

    fun <T> lookup(index: Int): T? =
        if (index in items.indices) items[index] as? T
        else null

    override fun toString() = items.joinToString()
}