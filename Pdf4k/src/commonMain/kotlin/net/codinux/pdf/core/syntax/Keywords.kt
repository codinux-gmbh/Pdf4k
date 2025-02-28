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

    val Obj = byteArrayOf(CharCodes.o, CharCodes.b, CharCodes.j)

    val Endobj = byteArrayOf(
        CharCodes.e,
        CharCodes.n,
        CharCodes.d,
        CharCodes.o,
        CharCodes.b,
        CharCodes.j,
    )


    val Xref = byteArrayOf(CharCodes.x, CharCodes.r, CharCodes.e, CharCodes.f)

    val Trailer = byteArrayOf(
        CharCodes.t,
        CharCodes.r,
        CharCodes.a,
        CharCodes.i,
        CharCodes.l,
        CharCodes.e,
        CharCodes.r,
    )

    val StartXref = byteArrayOf(
        CharCodes.s,
        CharCodes.t,
        CharCodes.a,
        CharCodes.r,
        CharCodes.t,
        CharCodes.x,
        CharCodes.r,
        CharCodes.e,
        CharCodes.f,
    )


    val True = byteArrayOf(CharCodes.t, CharCodes.r, CharCodes.u, CharCodes.e)

    val False = byteArrayOf(CharCodes.f, CharCodes.a, CharCodes.l, CharCodes.s, CharCodes.e)

    val Null = byteArrayOf(CharCodes.n, CharCodes.u, CharCodes.l, CharCodes.l)


    val Stream = byteArrayOf(
        CharCodes.s,
        CharCodes.t,
        CharCodes.r,
        CharCodes.e,
        CharCodes.a,
        CharCodes.m,
    )

    val StreamEOF1 = byteArrayOf(*Stream, CharCodes.Space, CharCodes.CarriageReturn, CharCodes.Newline)
    val StreamEOF2 = byteArrayOf(*Stream, CharCodes.CarriageReturn, CharCodes.Newline)
    val StreamEOF3 = byteArrayOf(*Stream, CharCodes.CarriageReturn)
    val StreamEOF4 = byteArrayOf(*Stream, CharCodes.Newline)

    val Endstream = byteArrayOf(
        CharCodes.e,
        CharCodes.n,
        CharCodes.d,
        CharCodes.s,
        CharCodes.t,
        CharCodes.r,
        CharCodes.e,
        CharCodes.a,
        CharCodes.m,
    )

    val EOF1endstream = byteArrayOf(CharCodes.CarriageReturn, CharCodes.Newline, *Endstream)
    val EOF2endstream = byteArrayOf(CharCodes.CarriageReturn, *Endstream)
    val EOF3endstream = byteArrayOf(CharCodes.Newline, *Endstream)

}