package at.searles.sourceeditor

import android.text.Editable
import at.searles.parsing.format.EditableText


class EditTextAdapter(private val editable: Editable): EditableText, CharSequence by editable {
    override fun insert(position: Long, insertion: CharSequence) {
        editable.insert(position.toInt(), insertion)
    }

    override fun delete(position: Long, length: Long) {
        editable.delete(position.toInt(), (position + length).toInt())
    }
}