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
import com.google.firebase.storage.FirebaseStorage
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import uz.gita.kvartarena.app.App
import uz.gita.kvartarena.data.local.EncryptedLocalStorage
import uz.gita.kvartarena.data.remote.FirebaseRemote
import uz.gita.kvartarena.databinding.ActivityUserProfileBinding
import uz.gita.kvartarena.model.User
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

class UserProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserProfileBinding
    private val REQUEST_CODE = 200
    private lateinit var imageUri: Uri
    private val u = App.user
    private val user = User(u.birthday, u.address1, u.name, u.surname, u.telegram, u.address2, u.kid, u.number)
    private lateinit var progressDialog: ProgressDialog
    private var rotation = 0
    private var changePhoto = false
    private var changeName = false
    private var changeSurname = false
    private var changeTelegram = false
    private lateinit var downsizedImageBytes: ByteArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadData()
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
        binding.circleImageView.setOnClickListener {
            checkP()
        }
        binding.save.setOnClickListener {
            if (changeName || changeSurname || changeTelegram) {
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
        if (changeTelegram || changeName || changePhoto || changeSurname) {
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
        binding.hometown.text = user.address1?.replace("/", ",\n")
        binding.phone.text = user.number
        binding.address.text = user.address2!!.replace("Oʻzbekiston, Toshkent, ", "").replaceFirst(",", ",\n")
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