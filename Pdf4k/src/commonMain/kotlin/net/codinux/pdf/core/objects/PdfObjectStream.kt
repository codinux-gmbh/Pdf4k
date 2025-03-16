package net.codinux.pdf.core.objects

class PdfObjectStream(
    val dict: PdfDict,
    val numberOfContainedObjects: Int,
    val containingObjects: Map<PdfRef, PdfObject>
) : PdfObject {
    override fun toString() = "${containingObjects.size} objects: ${containingObjects.entries.joinToString { "${it.key.objectNumber}: ${it.value::class.simpleName}" }}"
}