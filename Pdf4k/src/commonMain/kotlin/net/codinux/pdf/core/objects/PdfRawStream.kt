package net.codinux.pdf.core.objects

open class PdfRawStream(dict: PdfDict, val contents: ByteArray) : PdfStream(dict) {
    override fun toString() = "Raw stream with ${contents.size} bytes and dict $dict"
}