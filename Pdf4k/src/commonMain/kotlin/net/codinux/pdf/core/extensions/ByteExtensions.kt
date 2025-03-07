@file:OptIn(ExperimentalUnsignedTypes::class)

package net.codinux.pdf.core.extensions


infix fun Byte.shl(bitCount: Int): Int = this.toInt().shl(bitCount)

infix fun UByte.shl(bitCount: Int): Int = this.toInt().shl(bitCount)


// this is a quite naive and not very efficient implementation. But as we are only searching for 'startxref' and
// 'trailer' quite at the end of the PDF file and all efficient suggestions of ChatGPT were wrong, i now stick with this one
fun UByteArray.lastIndexOf(pattern: UByteArray, startIndex: Int = this.size - 1 - pattern.size): Int? {
    if (pattern.isEmpty() || this.isEmpty() || startIndex < 0 || pattern.size > this.size || startIndex > this.size - 1 - pattern.size) {
        return null
    }

    for (i in startIndex downTo 0) {
        if (this.sliceArray(i until i + pattern.size).contentEquals(pattern)) {
            return i
        }
    }

    return null
}