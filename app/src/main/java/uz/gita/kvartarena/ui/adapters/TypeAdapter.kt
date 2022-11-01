package uz.gita.kvartarena.ui.adapters

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import uz.gita.kvartarena.R
import uz.gita.kvartarena.model.Type
import uz.gita.kvartarena.utils.inflate

class TypeAdapter(val list: List<Type>) : BaseAdapter() {
    override fun getCount() = list.size

    override fun getItem(position: Int) = list[position]

    override fun getItemId(position: Int) = position.toLong()

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rootView = parent!!.inflate(R.layout.item_type_spinner)
        val image = rootView.findViewById<ImageView>(R.id.image_view)
        val text = rootView.findViewById<TextView>(R.id.text)
        image.setImageResource(list[position].icon)
        text.text = list[position].type
        return rootView
    }
}