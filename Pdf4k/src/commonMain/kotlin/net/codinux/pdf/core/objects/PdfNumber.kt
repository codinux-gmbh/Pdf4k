package net.codinux.pdf.core.objects

open class PdfNumber(val value: Number): PdfObject {
    override fun toString() = value.toString()
}