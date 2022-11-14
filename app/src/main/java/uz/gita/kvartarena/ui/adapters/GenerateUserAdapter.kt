package uz.gita.kvartarena.ui.adapters

import android.graphics.BitmapFactory
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import uz.gita.kvartarena.R
import uz.gita.kvartarena.data.remote.FirebaseRemote
import uz.gita.kvartarena.model.ItemUserGenerate
import uz.gita.kvartarena.utils.DiffUtils
import uz.gita.kvartarena.utils.inflate
import java.util.*

class GenerateUserAdapter : ListAdapter<ItemUserGenerate, GenerateUserAdapter.VH>(DiffUtils<ItemUserGenerate>()) {
    private lateinit var listener: (String) -> Unit
    private lateinit var smooth: (Int) -> Unit
    fun setListener(block: (String) -> Unit) {
        listener = block
    }

    fun setSmooth(block: (Int) -> Unit) {
        smooth = block
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        var m = 0
        val item = view.findViewById<ConstraintLayout>(R.id.item)
        val image = view.findViewById<ImageView>(R.id.imageView)
        val name = view.findViewById<TextView>(R.id.name_text)
        val amount = view.findViewById<TextView>(R.id.amount)
        val more = view.findViewById<LinearLayout>(R.id.more)
        val send = view.findViewById<TextView>(R.id.send)
        val get = view.findViewById<TextView>(R.id.get)
        val show = view.findViewById<TextView>(R.id.view)
        fun bind(user: ItemUserGenerate) {
            if (absoluteAdapterPosition % 2 != 0) {
                item.setBackgroundResource(android.R.color.transparent)
            } else {
                item.setBackgroundResource(R.drawable.excel_back)
            }
            show.setOnClickListener {
                listener.invoke(user.uid)
            }
            val sumneg = String.format(Locale.US, "%,d", user.negAmount).replace(",", " ")
            val sumpos = String.format(Locale.US, "%,d", user.posAmount).replace(",", " ")
            send.text = "$sumneg UZS"
            get.text = "+$sumpos UZS"
            image.setImageResource(R.drawable.photo)
            FirebaseRemote.getInstance().getImageCallback(user.uid) {
                image.setImageBitmap(BitmapFactory.decodeFile(it))
            }
            more.visibility = if (user.visibility) View.VISIBLE else View.GONE
            itemView.setOnClickListener {
                if (m != absoluteAdapterPosition) {
                    currentList[m].visibility = false
                    submitList(currentList)
//                    notifyItemChanged(m)
                }
                if (more.visibility == View.VISIBLE) {
                    TransitionManager.beginDelayedTransition(itemView as ViewGroup, AutoTransition().setInterpolator(null))
                    currentList[absoluteAdapterPosition].visibility = false
                    more.visibility = View.GONE
                } else {
                    TransitionManager.beginDelayedTransition(itemView as ViewGroup, AutoTransition().setInterpolator(null))
                    more.visibility = View.VISIBLE
                    currentList[absoluteAdapterPosition].visibility = true
                    smooth.invoke(absoluteAdapterPosition)
                }
                m = absoluteAdapterPosition
            }
            name.text = user.name
            val sum = String.format(Locale.US, "%,d", user.amount).replace(",", " ")
            amount.text = "$sum UZS"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(parent.inflate(R.layout.generate_user_item))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(currentList[position])
}