package net.codinux.pdf.core.objects

open class PdfHexString(val value: String) : PdfObject {
    override fun toString() = value
}