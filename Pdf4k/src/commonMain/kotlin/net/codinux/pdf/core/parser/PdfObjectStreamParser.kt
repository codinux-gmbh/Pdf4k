package net.codinux.pdf.core.parser

import net.codinux.pdf.core.objects.*
import net.codinux.pdf.core.streams.StreamDecoder

/**
 * An object stream is a stream object in which a sequence of indirect objects may be stored, as an
 * alternative to their being stored at the outermost PDF file level. See PDF spec 7.5.7 Object streams.
 *
 * Object streams are first introduced in PDF 1.5. The purpose of object streams is to allow indirect
 * objects other than streams to be stored more compactly by using the facilities provided by
 * stream compression filters.
 *
 * The term "compressed object" is used regardless of whether the stream is actually encoded with
 * a compression filter.
 *
 * The following objects shall not be stored in an object stream:
 * - Stream objects
 * - Objects with a generation number other than zero
 * - A document’s encryption dictionary (see 7.6, "Encryption")
 * - An object representing the value of the Length entry in an object stream dictionary
 * - In linearized files (see Annex F, "Linearized PDF" and Annex G, "Linearized PDF access strategies"), the document
 * catalog dictionary, the linearization dictionary, and page objects shall not appear in an object stream.
 *
 * Indirect references to objects inside object streams use the normal syntax: for example, 14 0 R.
 * Access to these objects requires a different way of storing cross-reference information; see 7.5.8,
 * "Cross-reference streams". Use of compressed objects requires a PDF 1.5 PDF reader. However,
 * compressed objects can be stored in a manner that a PDF 1.4 PDF reader can ignore.
 *
 * In addition to the regular keys for streams shown in "Table 5 — Entries common to all stream
 * dictionaries", the stream dictionary describing an object stream contains the entries specified in “Table
 * 16 — Additional entries specific to an object stream dictionary”:
 *
 * | Key     | Type              | Value |
 * |---------|------------------|--------------------------------------------------------------------------------------------------------------------------------|
 * | Type    | name (Required)  | The type of PDF object that this dictionary describes; shall be ObjStm for an object stream. |
 * | N       | integer (Required)  | The number of indirect objects stored in the stream. |
 * | First   | integer (Required)  | The byte offset in the decoded stream of the first compressed object. |
 * | Extends | stream (Optional)  | A reference to another object stream, of which the current object stream is an extension. Both streams are considered part of a collection of object streams (see below). A given collection consists of a set of streams whose Extends links form a directed acyclic graph. |
 *
 * **N** pairs of integers separated by white-space, where the first integer in each pair shall represent
 * the object number of a compressed object and the second integer shall represent the byte offset in
 * the decoded stream of that object, relative to the first object stored in the object stream, the offset
 * for which is the value of the stream's **First** entry. The byte offsets shall be in increasing order. The
 * pairs, themselves, shall also be separated by white-space.
 *
 * The generation number of an object stream and of any compressed object shall be zero. If either an
 * object stream or a compressed object is deleted and the object number is freed, that object number
 * shall be reused only for an ordinary (uncompressed) object other than an object stream. When new
 * object streams and compressed objects are created, they shall always be assigned new object numbers,
 * not old ones taken from the free list.
 */
open class PdfObjectStreamParser(rawStream: PdfRawStream, decoder: StreamDecoder = StreamDecoder.Instance)
    : PdfObjectParser(ByteStream.fromPdfRawStream(rawStream, decoder)) { // TODO: use composition instead of inheritance

    protected val dict = rawStream.dict

    protected val firstOffset: Int = dict.getAs<PdfNumber>("First")?.value?.toInt()
        ?: throw IllegalStateException("PdfRawStream dictionary needs to contain key /Title with a PdfNumber value")

    protected val objectCount: Int = dict.getAs<PdfNumber>("N")?.value?.toInt()
            ?: throw IllegalStateException("PdfRawStream dictionary needs to contain key /N with a PdfNumber value")


    open fun parseIndirectObjects(referencePool: MutableMap<String, PdfRef>): List<Pair<PdfRef, PdfObject>> {
        val offsetsAndObjectNumbers = parseOffsetsAndObjectNumbers()

        return offsetsAndObjectNumbers.map { (objectNumber, offset) ->
            bytes.moveTo(firstOffset + offset)

            val `object` = parseObject()
            val ref = PdfRef.getOrCreate(referencePool, objectNumber, 0)

            Pair(ref, `object`)
        }
    }

    protected open fun parseOffsetsAndObjectNumbers(): List<Pair<Int, Int>> {
        return (0..<objectCount).map { index ->
            skipWhitespaceAndComments()
            val objectNumber = parseRawInt()

            skipWhitespaceAndComments()
            val offset = parseRawInt()

            Pair(objectNumber, offset)
        }
    }

}