package net.codinux.pdf.core.objects

open class PdfStream(val dict: PdfDict) : PdfObject {
    override fun toString() = "Stream with dict $dict"
}