package net.codinux.pdf.core.document

import net.codinux.pdf.core.objects.PdfArray
import net.codinux.pdf.core.objects.PdfObject

class TrailerInfo (
    /**
     * (Required; shall not be an indirect reference) The total number of entries
     * in the PDF file’s cross-reference table, as defined by the combination of
     * the original section and all update sections. Equivalently, this value shall
     * be 1 greater than the highest object number defined in the PDF file.
     * Any object in a cross-reference section whose number is greater than
     * this value shall be ignored and defined to be missing by a PDF reader.
     */
    val size: Int?,

    /**
     * (Required; shall be an indirect reference) The catalog dictionary for the
     * PDF file (see 7.7.2, "Document catalog dictionary").
     */
    val root: PdfObject? = null,

    /**
     * (Required if document is encrypted; PDF 1.1) The PDF file’s encryption
     * dictionary (see 7.6, "Encryption").
     */
    val encrypt: PdfObject? = null,

    /**
     * (Optional; shall be an indirect reference) The PDF file’s information
     * dictionary. As described in 14.3.3, "Document information dictionary",
     * this method for specifying document metadata has been deprecated in
     * PDF 2.0 and should therefore only be used to encode information that is
     * stated as required elsewhere in this document.
     */
    val info: PdfObject? = null,

    /**
     * (Optional; shall be an indirect reference) The PDF file’s information
     * dictionary. As described in 14.3.3, "Document information dictionary",
     * this method for specifying document metadata has been deprecated in
     * PDF 2.0 and should therefore only be used to encode information that is
     * stated as required elsewhere in this document.
     *
     * Because the ID entries are not encrypted, the ID key can be checked to
     * assure that the correct PDF file is being accessed without decrypting
     * the PDF file. The restrictions that the objects all be direct objects and
     * not be encrypted ensure this.
     */
    val id: PdfArray? = null,

    /**
     * (Optional, present only if the file has more than one cross-reference
     * section; shall be a direct object) The byte offset from the beginning of the
     * PDF file to the beginning of the previous cross-reference stream.
     */
    val previousCrossReferenceSectionByteOffset: Int? = null,
) {
}