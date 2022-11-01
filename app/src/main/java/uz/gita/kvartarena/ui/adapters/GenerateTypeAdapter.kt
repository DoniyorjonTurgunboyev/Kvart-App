package uz.gita.kvartarena.ui.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Space
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import uz.gita.kvartarena.R
import uz.gita.kvartarena.model.Type
import uz.gita.kvartarena.utils.DiffUtils
import uz.gita.kvartarena.utils.inflate
import java.util.*

class GenerateTypeAdapter(val all: Int) : ListAdapter<Type, GenerateTypeAdapter.VH>(DiffUtils<Type>()) {
    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val item = view.findViewById<ConstraintLayout>(R.id.item)
        val image = view.findViewById<ImageView>(R.id.imageView)
        val typeT = view.findViewById<TextView>(R.id.type_text)
        val space = view.findViewById<Space>(R.id.space2)
        val percent = view.findViewById<TextView>(R.id.percent)
        val amount = view.findViewById<TextView>(R.id.amount)
        fun bind(type: Type) {
            if (absoluteAdapterPosition % 2 != 0) {
                item.setBackgroundResource(android.R.color.transparent)
            } else {
                item.setBackgroundResource(R.drawable.excel_back)
            }
            image.setImageResource(type.icon)
            space.updateLayoutParams<ConstraintLayout.LayoutParams> {
                horizontalBias = (type.amount / all.toFloat())
            }
            val sum = String.format(Locale.US, "%,d", type.amount).replace(",", " ")
            percent.text = "${(100 * type.amount / all)}%"
            amount.text = "$sum UZS"
            typeT.text = type.type
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(parent.inflate(R.layout.generate_type_item))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(currentList[position])
}