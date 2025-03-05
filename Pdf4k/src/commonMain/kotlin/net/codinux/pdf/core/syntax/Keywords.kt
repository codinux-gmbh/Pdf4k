package net.codinux.pdf.core.syntax

@OptIn(ExperimentalUnsignedTypes::class)
object Keywords {

    val Header = ubyteArrayOf(
        CharCodes.Percent,
        CharCodes.P,
        CharCodes.D,
        CharCodes.F,
        CharCodes.Dash,
    )

    val EOF = ubyteArrayOf(
        CharCodes.Percent,
        CharCodes.Percent,
        CharCodes.E,
        CharCodes.O,
        CharCodes.F,
    )

    val Obj = ubyteArrayOf(CharCodes.o, CharCodes.b, CharCodes.j)

    val Endobj = ubyteArrayOf(
        CharCodes.e,
        CharCodes.n,
        CharCodes.d,
        CharCodes.o,
        CharCodes.b,
        CharCodes.j,
    )


    val Xref = ubyteArrayOf(CharCodes.x, CharCodes.r, CharCodes.e, CharCodes.f)

    val Trailer = ubyteArrayOf(
        CharCodes.t,
        CharCodes.r,
        CharCodes.a,
        CharCodes.i,
        CharCodes.l,
        CharCodes.e,
        CharCodes.r,
    )

    val StartXref = ubyteArrayOf(
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


    val True = ubyteArrayOf(CharCodes.t, CharCodes.r, CharCodes.u, CharCodes.e)

    val False = ubyteArrayOf(CharCodes.f, CharCodes.a, CharCodes.l, CharCodes.s, CharCodes.e)

    val Null = ubyteArrayOf(CharCodes.n, CharCodes.u, CharCodes.l, CharCodes.l)


    val Stream = ubyteArrayOf(
        CharCodes.s,
        CharCodes.t,
        CharCodes.r,
        CharCodes.e,
        CharCodes.a,
        CharCodes.m,
    )

    val StreamEOF1 = ubyteArrayOf(*Stream, CharCodes.Space, CharCodes.CarriageReturn, CharCodes.Newline)
    val StreamEOF2 = ubyteArrayOf(*Stream, CharCodes.CarriageReturn, CharCodes.Newline)
    val StreamEOF3 = ubyteArrayOf(*Stream, CharCodes.CarriageReturn)
    val StreamEOF4 = ubyteArrayOf(*Stream, CharCodes.Newline)

    val Endstream = ubyteArrayOf(
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

    val EOF1endstream = ubyteArrayOf(CharCodes.CarriageReturn, CharCodes.Newline, *Endstream)
    val EOF2endstream = ubyteArrayOf(CharCodes.CarriageReturn, *Endstream)
    val EOF3endstream = ubyteArrayOf(CharCodes.Newline, *Endstream)

}