package net.codinux.pdf.core.objects

@OptIn(ExperimentalUnsignedTypes::class)
open class PdfRawStream(dict: PdfDict, val contents: UByteArray) : PdfStream(dict) {
    override fun toString() = "Raw stream with ${contents.size} bytes and dict $dict"
}