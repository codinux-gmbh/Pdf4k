package net.codinux.pdf.core.mapper

@OptIn(ExperimentalUnsignedTypes::class)
open class UTF16Charset(val littleEndian: Boolean) {

    open fun decode(src: UByteArray, start: Int = 0, end: Int = src.size): String {
        val out = StringBuilder()
        var consumed = 0

        for (n in start until end step 2) {
            val char = src.readS16(n, littleEndian).toChar()
            out.append(char)
            consumed += 2
        }

        return out.toString()
    }


    // Copied from korlibs/korge: https://github.com/korlibs/korge/blob/main/kmem/src/commonMain/kotlin/korlibs/memory/Bits.kt

    /** Takes n[bits] of [this] [Int], and extends the last bit, creating a plain [Int] in one's complement */
    fun Int.signExtend(bits: Int): Int = (this shl (32 - bits)) shr (32 - bits) // Int.SIZE_BITS

    // Copied from korlibs/korge: https://github.com/korlibs/korge/blob/main/kmem/src/commonMain/kotlin/korlibs/memory/ByteArrayReadWrite.kt

    private fun UByteArray.u8(o: Int): Int = this[o].toInt() and 0xFF

    private inline fun UByteArray.read16LE(o: Int): Int = (u8(o + 0) shl 0) or (u8(o + 1) shl 8)

    private inline fun UByteArray.read16BE(o: Int): Int = (u8(o + 1) shl 0) or (u8(o + 0) shl 8)

    // Signed
    fun UByteArray.readS16LE(o: Int): Int = read16LE(o).signExtend(16)
    fun UByteArray.readS16BE(o: Int): Int = read16BE(o).signExtend(16)

    // Custom Endian
    fun UByteArray.readS16(o: Int, little: Boolean): Int = if (little) readS16LE(o) else readS16BE(o)

}