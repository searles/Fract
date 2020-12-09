package at.searles.fractlang.extensions

import android.content.res.Resources
import android.graphics.Typeface
import android.text.Editable
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import at.searles.buf.Frame
import at.searles.fractlang.parsing.Annot
import at.searles.fractlang.parsing.FractlangGrammar
import at.searles.parsing.ParserStream
import at.searles.sourceeditor.R
import at.searles.sourceeditor.SourceObserver
import kotlin.math.max

class FractlangObserver(private val resources: Resources): SourceObserver {
    override fun onToken(src: Editable, tokenId: Int, frame: Frame) {
        when (tokenId) {
            FractlangGrammar.multilineCommentId ->
                comment(src, frame.start, frame.end)
            FractlangGrammar.singlelineCommentId ->
                comment(src, frame.start, frame.end)
        }
    }

    override fun onAnnotation(src: Editable, annotation: Any?, stream: ParserStream) {
        if (stream.start == stream.end) {
            return
        }

        when (annotation) {
            Annot.Comma -> comma(src, stream.start, stream.end)
            Annot.DefKeyword -> declarationKeyword(src, stream.start, stream.end)
            Annot.Keyword -> keyword(src, stream.start, stream.end)
            // TODO Annot.Num -> number(src, stream.start, stream.end)
            Annot.Str -> string(src, stream.start, stream.end)
        }
    }

    override fun onMissingEof(src: Editable, stream: ParserStream) {
        val end = src.length
        error(src, stream.end, end.toLong())
    }

// TODO    override fun onParserError(src: Editable, e: ParserLookaheadException) {
//        error(src, min((src.length - 1).toLong(), e.unexpectedTokenStart),
//                max(src.length.toLong(), e.unexpectedTokenEnd))
//    }

    fun error(src: Editable, start: Long, end: Long) {
        src.setSpan(BackgroundColorSpan(toColor(R.color.redBackgroundColor)), start.toInt(), end.toInt(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
    }

    private fun toColor(resourceId: Int): Int {
        return resources.getColor(resourceId, null)
    }

    override fun onUnexpectedEnd(src: Editable, stream: ParserStream) {
        val end = src.length
        error(src, max(0, stream.offset - 1), end.toLong())
    }

    private fun string(src: Editable, start: Long, end: Long) {
        src.setSpan(ForegroundColorSpan(toColor(R.color.blueTextColor)), start.toInt(), end.toInt(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private fun number(src: Editable, start: Long, end: Long) {
        src.setSpan(ForegroundColorSpan(toColor(R.color.greenTextColor)), start.toInt(), end.toInt(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private fun keyword(src: Editable, start: Long, end: Long) {
        src.setSpan(ForegroundColorSpan(toColor(R.color.violetTextColor)), start.toInt(), end.toInt(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        src.setSpan(StyleSpan(Typeface.BOLD), start.toInt(), end.toInt(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private fun declarationKeyword(src: Editable, start: Long, end: Long) {
        src.setSpan(ForegroundColorSpan(toColor(R.color.orangeTextColor)), start.toInt(), end.toInt(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        src.setSpan(StyleSpan(Typeface.BOLD), start.toInt(), end.toInt(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private fun comma(src: Editable, start: Long, end: Long) {
        src.setSpan(StyleSpan(Typeface.BOLD), start.toInt(), end.toInt(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private fun comment(src: Editable, start: Long, end: Long) {
        src.setSpan(StyleSpan(Typeface.ITALIC), start.toInt(), end.toInt(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}