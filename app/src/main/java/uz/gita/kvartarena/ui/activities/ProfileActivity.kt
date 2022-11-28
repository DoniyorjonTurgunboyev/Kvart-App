package uz.gita.kvartarena.ui.activities

import android.app.ProgressDialog
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import uz.gita.kvartarena.data.remote.FirebaseRemote
import uz.gita.kvartarena.databinding.ActivityProfileBinding
import uz.gita.kvartarena.model.User

class ProfileActivity : AppCompatActivity() {
    private lateinit var progressDialog: ProgressDialog
    private lateinit var binding: ActivityProfileBinding
    private lateinit var user: User
    private lateinit var uid: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Update data....")
        progressDialog.show()
        progressDialog.setCancelable(false)
        uid = intent.extras?.getString("uid")!!
        loadData()
        loadImage()
        binding.back.setOnClickListener {
            finish()
        }
    }

    private fun loadData() {
        user = intent.extras?.getSerializable("user") as User
        binding.name.text = user.name
        binding.fullname.text = user.name + " " + user.surname
        binding.surname.text = user.surname
        binding.telegram.text = user.telegram
        binding.address.text = user.address2?.replace(",", ",\n")
        binding.phone.text = user.number
        binding.hometown.text = user.address1?.replace(",", ",\n")
        binding.birthday.text = user.birthday
    }

    private fun loadImage() {
        FirebaseRemote.getInstance().getImageCallback(uid) {
            binding.circleImageView.setImageBitmap(BitmapFactory.decodeFile(it))
            progressDialog.dismiss()
        }
    }
}