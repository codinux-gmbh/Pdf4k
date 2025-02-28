package net.codinux.pdf.core.structures

import net.codinux.pdf.core.objects.PdfDict
import net.codinux.pdf.core.objects.PdfName
import net.codinux.pdf.core.objects.PdfObject

open class PdfPageLeaf(items: Map<PdfName, PdfObject>, val autoNormalizeCTM: Boolean = true) : PdfDict(items) {
}