package at.searles.fract

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import at.searles.fract.editors.ScaleDialogFragment

class ParameterAdapter(private val activity: FractMainActivity): RecyclerView.Adapter<ParameterAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(activity).inflate(R.layout.simple_text, parent, false)
        return ViewHolder(view).also {
            view.setOnClickListener(it)
            view.setOnLongClickListener(it)
        }
    }

    override fun getItemCount(): Int {
        // FIXME for now only scale and source and one palette
        return 3
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when(position) {
            0 -> holder.textView.text = "Source Code"
            1 -> holder.textView.text = "Scale"
            2 -> holder.textView.text = "Palette"
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        val textView: TextView = itemView.findViewById<TextView>(R.id.textView).apply {
            setOnClickListener(this@ViewHolder)
        }

        override fun onClick(v: View) {
            when(adapterPosition) {
                0 -> activity.openScaleEditor()
                1 -> activity.openSourceEditor()
                2 -> activity.openPaletteEditor(0)
            }
        }

        override fun onLongClick(v: View?): Boolean {
            // TODO("not implemented")
            return false
        }
    }
}