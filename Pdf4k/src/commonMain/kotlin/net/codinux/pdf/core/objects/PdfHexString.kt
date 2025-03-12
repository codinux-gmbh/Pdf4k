package net.codinux.pdf.core.objects

open class PdfHexString(val value: String, val asHex: String) : PdfObject {
    override fun toString() = value
}