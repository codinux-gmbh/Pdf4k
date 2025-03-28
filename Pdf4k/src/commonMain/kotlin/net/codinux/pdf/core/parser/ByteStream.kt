package net.codinux.pdf.core.parser

import net.codinux.pdf.core.NextByteAssertionError
import net.codinux.pdf.core.objects.PdfRawStream
import net.codinux.pdf.core.streams.StreamDecoder
import net.codinux.pdf.core.syntax.CharCodes

@OptIn(ExperimentalUnsignedTypes::class)
open class ByteStream(protected val bytes: UByteArray) {

    companion object {
        fun fromPdfRawStream(rawStream: PdfRawStream, decoder: StreamDecoder = StreamDecoder()): ByteStream =
            ByteStream(decoder.decodePdfRawStream(rawStream).decode())
    }


    protected val length = bytes.size

    protected var index = 0
    protected var line = 0
    protected var column = 0


    internal fun getBytes() = bytes


    open fun moveTo(offset: Int) {
        this.index = offset
    }

    open fun next(): UByte {
        val byte = this.bytes[this.index++]

        if (byte == CharCodes.Newline) {
            this.line++
            this.column = 0
        } else {
            this.column++
        }

        return byte
    }

    open fun peek(): UByte = this.bytes[this.index]

    open fun peekAhead(steps: Int): UByte = this.bytes[this.index + steps]

    open fun peekAt(offset: Int): UByte = this.bytes[offset]

    open fun assertNext(expected: UByte): UByte {
        if (this.peek() != expected) {
            throw NextByteAssertionError(this.position(), expected, this.peek())
        }

        return this.next()
    }

    open fun done(): Boolean = this.index >= length

    open fun hasNext(): Boolean = this.index < length

    open fun offset(): Int = this.index

    open fun slice(start: Int, end: Int): UByteArray = this.bytes.sliceArray(IntRange(start, end - 1))

    open fun position(): Position = Position(line, column, offset())


    open class Position(val line: Int, val column: Int, val offset: Int) {
        override fun toString(): String = "(line:$line column:$column offset:$offset)"
    }

}