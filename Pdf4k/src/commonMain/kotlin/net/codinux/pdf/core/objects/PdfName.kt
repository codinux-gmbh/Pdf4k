package net.codinux.pdf.core.objects

/**
 * Beginning with PDF 1.2 a name object is an atomic symbol uniquely defined by a sequence of any
 * characters (8-bit values) except null (character code 0). Uniquely defined means that any two name
 * objects that, after all escaping is expanded (see below), and the resulting sequences of bytes are not an
 * exact binary match denote different objects. Atomic means that a name has no internal structure;
 * although it is defined by a sequence of characters, those characters are not considered elements of the name.
 *
 * Even though a name may contain any characters except null, in order to express a name following the
 * general rules for parsing PDF files, when written into a PDF file names should be encoded as described
 * in the succeeding paragraphs.
 *
 * When writing a name in a PDF file, a SOLIDUS (2Fh) (/) shall be used to introduce a name. The
 * SOLIDUS is not part of the name but is a prefix indicating that what follows is a sequence of characters
 * representing the name in the PDF file and shall follow these rules:
 *
 * a) A NUMBER SIGN (23h) (#) in a name shall be written by using its 2-digit hexadecimal code (23),
 * preceded by the NUMBER SIGN.
 *
 * b) Any character in a name that is a regular character (other than NUMBER SIGN) shall be written as itself
 * or by using its 2-digit hexadecimal code, preceded by the NUMBER SIGN.
 *
 * c) Any character that is not a regular character shall be written using its 2-digit hexadecimal code,
 * preceded by the NUMBER SIGN only.
 *
 * NOTE 1
 * There is not a unique encoding of names into the PDF file because regular characters can be
 * coded in either of two ways.
 *
 * No token delimiter (such as white-space) occurs between the SOLIDUS and the encoded name. White-
 * space used as part of a name shall always be coded using the 2-digit hexadecimal notation.
 *
 * Regular characters that are outside the range EXCLAMATION MARK(21h) (!) to TILDE (7Eh) (~)
 * should be written using the hexadecimal notation.
 *
 * The token SOLIDUS (a slash followed by no regular characters) introduces a unique valid name defined
 * by the empty sequence of characters.
 *
 * NOTE 2
 * The examples shown in "Table 4 — Examples of literal names" and containing # are not valid
 * literal names in PDF 1.0 or 1.1.
 *
 * | Syntax for Literal name         | Resulting Name            |
 * |---------------------------------|---------------------------|
 * | /Name1                          | Name1                     |
 * | /ASomewhatLongerName            | ASomewhatLongerName       |
 * | /A;Name_With-Various***Characters? | A;Name_With-Various***Characters? |
 * | /1.2                            | 1.2                       |
 * | `/$$`                           | `$$`                      |
 * | /@pattern                       | @pattern                  |
 * | /.notdef                        | .notdef                   |
 * | /Lime#20Green                   | Lime Green                |
 * | /paired#28#29parentheses        | paired()parentheses       |
 * | /The_Key_of_F#23_Minor          | The_Key_of_F#_Minor       |
 * | /A#42                           | AB                        |
 *
 * In PDF syntax, literal names shall always be introduced by the SOLIDUS character (/), unlike keywords
 * such as stream, endstream, and obj.
 *
 * NOTE 3
 * This document follows a typographic convention of writing names without the leading SOLIDUS
 * when they appear in running text and tables. For example, Type and FullScreen denote names
 * that would actually be written in a PDF file (and in code examples in this document) as /Type
 * and /FullScreen.
 *
 * The maximum length of a name used in the computer on which the PDF processor is running may be
 * subject to an implementation limit; see Annex C, "Advice on maximising portability". The limit applies
 * to the number of characters in the name’s internal representation. For example, the name /A#20B has
 * three characters (A, SPACE, B), not six.
 *
 * As stated above, name objects shall be treated as atomic within a PDF file. Ordinarily, the bytes making
 * up the name are never treated as text to be presented to a human user or to an application external to a
 * PDF processor. However, occasionally the need arises to treat a name object as text, such as one that
 * represents a font name (see the BaseFont entry in "Table 109 — Entries in a Type 1 font dictionary"),
 * a colourant name in a Separation or DeviceN colour space, or a structure type (see 14.7.3, "Structure
 * types").
 *
 * In such situations, the sequence of bytes making up the name object should be interpreted according to
 * UTF-8, a variable-length byte-encoded representation of Unicode in which the printable ASCII
 * characters have the same representations as in ASCII. This enables a name object to represent text
 * virtually in any natural language, subject to the implementation limit on the length of a name.
 *
 * NOTE 4
 * PDF syntax does not prescribe what UTF-8 sequence to choose for representing any given piece
 * of externally specified text as a name object. In some cases, multiple UTF-8 sequences can
 * represent the same logical text. PDF name objects are considered distinct objects if, after all
 * escaping is expanded, the resulting sequences of bytes are not an exact binary match.
 */
open class PdfName(val name: String): PdfObject {

    companion object {
        fun getOrCreate(pool: MutableMap<String, PdfName>, name: String): PdfName {
            pool[name]?.let { return it }

            val newInstance = PdfName(name)
            pool[name] = newInstance
            return newInstance
        }

        val Length = PdfName("Length")
        val FlateDecode = PdfName("FlateDecode")
        val Resources = PdfName("Resources")
        val Font = PdfName("Font")
        val XObject = PdfName("XObject")
        val ExtGState = PdfName("ExtGState")
        val Contents = PdfName("Contents")
        val Type = PdfName("Type")
        val Parent = PdfName("Parent")
        val MediaBox = PdfName("MediaBox")
        val Page = PdfName("Page")
        val Annots = PdfName("Annots")
        val TrimBox = PdfName("TrimBox")
        val ArtBox = PdfName("ArtBox")
        val BleedBox = PdfName("BleedBox")
        val CropBox = PdfName("CropBox")
        val Rotate = PdfName("Rotate")
        val Title = PdfName("Title")
        val Author = PdfName("Author")
        val Subject = PdfName("Subject")
        val Creator = PdfName("Creator")
        val Keywords = PdfName("Keywords")
        val Producer = PdfName("Producer")
        val CreationDate = PdfName("CreationDate")
        val ModDate = PdfName("ModDate")

        val Filter = PdfName("Filter")
        val Root = PdfName("Root")
        val Encrypt = PdfName("Encrypt")
        val Info = PdfName("Info")
        val ID = PdfName("ID")
        val Size = PdfName("Size")
        val Index = PdfName("Index")
        val Prev = PdfName("Prev")

        // /Root dictionary / catalog entries
        val Version = PdfName("Version")
        val Lang = PdfName("Lang")
        val URI = PdfName("URI")
        val PageLayout = PdfName("PageLayout")
        val PageMode = PdfName("PageMode")
        val Extensions = PdfName("Extensions")
        // embedded files
        val AF = PdfName("AF")
        val Names = PdfName("Names")
        val EmbeddedFiles = PdfName("EmbeddedFiles")
    }


    // so that equality checks in Map<>s work
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PdfName) return false

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString() = name
}