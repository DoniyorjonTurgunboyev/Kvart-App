package uz.gita.kvartarena.ui.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.storage.FirebaseStorage
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.niwattep.materialslidedatepicker.SlideDatePickerDialog
import com.niwattep.materialslidedatepicker.SlideDatePickerDialogCallback
import uz.gita.kvartarena.R
import uz.gita.kvartarena.app.App
import uz.gita.kvartarena.data.local.EncryptedLocalStorage
import uz.gita.kvartarena.data.remote.FirebaseRemote
import uz.gita.kvartarena.databinding.ActivitySettingsBinding
import uz.gita.kvartarena.model.User
import uz.gita.kvartarena.notifications.FirebaseCloudMessaging
import uz.gita.kvartarena.ui.adapters.LocationAdapter
import uz.gita.kvartarena.utils.resIdByName
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class SettingsActivity : AppCompatActivity(), SlideDatePickerDialogCallback {
    private val REQUEST_CODE = 200
    private var rotation: Int = 0
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var imageUri: Uri
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var progressDialog: ProgressDialog
    private lateinit var downsizedImageBytes: ByteArray
    private var bRegion: String = ""
    private var bDistrict: String = ""
    private val storage = EncryptedLocalStorage.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        clickLocations()
        binding.number.setText(storage.numberF)
        firebaseStorage = FirebaseStorage.getInstance()
        val c = Calendar.getInstance()
        c.set(2000, 0, 1)
        val datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Select date").setSelection(c.timeInMillis).setTheme(R.style.ThemeOverlay_App_DatePicker).build()
        datePicker.addOnPositiveButtonClickListener {
            val trlocale = Locale("uz-UZ")
            val simple = SimpleDateFormat("dd.MM.yyyy", trlocale)
            val calendar = Calendar.getInstance(trlocale)
            val month: String = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()) as String
            Toast.makeText(this, month, Toast.LENGTH_SHORT).show()
            val result = Date(it)
            binding.birthday.text = simple.format(result)
        }
        binding.fiald3.setOnClickListener {
            datePicker()
        }
        binding.circleImageView.setOnClickListener { checkP() }
        binding.save.setOnClickListener {
            if (bRegion == "" || bDistrict == "") return@setOnClickListener
            uploadImage()
        }
    }

    private fun clickLocations() {
        binding.regionHome.setOnClickListener {
            val list = resources.getStringArray(R.array.regions).toList()
            showBottomSheetDialog(list, 1)
        }
        binding.districtHome.setOnClickListener {
            if (bRegion != "") {
                val s = bRegion.trim().replace("‘", "").replace(" ", "").toLowerCase(Locale.ROOT)
                val list = resources.getStringArray(this@SettingsActivity.resIdByName(s, "array")).toList()
                showBottomSheetDialog(list, 2)
            }
        }
    }

    private fun datePicker() {
        val endDate = Calendar.getInstance()
        endDate[Calendar.YEAR] = 2015
        val startDate = Calendar.getInstance()
        startDate[Calendar.YEAR] = 1950
        val selectedDate = Calendar.getInstance()
        selectedDate[Calendar.YEAR] = 2000
        val builder = SlideDatePickerDialog.Builder()
        builder.setEndDate(endDate)
        builder.setStartDate(startDate)
        builder.setPreselectedDate(selectedDate)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.setThemeColor(getColor(R.color.green))
        }
        val dialog: SlideDatePickerDialog = builder.build()
        dialog.show(supportFragmentManager, "Dialog")
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
                    (binding.regionHome.getChildAt(0) as TextView).text = it
                    bRegion = it
                    binding.districtHome.isClickable = true
                }
                2 -> {
                    (binding.districtHome.getChildAt(0) as TextView).text = it
                    bDistrict = it
                }
            }
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }

    private fun saveInfo(token: String) {
        val name = binding.name.text.toString()
        val surname = binding.surname.text.toString()
        val address = ""
        FirebaseRemote.getInstance().createUser(
            User(
                birthday = binding.birthday.text.toString(),
                "$bRegion/$bDistrict",
                name, surname, telegram = binding.telegram.text.toString(),
                address,
                number = binding.number.text.toString(),
                token = token
            )
        )
        startActivity(Intent(this, SplashActivity::class.java))
        finishAffinity()
        storage.settings = true
    }

    private fun openGalleryForImages() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE)
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
                    val exifInterface: ExifInterface =
                        ExifInterface(isn)
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
        rotatedBitmap.compress(CompressFormat.JPEG, 80, baos)
        binding.circleImageView.setImageBitmap(rotatedBitmap)
        return baos.toByteArray()
    }

    private fun uploadImage() {
        if (this::imageUri.isInitialized) {
            progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Uploading File....")
            progressDialog.show()
            progressDialog.setCancelable(false)
            firebaseStorage.getReference("images/" + storage.uid).putBytes(downsizedImageBytes)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    App.initUser()
                    FirebaseCloudMessaging.initToken {
                        saveInfo(it)
                    }
                }
        } else {
            Toast.makeText(this, "Please select image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkP() {
        Log.d("TTT", "checkP: ")
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

    override fun onPositiveClick(day: Int, month: Int, year: Int, calendar: Calendar) {
        val format = SimpleDateFormat("dd.MM.yyyy", Locale("uz", "UZ"))
        binding.birthday.text = format.format(calendar.time)
    }
}