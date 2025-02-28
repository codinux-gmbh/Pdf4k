package net.codinux.pdf.core.streams

interface StreamType {

    companion object {
        val OutOfRangeByte: Byte = -1
        val OutOfRangeUShort: UShort = (-1).toUShort()
    }


    val isEmpty: Boolean

    fun getByte(): Byte

    // seems only to be used in tests
    // fun getUInt16(): UShort

    // seems only to be used in tests
    // fun getInt32(): Int

    // forceClamped seems only to be used in tests
    fun getBytes(length: Int /*, forceClamped: Boolean? = null*/): ByteArray

    fun peekByte(): Byte

    // does not seem to be used
    // fun peekBytes(length: Int, forceClamped: Boolean? = null): ByteArray

    // seems only to be used in tests
    // fun skip(countBytes: Int)

    // seems only to be used in tests
    // fun reset()

    // seems only to be used in tests
    // fun makeSubStream(start: Int, length: Int): StreamType

    fun decode(): ByteArray

}