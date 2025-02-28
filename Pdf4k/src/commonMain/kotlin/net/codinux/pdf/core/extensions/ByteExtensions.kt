package net.codinux.pdf.core.extensions


infix fun Byte.shl(bitCount: Int): Int = this.toInt().shl(bitCount)