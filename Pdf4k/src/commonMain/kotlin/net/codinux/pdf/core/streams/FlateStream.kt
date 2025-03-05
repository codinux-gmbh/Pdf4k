package net.codinux.pdf.core.streams

import net.codinux.pdf.core.extensions.shl

/*
 * Copyright 1996-2003 Glyph & Cog, LLC
 *
 * The flate stream implementation contained in this file is a JavaScript port
 * of XPDF's implementation, made available under the Apache 2.0 open source
 * license.
 */

/*
 * Copyright 2012 Mozilla Foundation
 *
 * The FlateStream class contained in this file is a TypeScript port of the
 * JavaScript FlateStream class in Mozilla's pdf.js project, made available
 * under the Apache 2.0 open source license.
 */
@OptIn(ExperimentalUnsignedTypes::class)
open class FlateStream(protected val stream: StreamType, maybeLength: Int? = null) : DecodeStream(maybeLength) {

    companion object {
        val codeLenCodeMap = intArrayOf(16, 17, 18, 0, 8, 7, 9, 6, 10, 5, 11, 4, 12, 3, 13, 2, 14, 1, 15)

        val lengthDecode = intArrayOf(
            0x00003, 0x00004, 0x00005, 0x00006, 0x00007, 0x00008, 0x00009, 0x0000a,
            0x1000b, 0x1000d, 0x1000f, 0x10011, 0x20013, 0x20017, 0x2001b, 0x2001f,
            0x30023, 0x3002b, 0x30033, 0x3003b, 0x40043, 0x40053, 0x40063, 0x40073,
            0x50083, 0x500a3, 0x500c3, 0x500e3, 0x00102, 0x00102, 0x00102
        )

        val distDecode = intArrayOf(
            0x00001, 0x00002, 0x00003, 0x00004, 0x10005, 0x10007, 0x20009, 0x2000d,
            0x30011, 0x30019, 0x40021, 0x40031, 0x50041, 0x50061, 0x60081, 0x600c1,
            0x70101, 0x70181, 0x80201, 0x80301, 0x90401, 0x90601, 0xa0801, 0xa0c01,
            0xb1001, 0xb1801, 0xc2001, 0xc3001, 0xd4001, 0xd6001
        )

        val fixedLitCodeTab = Pair(intArrayOf(
            0x70100, 0x80050, 0x80010, 0x80118, 0x70110, 0x80070, 0x80030, 0x900c0,
            0x70108, 0x80060, 0x80020, 0x900a0, 0x80000, 0x80080, 0x80040, 0x900e0,
            0x70104, 0x80058, 0x80018, 0x90090, 0x70114, 0x80078, 0x80038, 0x900d0,
            0x7010c, 0x80068, 0x80028, 0x900b0, 0x80008, 0x80088, 0x80048, 0x900f0,
            0x70102, 0x80054, 0x80014, 0x8011c, 0x70112, 0x80074, 0x80034, 0x900c8,
            0x7010a, 0x80064, 0x80024, 0x900a8, 0x80004, 0x80084, 0x80044, 0x900e8,
            0x70106, 0x8005c, 0x8001c, 0x90098, 0x70116, 0x8007c, 0x8003c, 0x900d8,
            0x7010e, 0x8006c, 0x8002c, 0x900b8, 0x8000c, 0x8008c, 0x8004c, 0x900f8,
            0x70101, 0x80052, 0x80012, 0x8011a, 0x70111, 0x80072, 0x80032, 0x900c4,
            0x70109, 0x80062, 0x80022, 0x900a4, 0x80002, 0x80082, 0x80042, 0x900e4,
            0x70105, 0x8005a, 0x8001a, 0x90094, 0x70115, 0x8007a, 0x8003a, 0x900d4,
            0x7010d, 0x8006a, 0x8002a, 0x900b4, 0x8000a, 0x8008a, 0x8004a, 0x900f4,
            0x70103, 0x80056, 0x80016, 0x8011e, 0x70113, 0x80076, 0x80036, 0x900cc,
            0x7010b, 0x80066, 0x80026, 0x900ac, 0x80006, 0x80086, 0x80046, 0x900ec,
            0x70107, 0x8005e, 0x8001e, 0x9009c, 0x70117, 0x8007e, 0x8003e, 0x900dc,
            0x7010f, 0x8006e, 0x8002e, 0x900bc, 0x8000e, 0x8008e, 0x8004e, 0x900fc,
            0x70100, 0x80051, 0x80011, 0x80119, 0x70110, 0x80071, 0x80031, 0x900c2,
            0x70108, 0x80061, 0x80021, 0x900a2, 0x80001, 0x80081, 0x80041, 0x900e2,
            0x70104, 0x80059, 0x80019, 0x90092, 0x70114, 0x80079, 0x80039, 0x900d2,
            0x7010c, 0x80069, 0x80029, 0x900b2, 0x80009, 0x80089, 0x80049, 0x900f2,
            0x70102, 0x80055, 0x80015, 0x8011d, 0x70112, 0x80075, 0x80035, 0x900ca,
            0x7010a, 0x80065, 0x80025, 0x900aa, 0x80005, 0x80085, 0x80045, 0x900ea,
            0x70106, 0x8005d, 0x8001d, 0x9009a, 0x70116, 0x8007d, 0x8003d, 0x900da,
            0x7010e, 0x8006d, 0x8002d, 0x900ba, 0x8000d, 0x8008d, 0x8004d, 0x900fa,
            0x70101, 0x80053, 0x80013, 0x8011b, 0x70111, 0x80073, 0x80033, 0x900c6,
            0x70109, 0x80063, 0x80023, 0x900a6, 0x80003, 0x80083, 0x80043, 0x900e6,
            0x70105, 0x8005b, 0x8001b, 0x90096, 0x70115, 0x8007b, 0x8003b, 0x900d6,
            0x7010d, 0x8006b, 0x8002b, 0x900b6, 0x8000b, 0x8008b, 0x8004b, 0x900f6,
            0x70103, 0x80057, 0x80017, 0x8011f, 0x70113, 0x80077, 0x80037, 0x900ce,
            0x7010b, 0x80067, 0x80027, 0x900ae, 0x80007, 0x80087, 0x80047, 0x900ee,
            0x70107, 0x8005f, 0x8001f, 0x9009e, 0x70117, 0x8007f, 0x8003f, 0x900de,
            0x7010f, 0x8006f, 0x8002f, 0x900be, 0x8000f, 0x8008f, 0x8004f, 0x900fe,
            0x70100, 0x80050, 0x80010, 0x80118, 0x70110, 0x80070, 0x80030, 0x900c1,
            0x70108, 0x80060, 0x80020, 0x900a1, 0x80000, 0x80080, 0x80040, 0x900e1,
            0x70104, 0x80058, 0x80018, 0x90091, 0x70114, 0x80078, 0x80038, 0x900d1,
            0x7010c, 0x80068, 0x80028, 0x900b1, 0x80008, 0x80088, 0x80048, 0x900f1,
            0x70102, 0x80054, 0x80014, 0x8011c, 0x70112, 0x80074, 0x80034, 0x900c9,
            0x7010a, 0x80064, 0x80024, 0x900a9, 0x80004, 0x80084, 0x80044, 0x900e9,
            0x70106, 0x8005c, 0x8001c, 0x90099, 0x70116, 0x8007c, 0x8003c, 0x900d9,
            0x7010e, 0x8006c, 0x8002c, 0x900b9, 0x8000c, 0x8008c, 0x8004c, 0x900f9,
            0x70101, 0x80052, 0x80012, 0x8011a, 0x70111, 0x80072, 0x80032, 0x900c5,
            0x70109, 0x80062, 0x80022, 0x900a5, 0x80002, 0x80082, 0x80042, 0x900e5,
            0x70105, 0x8005a, 0x8001a, 0x90095, 0x70115, 0x8007a, 0x8003a, 0x900d5,
            0x7010d, 0x8006a, 0x8002a, 0x900b5, 0x8000a, 0x8008a, 0x8004a, 0x900f5,
            0x70103, 0x80056, 0x80016, 0x8011e, 0x70113, 0x80076, 0x80036, 0x900cd,
            0x7010b, 0x80066, 0x80026, 0x900ad, 0x80006, 0x80086, 0x80046, 0x900ed,
            0x70107, 0x8005e, 0x8001e, 0x9009d, 0x70117, 0x8007e, 0x8003e, 0x900dd,
            0x7010f, 0x8006e, 0x8002e, 0x900bd, 0x8000e, 0x8008e, 0x8004e, 0x900fd,
            0x70100, 0x80051, 0x80011, 0x80119, 0x70110, 0x80071, 0x80031, 0x900c3,
            0x70108, 0x80061, 0x80021, 0x900a3, 0x80001, 0x80081, 0x80041, 0x900e3,
            0x70104, 0x80059, 0x80019, 0x90093, 0x70114, 0x80079, 0x80039, 0x900d3,
            0x7010c, 0x80069, 0x80029, 0x900b3, 0x80009, 0x80089, 0x80049, 0x900f3,
            0x70102, 0x80055, 0x80015, 0x8011d, 0x70112, 0x80075, 0x80035, 0x900cb,
            0x7010a, 0x80065, 0x80025, 0x900ab, 0x80005, 0x80085, 0x80045, 0x900eb,
            0x70106, 0x8005d, 0x8001d, 0x9009b, 0x70116, 0x8007d, 0x8003d, 0x900db,
            0x7010e, 0x8006d, 0x8002d, 0x900bb, 0x8000d, 0x8008d, 0x8004d, 0x900fb,
            0x70101, 0x80053, 0x80013, 0x8011b, 0x70111, 0x80073, 0x80033, 0x900c7,
            0x70109, 0x80063, 0x80023, 0x900a7, 0x80003, 0x80083, 0x80043, 0x900e7,
            0x70105, 0x8005b, 0x8001b, 0x90097, 0x70115, 0x8007b, 0x8003b, 0x900d7,
            0x7010d, 0x8006b, 0x8002b, 0x900b7, 0x8000b, 0x8008b, 0x8004b, 0x900f7,
            0x70103, 0x80057, 0x80017, 0x8011f, 0x70113, 0x80077, 0x80037, 0x900cf,
            0x7010b, 0x80067, 0x80027, 0x900af, 0x80007, 0x80087, 0x80047, 0x900ef,
            0x70107, 0x8005f, 0x8001f, 0x9009f, 0x70117, 0x8007f, 0x8003f, 0x900df,
            0x7010f, 0x8006f, 0x8002f, 0x900bf, 0x8000f, 0x8008f, 0x8004f, 0x900ff
        ), 9)

        val fixedDistCodeTab = Pair(intArrayOf(
            0x50000, 0x50010, 0x50008, 0x50018, 0x50004, 0x50014, 0x5000c, 0x5001c,
            0x50002, 0x50012, 0x5000a, 0x5001a, 0x50006, 0x50016, 0x5000e, 0x00000,
            0x50001, 0x50011, 0x50009, 0x50019, 0x50005, 0x50015, 0x5000d, 0x5001d,
            0x50003, 0x50013, 0x5000b, 0x5001b, 0x50007, 0x50017, 0x5000f, 0x00000
        ), 5)
    }


    protected var codeSize = 0

    protected var codeBuf = 0


    init {
        val cmf = stream.getByte()
        val flg = stream.getByte()
        if (cmf == null || flg == null) {
            throw IllegalArgumentException("Invalid header in flate stream: $cmf, $flg")
        }
        if ((cmf and 0x0F.toUByte()) != 0x08.toUByte()) {
            throw IllegalArgumentException("Unknown compression method in flate stream: $cmf, $flg")
        }
        if ((cmf.shl(8) + flg.toUByte().toInt()) % 31 != 0) {
            throw IllegalArgumentException("Bad FCHECK in flate stream: $cmf, $flg")
        }
        if (flg and 0x20.toUByte() != 0.toUByte()) {
            throw IllegalArgumentException("FDICT bit set in flate stream: $cmf, $flg")
        }
    }


    override fun readBlock() {
        var buffer: UByteArray
        var len: Int
        val str = stream

        // Read block header
        var hdr = getBits(3)
        if (hdr and 1 != 0) {
            eof = true
        }
        hdr = hdr shr 1

        if (hdr == 0) {
            // Uncompressed block
            var byte = str.getByte()
            if (byte == null) {
                throw Error("Bad block header in flate stream")
            }

            var blockLen = byte.toInt()
            if (str.getByte().also { byte = it } == null) {
                throw Error("Bad block header in flate stream")
            }

            blockLen = blockLen or (byte!!.toInt() shl 8)

            if (str.getByte().also { byte = it } == null) {
                throw Error("Bad block header in flate stream")
            }

            var check = byte!!.toInt()
            if (str.getByte().also { byte = it } == null) {
                throw Error("Bad block header in flate stream")
            }

            check = check or (byte!!.toInt() shl 8)

            if (check != (blockLen.inv() and 0xFFFF) && !(blockLen == 0 && check == 0)) {
                throw Error("Bad uncompressed block length in flate stream")
            }

            codeBuf = 0
            codeSize = 0

            var bufferLength = bufferLength
            buffer = ensureBuffer(bufferLength + blockLen)
            val end = bufferLength + blockLen
            bufferLength = end

            if (blockLen == 0) {
                if (str.peekByte() == null) {
                    eof = true
                }
            } else {
                for (n in bufferLength until end) {
                    if (str.getByte().also { byte = it } == null) {
                        eof = true
                        break
                    }
                    buffer[n] = byte!!.toUByte()
                }
            }
            return
        }

        // Compressed block
        val litCodeTable: Pair<IntArray, Int>
        val distCodeTable: Pair<IntArray, Int>

        if (hdr == 1) {
            // Fixed Huffman codes
            litCodeTable = fixedLitCodeTab
            distCodeTable = fixedDistCodeTab
        } else if (hdr == 2) {
            // Dynamic Huffman codes
            val numLitCodes = getBits(5) + 257
            val numDistCodes = getBits(5) + 1
            val numCodeLenCodes = getBits(4) + 4

            // Build the code length table
            val codeLenCodeLengths = ByteArray(codeLenCodeMap.size)
            for (i in 0 until numCodeLenCodes) {
                codeLenCodeLengths[codeLenCodeMap[i]] = getBits(3).toByte()
            }
            val codeLenCodeTab = generateHuffmanTable(codeLenCodeLengths)

            // Build the literal and distance code tables
            len = 0
            var i = 0
            val codes = numLitCodes + numDistCodes
            val codeLengths = ByteArray(codes)

            while (i < codes) {
                when (val code = getCode(codeLenCodeTab)) {
                    16 -> {
                        val repeatLength = getBits(2) + 3
                        repeat(repeatLength) { codeLengths[i++] = len.toByte() }
                    }
                    17 -> {
                        val repeatLength = getBits(3) + 3
                        len = 0
                        repeat(repeatLength) { codeLengths[i++] = len.toByte() }
                    }
                    18 -> {
                        val repeatLength = getBits(7) + 11
                        len = 0
                        repeat(repeatLength) { codeLengths[i++] = len.toByte() }
                    }
                    else -> {
                        codeLengths[i++] = code.toByte()
                        len = code
                    }
                }
            }

            litCodeTable = generateHuffmanTable(codeLengths.copyOf(numLitCodes))
            distCodeTable = generateHuffmanTable(codeLengths.copyOfRange(numLitCodes, codes))
        } else {
            throw Error("Unknown block type in flate stream")
        }

        // Decompression loop
        buffer = this.buffer
        var limit = buffer?.size ?: 0
        var pos = bufferLength

        while (true) {
            var code1 = getCode(litCodeTable)
            when {
                code1 < 256 -> {
                    if (pos + 1 >= limit) {
                        buffer = ensureBuffer(pos + 1)
                        limit = buffer.size
                    }
                    buffer[pos++] = code1.toUByte()
                }
                code1 == 256 -> {
                    bufferLength = pos
                    return
                }
                else -> {
                    code1 -= 257
                    val lengthInfo = lengthDecode[code1]
                    val extraBits = lengthInfo shr 16
                    val length = (lengthInfo and 0xFFFF) + if (extraBits > 0) getBits(extraBits) else 0

                    var code2 = getCode(distCodeTable)
                    val distanceInfo = distDecode[code2]
                    val extraDistBits = distanceInfo shr 16
                    val dist = (distanceInfo and 0xFFFF) + if (extraDistBits > 0) getBits(extraDistBits) else 0

                    if (pos + length >= limit) {
                        buffer = ensureBuffer(pos + length)
                        limit = buffer.size
                    }

                    for (k in 0 until length) {
                        buffer[pos] = buffer[pos - dist]
                        pos++
                    }
                }
            }
        }
    }

    protected open fun getBits(numBits: Int): Int {
        var codeSize = this.codeSize
        var codeBuf = this.codeBuf

        while (codeSize < numBits) {
            val byte = stream.getByte()
            if (byte == null) {
                throw IllegalStateException("Bad encoding in flate stream")
            }

            codeBuf = codeBuf or (byte shl codeSize)
            codeSize += 8
        }

        val result = codeBuf and ((1 shl numBits) - 1)
        this.codeBuf = codeBuf shr numBits
        this.codeSize = codeSize - numBits

        return result
    }

    protected open fun getCode(table: Pair<IntArray, Int>): Int {
        val codes = table.first
        val maxLen = table.second
        val str = stream

        var codeSize = this.codeSize
        var codeBuf = this.codeBuf

        while (codeSize < maxLen) {
            val b = str.getByte()
            if (b == null) {
                // Premature end of stream. Code might still be valid.
                break
            }
            codeBuf = codeBuf or (b shl codeSize)
            codeSize += 8
        }

        val code = codes[codeBuf and ((1 shl maxLen) - 1)]
        val codeLen = code shr 16
        val codeVal = code and 0xFFFF

        if (codeLen < 1 || codeSize < codeLen) {
            throw Error("Bad encoding in flate stream")
        }

        this.codeBuf = codeBuf shr codeLen
        this.codeSize = codeSize - codeLen

        return codeVal
    }

    protected open fun generateHuffmanTable(lengths: ByteArray): Pair<IntArray, Int> {
        val n = lengths.size

        // Find max code length
        var maxLen = lengths.maxOrNull()?.toInt() ?: 0

        // Build the table
        val size = 1 shl maxLen
        val codes = IntArray(size)

        var code = 0
        var skip = 2

        for (len in 1..maxLen) {
            for (valIndex in lengths.indices) {
                if (lengths[valIndex].toInt() == len) {
                    // Bit-reverse the code
                    var code2 = 0
                    var t = code
                    for (i in 0 until len) {
                        code2 = (code2 shl 1) or (t and 1)
                        t = t shr 1
                    }

                    // Fill the table entries
                    for (i in code2 until size step skip) {
                        codes[i] = (len shl 16) or valIndex
                    }
                    code++
                }
            }
            code = code shl 1
            skip = skip shl 1
        }

        return Pair(codes, maxLen)
    }

}