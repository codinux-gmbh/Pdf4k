package net.codinux.pdf.core.structures

import net.codinux.log.logger
import net.codinux.pdf.api.PageLayout
import net.codinux.pdf.api.PageMode
import net.codinux.pdf.core.objects.PdfDict
import net.codinux.pdf.core.objects.PdfName
import net.codinux.pdf.core.objects.PdfObject
import net.codinux.pdf.core.objects.PdfString
import kotlin.enums.EnumEntries

open class PdfCatalog(items: Map<PdfName, PdfObject>) : PdfDict(items) {

    val language: String? = (items[PdfName.Lang] as? PdfString)?.value

    /**
     * (Optional; PDF 1.1) A URI dictionary containing document-level information for URI (uniform resource identifier)
     * actions (see 12.6.4.8, "URI actions").
     */
    val uri: PdfDict? = items[PdfName.URI] as? PdfDict

    /**
     * (Optional) A name object specifying the page layout shall be used when the document is opened
     */
    val pageLayout: PageLayout = mapEnum(items, PdfName.PageLayout, PageLayout.entries, PageLayout.SinglePage)

    /**
     * (Optional) A name object specifying how the document shall be displayed when opened.
     */
    val pageMode: PageMode = mapEnum(items, PdfName.PageMode, PageMode.entries, PageMode.UseNone)

    /**
     * (Optional; ISO 32000-1) An extensions dictionary containing
     * developer prefix identification and version numbers for developer
     * extensions that occur in this document. 7.12, "Extensions
     * dictionary", describes this dictionary and how it shall be used.
     */
    val extensions: PdfDict? = items[PdfName.Extensions] as? PdfDict

    protected val log by logger()


    protected open fun <T : Enum<T>> mapEnum(items: Map<PdfName, PdfObject>, itemKey: PdfName, entries: EnumEntries<T>, defaultValue: T): T {
        val enumName = (items[itemKey] as? PdfName)?.name
        if (enumName != null) {
            val enumValue = entries.firstOrNull { it.name == enumName }
            if (enumValue != null) {
                return enumValue
            } else {
                log.warn { "Could not find Enum value with name '$enumName' in Enum entries '${entries.joinToString { it.name }}'" }
            }
        }

        return defaultValue
    }

}