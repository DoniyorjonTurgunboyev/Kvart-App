package uz.gita.kvartarena.ui.activities
//
//import android.os.Bundle
//import android.util.Log
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.karumi.dexter.Dexter
//import com.karumi.dexter.MultiplePermissionsReport
//import com.karumi.dexter.PermissionToken
//import com.karumi.dexter.listener.PermissionRequest
//import com.karumi.dexter.listener.multi.MultiplePermissionsListener
//import com.yandex.mapkit.Animation
//import com.yandex.mapkit.MapKitFactory
//import com.yandex.mapkit.geometry.Point
//import com.yandex.mapkit.layers.ObjectEvent
//import com.yandex.mapkit.location.LocationListener
//import com.yandex.mapkit.location.LocationStatus
//import com.yandex.mapkit.map.CameraPosition
//import com.yandex.mapkit.mapview.MapView
//import com.yandex.mapkit.user_location.UserLocationLayer
//import com.yandex.mapkit.user_location.UserLocationObjectListener
//import com.yandex.mapkit.user_location.UserLocationView
//import com.yandex.runtime.image.ImageProvider
//import uz.gita.kvartarena.R
//import uz.gita.kvartarena.databinding.ActivityGeolocationBinding
//
//
//class GeolocationActivity : AppCompatActivity() {
//    private lateinit var mapview: MapView
//    private lateinit var binding: ActivityGeolocationBinding
//    private lateinit var userLocation: UserLocationLayer
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        MapKitFactory.initialize(this)
//        binding = ActivityGeolocationBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//        mapview = binding.map
//        checkP()
//        location()
//        mapview.map.move(
//            CameraPosition(Point(41.311024, 69.243919), 20.0f, 0.0f, 0.0f),
//            Animation(Animation.Type.SMOOTH, 5f),
//            null
//        )
//        getSelectionPoint()
//    }
//
//    private fun getSelectionPoint() {
//        MapKitFactory.getInstance().createLocationManager().requestSingleUpdate(object : LocationListener {
//            override fun onLocationUpdated(p0: com.yandex.mapkit.location.Location) {
//                Toast.makeText(this@GeolocationActivity, "${p0.position.latitude}", Toast.LENGTH_SHORT).show()
//            }
//
//            override fun onLocationStatusUpdated(p0: LocationStatus) {
//                Toast.makeText(this@GeolocationActivity, p0.name, Toast.LENGTH_SHORT).show()
//            }
//
//        })
//    }
//
//    private fun location() {
//        mapview.map.mapObjects.addPlacemark(Point(41.311024, 69.243919))
//        var mapkit = MapKitFactory.getInstance()
//        val probki = mapkit.createTrafficLayer(mapview.mapWindow)
//        probki.isTrafficVisible = true
//        userLocation = mapkit.createUserLocationLayer(mapview.mapWindow)
//        userLocation.isVisible = true
//        userLocation.setTapListener {
//            mapview.map.move(
//                CameraPosition(it, 13.0f, 0.0f, 0.0f),
//                Animation(Animation.Type.SMOOTH, 5f),
//                null
//            )
//        }
//        mapview.setOnClickListener {
//            Toast.makeText(this, "${(it as MapView).focusPoint.x}", Toast.LENGTH_SHORT).show()
//        }
//        userLocation.setObjectListener(object : UserLocationObjectListener {
//            override fun onObjectAdded(p0: UserLocationView) {
//                Log.d("TTT", "onObjectAdded: ${p0.arrow.geometry}")
//                p0.arrow.setIcon(ImageProvider.fromResource(this@GeolocationActivity, R.drawable.navigation))
//                mapview.map.move(
//                    CameraPosition(p0.arrow.geometry, 13.0f, 0.0f, 0.0f),
//                    Animation(Animation.Type.SMOOTH, 5f),
//                    null
//                )
//            }
//
//            override fun onObjectRemoved(p0: UserLocationView) {
//
//            }
//
//            override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {
//                Log.d("TTT", "onObjectAdded: ${p0.arrow.geometry}")
//                mapview.map.move(
//                    CameraPosition(p0.arrow.geometry, 13.0f, 0.0f, 0.0f),
//                    Animation(Animation.Type.SMOOTH, 5f),
//                    null
//                )
//            }
//
//        })
//        userLocation.isHeadingEnabled = true
//    }
//
//    private fun checkP() {
//        Dexter.withContext(this).withPermissions(android.Manifest.permission.ACCESS_FINE_LOCATION)
//            .withListener(object : MultiplePermissionsListener {
//                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
////                    location()
//                }
//
//                override fun onPermissionRationaleShouldBeShown(p0: MutableList<PermissionRequest>?, p1: PermissionToken?) {
//                    p1?.continuePermissionRequest()
//                }
//            }).check()
//    }
//
//    override fun onStop() {
//        mapview.onStop()
//        MapKitFactory.getInstance().onStop()
//        super.onStop()
//    }
//
//    override fun onStart() {
//        super.onStart()
//        MapKitFactory.getInstance().onStart()
//        mapview.onStart()
//    }
//}