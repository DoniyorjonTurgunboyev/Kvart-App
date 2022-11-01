package uz.gita.kvartarena.ui.adapters

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView
import uz.gita.kvartarena.R
import uz.gita.kvartarena.data.remote.FirebaseRemote
import uz.gita.kvartarena.model.Apartment
import uz.gita.kvartarena.utils.DiffUtils
import uz.gita.kvartarena.utils.inflate

class ApartAdapter(private val lifecycleOwner: LifecycleOwner) : ListAdapter<Apartment, ApartAdapter.VH>(DiffUtils<Apartment>()) {
    private lateinit var listener: (Apartment) -> Unit
    fun setListener(block: (Apartment) -> Unit) {
        listener = block
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private var name = view.findViewById<TextView>(R.id.name)
        private var image = view.findViewById<CircleImageView>(R.id.image)
        private var uid = view.findViewById<TextView>(R.id.uid)
        @SuppressLint("SetTextI18n")
        fun bind(apartment: Apartment) {
            itemView.setOnClickListener { listener.invoke(apartment) }
            image.setImageResource(R.drawable.ic_baseline_home_work_24)
            FirebaseRemote.getInstance().getImageCallback(apartment.uid.toString()) {
                image.setImageBitmap(BitmapFactory.decodeFile(it))
            }
            name.text = apartment.name
            uid.text = "id : ${apartment.uid}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(parent.inflate(R.layout.apart_item))

    override fun onBindViewHolder(holder: VH, position: Int) {
        return holder.bind(currentList[position])
    }
}