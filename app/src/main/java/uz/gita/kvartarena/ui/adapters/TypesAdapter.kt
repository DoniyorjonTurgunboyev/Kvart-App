package uz.gita.kvartarena.ui.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import uz.gita.kvartarena.R
import uz.gita.kvartarena.model.Type
import uz.gita.kvartarena.utils.DiffUtils
import uz.gita.kvartarena.utils.inflate

class TypesAdapter : ListAdapter<Type, TypesAdapter.VH>(DiffUtils<Type>()) {
    private lateinit var listener: (Type) -> Unit
    fun setListener(block: (Type) -> Unit) {
        listener = block
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.image_view)
        val text: TextView = view.findViewById(R.id.text)
        fun bind(item: Type) {
            image.setImageResource(item.icon)
            text.text = item.type
            itemView.setOnClickListener { listener.invoke(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(parent.inflate(R.layout.item_type_spinner))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(currentList[position])
}