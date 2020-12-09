package at.searles.paletteeditor.colorsview

import android.content.ClipData
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import at.searles.commons.color.Colors
import at.searles.paletteeditor.R
import at.searles.paletteeditor.DefaultColors

class ColorsAdapter(private val context: Context) : RecyclerView.Adapter<ColorsAdapter.ViewHolder>() {
    val colors = DefaultColors.colors

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.colored_button, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return colors.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTo(position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val button = itemView.findViewById<Button>(R.id.coloredButton).apply {
            setOnLongClickListener { startDragging(it, colors[adapterPosition]); true }
        }

        fun bindTo(position: Int) {
            button.backgroundTintList = ColorStateList.valueOf(colors[position])
        }
    }

    private fun startDragging(view: View, color: Int) {
        val data = ClipData.newPlainText("colorString", Colors.toColorString(color))
        val shadowBuilder = View.DragShadowBuilder(view)
        view.startDragAndDrop(data, shadowBuilder, view, 0)
    }
}
