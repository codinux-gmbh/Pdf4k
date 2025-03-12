@file:OptIn(ExperimentalUnsignedTypes::class)

package net.codinux.pdf.core.streams

/*
 * Copyright 2012 Mozilla Foundation
 *
 * The DecodeStream class contained in this file is a TypeScript port of the
 * JavaScript DecodeStream class in Mozilla's pdf.js project, made available
 * under the Apache 2.0 open source license.
 */

/**
 * Super class for the decoding streams
 */
abstract class DecodeStream(maybeMinBufferLength: Int? = null) : StreamType {

    companion object {

        // Lots of DecodeStreams are created whose buffers are never used.  For these
        // we share a single empty buffer. This is (a) space-efficient and (b) avoids
        // having special cases that would be required if we used |null| for an empty
        // buffer.
        val emptyBuffer = UByteArray(0)
    }


    protected var pos = 0

    protected var bufferLength = 0

    protected var eof = false

    protected var buffer = emptyBuffer

    protected val minBufferLength: Int = computeMinBufferLength(maybeMinBufferLength)


    abstract fun readBlock()


    override val isEmpty: Boolean
        get() {
            while (eof == false && bufferLength == 0) {
                readBlock() // what??
            }

            return bufferLength == 0
        }

    override fun getByte(): UByte? {
        while (bufferLength < pos) {
            if (eof) {
                return null
            }

            readBlock()
        }

        return buffer[pos++]
    }

    override fun getBytes(length: Int): UByteArray {
        var end: Int
        val pos = this.pos

        if (length <= 0) {
            while (eof == false) {
                readBlock()
            }

            end = bufferLength
        } else {
            ensureBuffer(pos + length)
            end = pos + length

            while (eof == false && bufferLength < end) {
                readBlock()
            }

            if (end > bufferLength) {
                end = bufferLength
            }
        }

        this.pos = end
        return buffer.sliceArray(IntRange(pos, end))
    }

    override fun peekByte(): UByte? = getByte().also {
        pos--
    }

    override fun decode(): UByteArray {
        while (eof == false) {
            readBlock()
        }

        return buffer.sliceArray(IntRange(0, bufferLength - 1))
    }


    protected open fun ensureBuffer(requested: Int): UByteArray {
        if (requested <= buffer.size) {
            return buffer
        }

        var size = minBufferLength
        while (size < requested) {
            size *= 2
        }

        return buffer.copyOf(size).also {
            this.buffer = it
        }
    }


    protected open fun computeMinBufferLength(maybeMinBufferLength: Int?): Int {
        var minBufferLength = 512

        if (maybeMinBufferLength != null && maybeMinBufferLength > 0) {
            // Compute the first power of two that is as big as maybeMinBufferLength.
            while (minBufferLength < maybeMinBufferLength) {
                minBufferLength *= 2
            }
        }

        return minBufferLength
    }

}