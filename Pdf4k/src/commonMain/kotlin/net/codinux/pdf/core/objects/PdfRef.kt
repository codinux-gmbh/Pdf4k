package net.codinux.pdf.core.objects

open class PdfRef(
    val objectNumber: Int,
    val generationNumber: Int,
) : PdfObject {

    companion object {
        fun tag(objectNumber: Int, generationNumber: Int) = "$objectNumber $generationNumber R"

        fun getOrCreate(pool: MutableMap<String, PdfRef>, objectNumber: Int, generationNumber: Int): PdfRef {
            val tag = tag(objectNumber, generationNumber)

            pool[tag]?.let { existing ->
                return existing
            }

            val newRef = PdfRef(objectNumber, generationNumber)
            pool.put(tag, newRef)

            return newRef
        }
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PdfRef) return false

        if (objectNumber != other.objectNumber) return false

        // we ignore the generation number, there should never be two objects with the same object number but different
        // generation numbers in a PDF.
        // that's also senseful for PdfRef to objects in object streams as for these the reference to it states no generation number:
        // "The generation number of an object stream and of any compressed object shall be zero." (p. 63)

        return true
    }

    override fun hashCode(): Int = objectNumber // the same here, we ignore the generation number, see equals()

    override fun toString() = tag(objectNumber, generationNumber)

}