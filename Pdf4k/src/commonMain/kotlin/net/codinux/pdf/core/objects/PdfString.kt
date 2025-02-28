package net.codinux.pdf.core.objects

open class PdfString(val value: String) : PdfObject {
    override fun toString() = value
}