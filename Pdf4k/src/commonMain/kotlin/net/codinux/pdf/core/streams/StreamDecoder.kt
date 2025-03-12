package net.codinux.pdf.core.streams

import net.codinux.pdf.core.UnexpectedObjectTypeError
import net.codinux.pdf.core.UnsupportedEncodingError
import net.codinux.pdf.core.objects.*

open class StreamDecoder {

    companion object {
        val Instance = StreamDecoder()
    }


    open fun decodePdfRawStream(rawStream: PdfRawStream): StreamType {
        var stream: StreamType = Stream(rawStream.contents)

        val filter = rawStream.dict.get(PdfName.Filter)
        val decodeParameter = rawStream.dict.get("DecodeParms") // TODO: DecodeParms can also be an indirect object

        return if (filter is PdfName) {
            decodeStream(stream, filter, decodeParameter as? PdfDict)
        } else if (filter is PdfArray) {
            filter.items.indices.forEach { index ->
                stream = decodeStream(stream, filter.items[index] as PdfName, (decodeParameter as? PdfArray)?.items?.get(index) as? PdfDict)
            }
            stream
        } else if (filter == null) {
            throw UnexpectedObjectTypeError(listOf(PdfName::class, PdfArray::class), if (filter == null) null else filter::class)
        } else {
            stream // TODO: is this a bug in pdf-lib that if filter is neither a PdfName nor a PdfArray that then stream is returned?
        }
    }

    open fun decodeStream(stream: StreamType, encoding: PdfName, params: PdfDict?): StreamType = when (encoding.name) {
        "FlateDecode" -> FlateStream(stream)
//        "LZWDecode" -> {
//            val earlyChange = params?.getAs<PdfNumber>("EarlyChange")?.value?.toInt() ?: 1
//            LZWStream(stream, null, earlyChange)
//        }
//        "ASCII85Decode" -> Ascii85Stream(stream)
//        "ASCIIHexDecode" -> AsciiHexStream(stream)
//        "RunLengthDecode" -> RunLengthStream(stream)
        else -> throw UnsupportedEncodingError(encoding.name)

    }

}