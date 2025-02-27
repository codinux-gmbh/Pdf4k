package net.codinux.pdf.core.syntax

object Keywords {

    val Header = byteArrayOf(
        CharCodes.Percent,
        CharCodes.P,
        CharCodes.D,
        CharCodes.F,
        CharCodes.Dash,
    )

    val EOF = byteArrayOf(
        CharCodes.Percent,
        CharCodes.Percent,
        CharCodes.E,
        CharCodes.O,
        CharCodes.F,
    )

    val obj = byteArrayOf(CharCodes.o, CharCodes.b, CharCodes.j)

    val endobj = byteArrayOf(
        CharCodes.e,
        CharCodes.n,
        CharCodes.d,
        CharCodes.o,
        CharCodes.b,
        CharCodes.j,
    )

}