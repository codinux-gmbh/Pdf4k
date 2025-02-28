package net.codinux.pdf.core.objects

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
    }

    override fun toString() = name
}