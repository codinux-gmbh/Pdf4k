package net.codinux.pdf.core.objects

open class PdfRef protected constructor(
    val objectNumber: Int,
    val generationNumber: Int,
) : PdfObject() {

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

    override fun toString() = tag(objectNumber, generationNumber)

}