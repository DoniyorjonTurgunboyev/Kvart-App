package uz.gita.kvartarena.ui.activities

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import uz.gita.kvartarena.app.App
import uz.gita.kvartarena.data.local.EncryptedLocalStorage
import uz.gita.kvartarena.data.remote.FirebaseRemote
import uz.gita.kvartarena.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var progressDialog: ProgressDialog
    private lateinit var binding: ActivityMainBinding
    private val storage = EncryptedLocalStorage.getInstance()
    override fun onResume() {
        super.onResume()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Update data....")
        progressDialog.show()
        progressDialog.setCancelable(false)
        loadImage()
        loadData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apart.setOnClickListener {
            if (App.user.kid == "") {
                Toast.makeText(this, "Siz hali kvartiraga qo'shilmagansiz", Toast.LENGTH_SHORT).show()
            } else {
                startActivity(Intent(this, ApartmentActivity::class.java))
            }
        }
        binding.addApartment.setOnClickListener {
            if (App.user.kid == "")
                startActivity(Intent(this, CreateApartment::class.java))
            else {
                Toast.makeText(this, "Siz avval kvartira yaratgansiz", Toast.LENGTH_SHORT).show()
            }
        }
        binding.apartment.setOnClickListener {
            startActivity(Intent(this, ApartmentsActivity::class.java))
        }
        binding.profile.setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
        }
    }

    private fun loadData() {
        App.user.apply {
            binding.fullname.text = "$name $surname"
        }
    }

    private fun loadImage() {
        FirebaseRemote.getInstance().getImageCallback(storage.profile) {
            binding.image.setImageBitmap(BitmapFactory.decodeFile(it))
            progressDialog.dismiss()
            storage.profile = ""
        }
    }
}