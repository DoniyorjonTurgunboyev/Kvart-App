package uz.gita.kvartarena.ui.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.storage.FirebaseStorage
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import uz.gita.kvartarena.R
import uz.gita.kvartarena.app.App
import uz.gita.kvartarena.data.local.EncryptedLocalStorage
import uz.gita.kvartarena.data.remote.FirebaseRemote
import uz.gita.kvartarena.databinding.ActivityUserProfileBinding
import uz.gita.kvartarena.model.User
import uz.gita.kvartarena.ui.adapters.LocationAdapter
import uz.gita.kvartarena.utils.resIdByName
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*

class UserProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserProfileBinding
    private val REQUEST_CODE = 200
    private lateinit var imageUri: Uri
    private val u = App.user
    private val user = User(u.birthday, u.address1, u.name, u.surname, u.telegram, u.address2, u.kid, u.number)
    private lateinit var progressDialog: ProgressDialog
    private var rotation = 0
    private var location = ""
    private var changePhoto = false
    private var changeName = false
    private var changeSurname = false
    private var changeTelegram = false
    private var changeAddress = false
    private lateinit var downsizedImageBytes: ByteArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadData()
        binding.field6.setOnClickListener {
            val list = resources.getStringArray(R.array.regions).toList()
            showBottomSheetDialog(list, 1)
        }
        binding.back.setOnClickListener { finish() }
        binding.name.addTextChangedListener {
            changeName = it.toString() != App.user.name
            user.name = it.toString()
            checkSave()
        }
        binding.surname.addTextChangedListener {
            changeSurname = it.toString() != App.user.surname
            user.surname = it.toString()
            checkSave()
        }
        binding.telegram.addTextChangedListener {
            changeTelegram = it.toString() != App.user.telegram
            user.telegram = it.toString()
            checkSave()
        }
        binding.address.addTextChangedListener {
            changeAddress = App.user.address2?.replace("/", "\n") != it.toString()
            user.address2 = it.toString().replace("\n", "/")
            checkSave()
        }
        binding.circleImageView.setOnClickListener {
            checkP()
        }
        binding.save.setOnClickListener {
            if (changeName || changeSurname || changeAddress || changeTelegram) {
                saveInfo()
            }
            if (changePhoto) {
                uploadImage()
            } else {
                finish()
            }
        }
    }

    private fun saveInfo() {
        FirebaseRemote.getInstance().updateUser(user)
        App.user = user
    }

    private fun openGalleryForImages() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE)
    }

    private fun checkP() {
        Dexter.withContext(this).withPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.MANAGE_DOCUMENTS)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                    openGalleryForImages()
                }

                override fun onPermissionRationaleShouldBeShown(p0: MutableList<PermissionRequest>?, p1: PermissionToken?) {
                    p1?.continuePermissionRequest()
                }
            }).check()
    }

    private fun checkSave() {
        if (changeTelegram || changeName || changeAddress || changePhoto || changeSurname) {
            binding.save.visibility = View.VISIBLE
        } else {
            binding.save.visibility = View.INVISIBLE
        }
    }

    private fun loadData() {
        val user = App.user
        binding.name.setText(user.name)
        binding.surname.setText(user.surname)
        binding.telegram.setText(user.telegram)
        binding.address.text = user.address2?.replace("/", ",\n")
        binding.phone.text = user.number
        binding.hometown.text = user.address1?.replace("/", ",\n")
        binding.birthday.text = user.birthday
        FirebaseRemote.getInstance().getImageCallback("") {
            binding.circleImageView.setImageBitmap(BitmapFactory.decodeFile(it))
        }
    }

    private fun uploadImage() {
        if (this::imageUri.isInitialized) {
            progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Uploading File....")
            progressDialog.show()
            progressDialog.setCancelable(false)
            FirebaseStorage.getInstance().getReference("images/" + EncryptedLocalStorage.getInstance().uid).putBytes(downsizedImageBytes)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Successfully", Toast.LENGTH_SHORT).show()
                    EncryptedLocalStorage.getInstance().profile = "1"
                    finish()
                }
        } else {
            Toast.makeText(this, "Please select image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showBottomSheetDialog(list: List<String>, type: Int) {
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(R.layout.bottomsheetdialog)
        val rv = bottomSheetDialog.findViewById<RecyclerView>(R.id.rvLocation)!!
        val adapter = LocationAdapter()
        rv.layoutManager = LinearLayoutManager(this)
        adapter.submitList(list)
        rv.adapter = adapter
        adapter.setListener {
            when (type) {
                1 -> {
                    location = it
                    val s = location.trim().replace("‘", "").replace(" ", "").toLowerCase(Locale.ROOT)
                    val list = resources.getStringArray(this@UserProfileActivity.resIdByName(s, "array")).toList()
                    showBottomSheetDialog(list, 2)
                }
                2 -> {
                    location += "/$it"
                    binding.address.text = location.replace("/", ",\n")
                    changeAddress = true
                    checkSave()
                }
            }
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {
            if (data?.clipData != null) {
                val count = data.clipData?.itemCount
                for (i in 0 until count!!) {
                    var imageUri: Uri = data.clipData?.getItemAt(i)!!.uri
                }
            } else if (data?.data != null) {
                imageUri = data.data!!
                val isn = contentResolver.openInputStream(imageUri) as InputStream
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val exifInterface = ExifInterface(isn)
                    rotation = 0
                    val orientation = exifInterface.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                    when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> rotation = 90
                        ExifInterface.ORIENTATION_ROTATE_180 -> rotation = 180
                        ExifInterface.ORIENTATION_ROTATE_270 -> rotation = 270
                    }
                }
                val fullBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
                val scaleDivider = 2
                val scaleWidth: Int = fullBitmap.width / scaleDivider
                val scaleHeight: Int = fullBitmap.height / scaleDivider
                downsizedImageBytes = getDownsizedImageBytes(fullBitmap, scaleWidth, scaleHeight)!!
                changePhoto = true
                checkSave()
            }
        }
    }

    @Throws(IOException::class)
    fun getDownsizedImageBytes(fullBitmap: Bitmap?, scaleWidth: Int, scaleHeight: Int): ByteArray? {
        val scaledBitmap = Bitmap.createScaledBitmap(fullBitmap!!, scaleWidth, scaleHeight, true)
        val baos = ByteArrayOutputStream()
        val matrix = Matrix()
        matrix.postRotate(rotation.toFloat())
        val rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, matrix, true)
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        binding.circleImageView.setImageBitmap(rotatedBitmap)
        return baos.toByteArray()
    }
}