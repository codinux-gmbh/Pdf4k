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
        if (generationNumber != other.generationNumber) return false

        return true
    }

    override fun hashCode(): Int {
        var result = objectNumber
        result = 31 * result + generationNumber
        return result
    }

    override fun toString() = tag(objectNumber, generationNumber)

}