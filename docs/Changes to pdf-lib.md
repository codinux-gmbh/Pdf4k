
- In class names `PDFXyz` has been renamed to `PdfXyz`.
- `PdfDocument` is now a pure data class with no logic like loading a PDF.
- `PDFDocument.load()` has been moved to `PdfParser.parseDocument()`.
- `PdfParser.parseDocument()` reads only the most elementary bytes of a PDF, every else lazily on demand. `PdfParser.parseDocumentEagerly()` applies the same behavior as pdf-lib's `PDFDocument.load()` by reading whole PDF byte by byte.
- `PdfDocument` is a pure readonly class. If there will ever be a PDF writer functionality, i'll create a `PdfBuilder` class.