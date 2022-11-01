package uz.gita.kvartarena.ui.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import uz.gita.kvartarena.app.App
import uz.gita.kvartarena.data.local.EncryptedLocalStorage
import uz.gita.kvartarena.data.remote.FirebaseRemote
import uz.gita.kvartarena.databinding.ActivityCreateApartmentBinding
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

class CreateApartment : AppCompatActivity() {
    private var rotation: Int = 0
    private lateinit var downsizedImageBytes: ByteArray
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var progressDialog: ProgressDialog
    private val storage = EncryptedLocalStorage.getInstance()
    private lateinit var imageUri: Uri
    private val REQUEST_CODE = 200
    private lateinit var id: String
    private val user = App.user
    private lateinit var binding: ActivityCreateApartmentBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateApartmentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseStorage = FirebaseStorage.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        binding.address.text = App.user.address2!!.replace("/", "\n")
        binding.upload.setOnClickListener { checkP() }
        binding.back.setOnClickListener { finish() }
        binding.owner.text = "${user.name} ${user.surname}"
        binding.create.setOnClickListener {
            id = String.format("%06d", ((System.currentTimeMillis() / 1000) % 1000000).toInt())
            saveInfo()
        }
    }

    private fun uploadImage() {
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Uploading File....")
        progressDialog.show()
        progressDialog.setCancelable(false)
        firebaseStorage.getReference("images/$id").putBytes(downsizedImageBytes)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Successfully added", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun saveInfo() {
        val map = HashMap<String, Any>()
        binding.name.apply {
            if (text.toString().trim() == "") {
                this.error = "Nomini kiriting"
                return
            } else {
                map["name"] = binding.name.text.toString().trim()
            }
        }
        binding.bio.apply {
            if (text.toString().trim() == "") {
                this.error = "Ma'lumot kiriting"
                return
            } else {
                map["bio"] = binding.bio.text.toString().trim()
            }
        }
        if (!this::imageUri.isInitialized) {
            Toast.makeText(this, "Please choose image", Toast.LENGTH_SHORT).show()
            return
        }
        uploadImage()
        map["owner"] = storage.uid
        map["ownername"] = binding.owner.text.toString()
        map["address"] = App.user.address2!!
        firebaseDatabase.getReference("Apartments").child(id)
            .setValue(map)
            .addOnSuccessListener {
                FirebaseRemote.getInstance().addMemberToApartment(id)
            }
    }

    private fun openGalleryForImages() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {
            // if multiple images are selected
            if (data?.clipData != null) {
                var count = data.clipData?.itemCount
                for (i in 0 until count!!) {
                    var imageUri: Uri = data.clipData?.getItemAt(i)!!.uri
                }
            } else if (data?.data != null) {
                imageUri = data.data!!
                val isn = contentResolver.openInputStream(imageUri) as InputStream
                val exifInterface: ExifInterface = ExifInterface(isn)
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
                val fullBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)

                Log.d("DDD", "uploadImage: ${fullBitmap.byteCount}")
                val scaleDivider = 4
                val scaleWidth: Int = fullBitmap.width / scaleDivider
                val scaleHeight: Int = fullBitmap.height / scaleDivider
                downsizedImageBytes = getDownsizedImageBytes(fullBitmap, scaleWidth, scaleHeight)!!
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
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos)
        binding.image.setImageBitmap(rotatedBitmap)
        return baos.toByteArray()
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
}