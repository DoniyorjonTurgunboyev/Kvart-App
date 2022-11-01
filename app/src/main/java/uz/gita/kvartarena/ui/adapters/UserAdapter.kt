package uz.gita.kvartarena.ui.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView
import uz.gita.kvartarena.R
import uz.gita.kvartarena.data.remote.FirebaseRemote
import uz.gita.kvartarena.model.ItemUser
import uz.gita.kvartarena.utils.DiffUtils
import uz.gita.kvartarena.utils.inflate

class UserAdapter(private val context: Context) : ListAdapter<ItemUser, UserAdapter.VH>(DiffUtils<ItemUser>()) {
    private lateinit var checkListener: (Int) -> Unit
    private lateinit var unCheckListener: (Int) -> Unit

    fun setOnCheckListener(block: (Int) -> Unit) {
        checkListener = block
    }

    fun setUnCheckListener(block: (Int) -> Unit) {
        unCheckListener = block
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val name = view.findViewById<TextView>(R.id.name)
        val image = view.findViewById<CircleImageView>(R.id.image)
        val check = view.findViewById<CheckBox>(R.id.check)
        val item = view.findViewById<RelativeLayout>(R.id.user_item)
        fun bind(user: ItemUser) {
            FirebaseRemote.getInstance().getImageCallback(user.uid) {
                image.setImageBitmap(BitmapFactory.decodeFile(it))
            }
            fun checkAll() {
                if (check.isChecked) {
                    check.scaleX = 1f
                    check.scaleY = 1f
                    name.setTextColor(ContextCompat.getColor(context, R.color.check))
                    item.setBackgroundResource(R.drawable.item_user_back)
                    checkListener.invoke(absoluteAdapterPosition)
                } else {
                    check.scaleX = 0.8f
                    check.scaleY = 0.8f
                    name.setTextColor(ContextCompat.getColor(context, R.color.uncheck))
                    item.setBackgroundResource(R.drawable.item_user_back2)
                    unCheckListener.invoke(absoluteAdapterPosition)
                }
            }
            name.text = user.name
            check.isChecked = user.checked
            check.setOnClickListener {
                user.checked = check.isChecked
                checkAll()
            }
            item.setOnClickListener {
                check.isChecked = !check.isChecked
                user.checked = check.isChecked
                checkAll()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(parent.inflate(R.layout.user_item))

    override fun onBindViewHolder(holder: VH, position: Int) {
        return holder.bind(currentList[position])
    }
}