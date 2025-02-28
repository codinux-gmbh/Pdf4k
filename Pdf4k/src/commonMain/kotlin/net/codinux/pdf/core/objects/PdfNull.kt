package net.codinux.pdf.core.objects

open class PdfNull : PdfObject {

    companion object {
        val Null = PdfNull()
    }


    override fun toString() = "null"
}