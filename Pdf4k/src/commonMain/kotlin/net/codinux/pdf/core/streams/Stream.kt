package net.codinux.pdf.core.streams

@OptIn(ExperimentalUnsignedTypes::class)
open class Stream(
    protected val bytes: UByteArray,
    protected val start: Int = 0,
    protected val end: Int = bytes.size
) : StreamType {

    protected var pos = start


    open val length: Int = end - start

    override val isEmpty: Boolean = length == 0

    override fun getByte(): UByte? =
        if (pos >= end) null
        else bytes[pos++]

//    override fun getUInt16(): UShort {
//        val byte0 = getByte()
//        val byte1 = getByte()
//
//        return if (byte0 == OutOfRangeByte || byte1 == OutOfRangeByte) {
//            OutOfRangeUShort
//        } else {
//            (byte0.shl(8) + byte1).toUShort()
//        }
//    }
//
//    override fun getInt32(): Int =
//        getByte().shl(24) + getByte().shl(16) + getByte().shl(8) + getByte()

    override fun getBytes(length: Int /*, forceClamped: Boolean?*/): UByteArray =
        if (length <= 0) {
            bytes.sliceArray(IntRange(pos, end))
        } else {
            val streamEnd = this.end
            var end = pos + length
            if (end > streamEnd) {
                end = streamEnd
            }

            this.pos = end
            bytes.sliceArray(IntRange(pos, end))
        }

    override fun peekByte(): UByte? = getByte().also {
        this.pos--
    }

//    override fun peekBytes(length: Int, forceClamped: Boolean?): ByteArray {
//    }
//
//    override fun skip(countBytes: Int) {
//    }
//
//    override fun reset() {
//    }
//
//    override fun makeSubStream(start: Int, length: Int): StreamType {
//    }

    override fun decode(): UByteArray = bytes

}