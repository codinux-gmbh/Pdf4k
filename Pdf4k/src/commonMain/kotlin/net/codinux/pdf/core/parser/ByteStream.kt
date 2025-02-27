package net.codinux.pdf.core.parser

import net.codinux.pdf.core.syntax.CharCodes

open class ByteStream(protected val bytes: ByteArray) {

    protected val length = bytes.size

    protected var index = 0
    protected var line = 0
    protected var column = 0


    open fun moveTo(offset: Int) {
        this.index = offset
    }

    open fun next(): Byte {
        val byte = this.bytes[this.index++]

        if (byte == CharCodes.Newline) {
            this.line++
            this.column = 0
        } else {
            this.column++
        }

        return byte
    }

    open fun peek(): Byte = this.bytes[this.index]

    open fun peekAhead(steps: Int): Byte = this.bytes[this.index + steps]

    open fun peekAt(offset: Int): Byte = this.bytes[offset]

    open fun assertNext(expected: Byte): Byte {
        if (this.peek() != expected) {
            throw IllegalStateException()
        }

        return this.next()
    }

    open fun done(): Boolean = this.index >= length

    open fun hasNext(): Boolean = this.index < length

    open fun offset(): Int = this.index

    open fun slice(start: Int, end: Int): ByteArray = this.bytes.sliceArray(IntRange(start, end - 1))

    open fun position(): Position = Position(line, column, column)


    open class Position(val line: Int, val column: Int, val offset: Int) {
        override fun toString(): String = "(line:$line column:$column offset:$offset)"
    }

}