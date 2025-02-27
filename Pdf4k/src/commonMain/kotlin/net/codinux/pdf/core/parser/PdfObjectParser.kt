package net.codinux.pdf.core.parser

import net.codinux.pdf.core.document.PdfContext

open class PdfObjectParser(
    bytes: ByteStream,
    protected val context: PdfContext,
    capNumbers: Boolean = false
) : BaseParser(bytes, capNumbers) {
}