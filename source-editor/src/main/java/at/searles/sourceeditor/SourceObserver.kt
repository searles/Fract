package at.searles.sourceeditor

import android.text.Editable
import at.searles.buf.Frame
import at.searles.parsing.ParserStream

interface SourceObserver {
    fun onToken(src: Editable, tokenId: Int, frame: Frame)
    fun onAnnotation(src: Editable, annotation: Any?, stream: ParserStream)
    fun onMissingEof(src: Editable, stream: ParserStream)
    // TODO fun onParserError(src: Editable, e: ParserLookaheadException)
    fun onUnexpectedEnd(src: Editable, stream: ParserStream)
}