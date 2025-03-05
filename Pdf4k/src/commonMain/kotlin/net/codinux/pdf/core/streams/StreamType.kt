package net.codinux.pdf.core.streams

@OptIn(ExperimentalUnsignedTypes::class)
interface StreamType {

    companion object {
        val OutOfRangeByte: UByte? = null
    }


    val isEmpty: Boolean

    fun getByte(): UByte?

    // seems only to be used in tests
    // fun getUInt16(): UShort

    // seems only to be used in tests
    // fun getInt32(): Int

    // forceClamped seems only to be used in tests
    fun getBytes(length: Int /*, forceClamped: Boolean? = null*/): UByteArray

    fun peekByte(): UByte?

    // does not seem to be used
    // fun peekBytes(length: Int, forceClamped: Boolean? = null): ByteArray

    // seems only to be used in tests
    // fun skip(countBytes: Int)

    // seems only to be used in tests
    // fun reset()

    // seems only to be used in tests
    // fun makeSubStream(start: Int, length: Int): StreamType

    fun decode(): UByteArray

}