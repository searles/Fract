package at.searles.fract.editors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import at.searles.fract.R

class SelectPalettesAdapter:
    RecyclerView.Adapter<SelectPalettesAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.parameter_bool_item, parent, false)
        val vh = ViewHolder(view)
        view.findViewById<CheckBox>(R.id.parameterNameCheckBox).apply {
            setOnCheckedChangeListener(vh)
        }

        //TODO("Not yet implemented")

        return vh
    }

    override fun getItemCount(): Int {
        //TODO("Not yet implemented")
        return 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // TODO("Not yet implemented")
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView),
        CompoundButton.OnCheckedChangeListener {
        val checkBox = itemView.findViewById<CheckBox>(R.id.parameterNameCheckBox)

        override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
            TODO("Not yet implemented")
        }
    }
}