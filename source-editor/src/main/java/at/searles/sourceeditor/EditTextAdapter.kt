package at.searles.sourceeditor

import android.text.Editable
import at.searles.parsingtools.formatter.EditableText

class EditTextAdapter(private val editable: Editable): EditableText, CharSequence by editable {
    override fun insert(position: Long, insertion: CharSequence) {
        editable.insert(position.toInt(), insertion)
    }

    override fun delete(start: Long, end: Long) {
        editable.delete(start.toInt(), end.toInt())
    }
}