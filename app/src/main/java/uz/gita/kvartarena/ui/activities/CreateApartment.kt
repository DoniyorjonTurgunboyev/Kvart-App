package uz.gita.kvartarena.ui.activities

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.location.LocationManager
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.Transformation
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
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.mapview.MapView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Response
import uz.gita.kvartarena.app.App
import uz.gita.kvartarena.data.local.EncryptedLocalStorage
import uz.gita.kvartarena.data.remote.FirebaseRemote
import uz.gita.kvartarena.data.remote.GeoCoderRetrofit
import uz.gita.kvartarena.databinding.ActivityCreateApartmentBinding
import uz.gita.kvartarena.model.geocoder.GeoApi
import java.io.*

class CreateApartment : AppCompatActivity(), CameraListener {
    private lateinit var mapView: MapView
    private lateinit var mapKit: MapKit
    private var margin = 4
    private var s = true
    private var lat: Double = 0.0
    private var long: Double = 0.0
    lateinit var locationManager: LocationManager
    private val geoService = GeoCoderRetrofit.getRetrofit()
    private var rotation: Int = 0
    private lateinit var downsizedImageBytes: ByteArray
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var progressDialog: ProgressDialog
    private val storage = EncryptedLocalStorage.getInstance()
    private lateinit var imageUri: Uri
    private val REQUEST_CODE = 200
    private var change = false
    private lateinit var id: String
    private val user = App.user
    private lateinit var binding: ActivityCreateApartmentBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateApartmentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadMap()
        binding.myLocation.setOnClickListener {
            geocoder()
        }
        binding.circleImageView.setOnClickListener {
            checkP()
        }
        binding.back.setOnClickListener { finish() }
        firebaseStorage = FirebaseStorage.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        binding.create.setOnClickListener {
            id = String.format("%06d", ((System.currentTimeMillis() / 1000) % 1000000).toInt())
            binding.address
            val bitmap = getScreenShotFromView(binding.cardView3)
            saveMediaToStorage(bitmap!!)
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
                App.initApart()
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
        if (!this::imageUri.isInitialized) {
            Toast.makeText(this, "Please choose image", Toast.LENGTH_SHORT).show()
            return
        }
        map["owner"] = storage.uid
        map["ownername"] = user.name!!
        map["lat"] = lat
        map["long"] = long
        map["address"] = binding.address.text.toString()
        if (map["address"].toString().isEmpty()) {
            Toast.makeText(this, "Avval manzilni tasdiqlang", Toast.LENGTH_SHORT).show()
            return
        }
        uploadImage()
        App.user.address2 = map["address"].toString()
        FirebaseRemote.getInstance().updateUser(App.user)
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
            if (data?.clipData != null) {
                var count = data.clipData?.itemCount
                for (i in 0 until count!!) {
                    var imageUri: Uri = data.clipData?.getItemAt(i)!!.uri
                }
            } else if (data?.data != null) {
                imageUri = data.data!!
                val isn = contentResolver.openInputStream(imageUri) as InputStream
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
                val fullBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
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
        val rotatedBitmap = Bitmap.createBitmap(
            scaledBitmap,
            0,
            0,
            scaledBitmap.width,
            scaledBitmap.height,
            matrix,
            true
        )
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos)
        binding.circleImageView.setImageBitmap(rotatedBitmap)
        return baos.toByteArray()
    }

    private fun checkP() {
        Dexter.withContext(this).withPermissions(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.MANAGE_DOCUMENTS
        )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                    openGalleryForImages()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    p1?.continuePermissionRequest()
                }
            }).check()
    }

    private fun loadMap() {
        mapKit = MapKitFactory.getInstance()
        mapView = binding.map
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
        val param = binding.imageView3.layoutParams as ViewGroup.MarginLayoutParams
        margin = param.bottomMargin
        mapView.map.addCameraListener(this)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        mapView.map.move(
            CameraPosition(Point(41.311299, 69.279770), 14f, 0f, 0f),
            Animation(Animation.Type.SMOOTH, 2f),
            null
        )
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onStart() {
        mapKit.onStart()
        mapView.onStart()
        super.onStart()
    }

    override fun onCameraPositionChanged(
        p0: Map,
        p1: CameraPosition,
        p2: CameraUpdateReason,
        p3: Boolean
    ) {
        val dpRatio: Float = this.resources.displayMetrics.density
        if (p3) {
            change = true
            s = true
            val animation = object : android.view.animation.Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                    val param = binding.imageView3.layoutParams as ViewGroup.MarginLayoutParams
                    param.bottomMargin = (12 * dpRatio - 8 * interpolatedTime * dpRatio).toInt()
                    binding.imageView3.layoutParams = param
                }
            }
            animation.duration = 200
            binding.imageView3.startAnimation(animation)
            val animation2 = object : android.view.animation.Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                    val param = binding.imageView4.layoutParams
                    param.width = ((16 + 16 * interpolatedTime) * dpRatio).toInt()
                    param.height = ((4 + 4 * interpolatedTime) * dpRatio).toInt()
                    binding.imageView4.alpha = 100 - 50 * interpolatedTime
                }
            }
            animation2.duration = 200
            binding.imageView4.startAnimation(animation2)
        } else
            if (s) {
                s = false
                val animation = object : android.view.animation.Animation() {
                    override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                        val param = binding.imageView3.layoutParams as ViewGroup.MarginLayoutParams
                        param.bottomMargin = (4 * dpRatio + 8 * interpolatedTime * dpRatio).toInt()
                        binding.imageView3.layoutParams = param
                    }
                }
                animation.duration = 200
                binding.imageView3.startAnimation(animation)
                val animation2 = object : android.view.animation.Animation() {
                    override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                        val param = binding.imageView4.layoutParams
                        param.width = ((32 - 16 * interpolatedTime) * dpRatio).toInt()
                        param.height = ((8 - 4 * interpolatedTime) * dpRatio).toInt()
                        binding.imageView4.alpha = 50 + 50 * interpolatedTime
                    }
                }
                animation2.duration = 200
                binding.imageView4.startAnimation(animation2)
            }
        if (p3) {
            lat = p1.target.latitude
            long = p1.target.longitude
        }
    }

    private fun geocoder() {
        CoroutineScope(Dispatchers.IO).launch {
            val geo = "$long,$lat"
            if (change) {
                runOnUiThread {
                    binding.loading.visibility = View.VISIBLE
                    binding.address.text = "Loading..."
                }
                delay(1000)
                val call: retrofit2.Call<GeoApi> = geoService.get("json", "Your Key", "uz", geo)
                call.enqueue(object : retrofit2.Callback<GeoApi> {
                    override fun onResponse(
                        call: retrofit2.Call<GeoApi>,
                        response: Response<GeoApi>
                    ) {
                        val t = response.body().toString()
                        runOnUiThread {
                            binding.address.text =
                                t.substring(t.indexOf("text") + 5, t.indexOf("kind"))
                            binding.loading.visibility = View.INVISIBLE
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<GeoApi>, t: Throwable) {

                    }
                })
                change = false
            }
        }
    }

    private fun getScreenShotFromView(v: View): Bitmap? {
        // create a bitmap object
        var screenshot: Bitmap? = null
        try {
            // inflate screenshot object
            // with Bitmap.createBitmap it
            // requires three parameters
            // width and height of the view and
            // the background color
            screenshot =
                Bitmap.createBitmap(v.measuredWidth, v.measuredHeight, Bitmap.Config.ARGB_8888)
            // Now draw this bitmap on a canvas
            val canvas = Canvas(screenshot)
            v.draw(canvas)
        } catch (e: Exception) {
            Log.e("TTT", "Failed to capture screenshot because:" + e.message)
        }
        // return the bitmap
        return screenshot
    }


    // this method saves the image to gallery
    private fun saveMediaToStorage(bitmap: Bitmap) {
        // Generating a file name
        val filename = "${System.currentTimeMillis()}.jpg"

        // Output stream
        var fos: OutputStream? = null

        // For devices running android >= Q
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // getting the contentResolver
            this.contentResolver?.also { resolver ->

                // Content resolver will process the contentvalues
                val contentValues = ContentValues().apply {

                    // putting file information in content values
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                // Inserting the contentValues to
                // contentResolver and getting the Uri
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                // Opening an outputstream with the Uri that we got
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            // These for devices running on android < Q
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }

        fos?.use {
            // Finally writing the bitmap to the output stream that we opened
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(this, "Captured View and saved to Gallery", Toast.LENGTH_SHORT).show()
        }
    }
}