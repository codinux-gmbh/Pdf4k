package net.codinux.pdf.core.objects

open class PdfBool(val value: Boolean) : PdfObject {

    companion object {
        val True = PdfBool(true)

        val False = PdfBool(false)
    }

    override fun toString() = value.toString()
}