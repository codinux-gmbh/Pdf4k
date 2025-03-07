package net.codinux.pdf.core.objects

open class PdfCrossRefSection(
    /**
     * If PDF file uses modern stream (`true`) or classical table (`false`) for Cross Reference Section.
     */
    val isCrossReferenceStream: Boolean,
    protected val subsections: MutableList<MutableList<PdfCrossRefEntry>> = mutableListOf()
) : PdfObject {

    protected var chunkIndex = -1

    protected var chunkLength = 0


    open fun getSections(): List<List<PdfCrossRefEntry>> =
        subsections.map { it.toList() } // make a copy so that outside is not able to modify internal state

    open fun addEntryThatIsInUse(ref: PdfRef, offset: Int) {
        append(PdfCrossRefEntry(ref, offset, false))
    }

    open fun addDeletedEntry(ref: PdfRef, nextFreeObjectNumber: Int) {
        append(PdfCrossRefEntry(ref, nextFreeObjectNumber, true))
    }

    /**
     * A Xref table may looks like this:
     *
     * ```
     * xref
     * 0 3
     * 0000000000 65535 f
     * 0000000017 00000 n
     * 0000000089 00000 n
     *
     * 5 2
     * 0000000123 00000 n
     * 0000000456 00000 n
     * ```
     *
     * The PdfRef numbers like "`0 3`" or "`5 2`" here mean: Object numbers start at `0` (`5`) and there are `3` (`2`) objects in this chunk.
     * So mind the object number gaps for correct object numbering.
     */
    protected open fun append(entry: PdfCrossRefEntry) {
        if (chunkLength == 0) {
            subsections.add(mutableListOf(entry))
            chunkIndex = 0
            chunkLength = 1
        } else {
            val chunk = subsections[chunkIndex]
            val previousEntry = chunk[chunkLength - 1]

            if (entry.ref.objectNumber - previousEntry.ref.objectNumber > 1) {
                subsections.add(mutableListOf(entry))
                chunkIndex += 1
                chunkLength = 1
            } else {
                chunk.add(entry)
                chunkLength += 1
            }
        }
    }

}