package net.codinux.pdf.core.objects

class PdfArray(val items: List<PdfObject>) : PdfObject {

    val size = items.size

    operator fun get(index: Int): PdfObject? =
        if (index in items.indices) items[index]
        else null

    @Suppress("UNCHECKED_CAST")
    fun <T : PdfObject> getAs(index: Int): T? = get(index) as? T

    override fun toString() = items.joinToString()
}