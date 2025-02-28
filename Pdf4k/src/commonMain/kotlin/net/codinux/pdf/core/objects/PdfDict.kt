package net.codinux.pdf.core.objects

open class PdfDict(val items: Map<PdfName, PdfObject>): PdfObject {

    fun get(name: PdfName): PdfObject? = items[name]

    fun get(name: String): PdfObject? = items.entries.firstOrNull { it.key.name == name }?.value

    fun <T: PdfObject> getAs(name: PdfName): T? = get(name) as? T

    fun <T : PdfObject> getAs(name: String): T? = get(name) as? T


    override fun toString() = items.entries.joinToString("\n") { it.key.toString() + ": " + it.value.toString() }
}