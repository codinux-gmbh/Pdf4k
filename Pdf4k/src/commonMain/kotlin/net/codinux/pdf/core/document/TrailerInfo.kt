package net.codinux.pdf.core.document

import net.codinux.pdf.core.objects.PdfObject

class TrailerInfo(
    val root: PdfObject? = null,
    val encrypt: PdfObject? = null,
    val info: PdfObject? = null,
    val id: PdfObject? = null,
) {
}