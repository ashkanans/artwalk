package io.ashkanans.artwalk

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.SphericalUtil
import io.ashkanans.artwalk.databinding.ActivityMapsBinding
import kotlin.math.atan2


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationManager: LocationManager
    private var currentLocation: Location? = null

    private val requestcode = 1001 // You should define your request code here
    private val updateInterval = 5000L // Update every 5 seconds

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var permission: Boolean = false;

    private lateinit var sensorManager: SensorManager
    private var magnetometer: Sensor? = null

    private var directionPolygon: Polygon? = null
    private var currentDirectionAngle: Float = 0f

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_CAMERA_PERMISSION = 2
    }

    private var imageUri: Uri? = null

    override fun onResume() {
        super.onResume()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        magnetometer?.let {
            sensorManager.registerListener(magnetometerListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(magnetometerListener)
    }

    private val magnetometerListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
                // Get the magnetic field values along the x, y, and z axes
                val magneticFieldValues = event.values

                // Calculate the direction using the magnetic field values
                val direction = calculateDirection(magneticFieldValues[0], magneticFieldValues[1])

                // Draw a circular sector on the map representing the direction
                drawDirectionOnMap(direction)
            }
        }
    }

    private fun calculateDirection(x: Float, y: Float): Float {
        // Calculate the direction in degrees using the atan2 function
        var direction = Math.toDegrees(atan2(y.toDouble(), x.toDouble())).toFloat()

        // Adjust the direction to be in the range of 0 to 360 degrees
        if (direction < 0) {
            direction += 360f
        }

        return direction
    }

    private fun drawDirectionOnMap(newDirection: Float) {
        // Check if currentLocation is null
        if (currentLocation == null) {
            // Handle the case where currentLocation is null, maybe show a message or return early
            directionPolygon?.remove()
            return
        }

        // Draw a quarter circular sector on the map representing the direction
        val centerLatLng = LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
        val radius = 50 // Adjust the radius as needed (smaller radius)
        val startAngle =
            newDirection - 135 // Starting angle of the sector (e.g., 45 degrees left of the direction)
        val sweepAngle = 90 // Sweep angle of the sector (e.g., 90 degrees)
        val sectorOptions = PolygonOptions()
            .strokeColor(Color.TRANSPARENT) // No border
            .fillColor(Color.argb(128, 255, 0, 0)) // Red color for fill

        // If directionPolygon already exists, update its angle
        if (directionPolygon != null) {
            val angleDiff = newDirection - currentDirectionAngle
            val updatedPoints = mutableListOf<LatLng>()
            for (point in directionPolygon!!.points) {
                val angle = SphericalUtil.computeHeading(centerLatLng, point)
                val newPoint = SphericalUtil.computeOffset(
                    centerLatLng,
                    radius.toDouble(),
                    (angle + angleDiff).toDouble()
                )
                updatedPoints.add(newPoint)
            }
            directionPolygon!!.points = updatedPoints
            currentDirectionAngle = newDirection
            return
        }

        // Create new directionPolygon if it doesn't exist
        for (i in startAngle.toInt()..(startAngle + sweepAngle).toInt()) {
            val point = SphericalUtil.computeOffset(centerLatLng, radius.toDouble(), i.toDouble())
            sectorOptions.add(point)
        }
        directionPolygon = mMap.addPolygon(sectorOptions)
        currentDirectionAngle = newDirection
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.location_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize Fused Location Provider client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        this.permission= isLocationPermissionGranted()

//        // Set onClickListener for the relocate button
//        val relocateButton: ImageButton = findViewById(R.id.relocate_button)
//        relocateButton.setOnClickListener {
//            // Call the method to move the camera to the user's current location
//            relocateCameraToCurrentLocation()
//        }
//        val captureButton: FloatingActionButton = findViewById(R.id.capture_button)
//        captureButton.setOnClickListener { openCamera() }
    }

    private fun openCamera() {
        startActivity(Intent(this, ImageDetectionActivity::class.java))
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            imageUri = createImageUri()
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    private fun createImageUri(): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "new_image.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        val resolver = contentResolver
        return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                dispatchTakePictureIntent()
            } else {
                Toast.makeText(
                    this,
                    "Camera permission is required to use this feature",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            imageUri?.let { uri ->
                val resolver = contentResolver
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    resolver.update(uri, ContentValues().apply {
                        put(MediaStore.Images.Media.IS_PENDING, 0)
                    }, null, null)
                }

//                val imageView: ImageView = findViewById(R.id.imageView)
//                imageView.setImageURI(uri)

//                // Process the captured image
//                val bitmap = MediaStore.Images.Media.getBitmap(resolver, uri)
//                processImage(bitmap)
            }
        }
    }

    private fun processImage(imageBitmap: Bitmap) {
        // Code to process the image and identify the landmark
    }


    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Initialize location manager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (this.permission) {

            // Request location updates for Network provider
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                updateInterval,
                5F,
                networkLocationListener
            )

            // Request location updates for GPS provider
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                updateInterval,
                5f,
                gpsLocationListener
            )

            // Request location updates from Fused Location Provider
            val locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(updateInterval)
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)

            // Start periodic task to update location every 5 seconds
            val handler = Handler()
            handler.postDelayed({
                updateLocation()
            }, updateInterval)
        } else {
            // Set default location (Sydney) if permission not granted
            val sydney = LatLng(-34.0, 151.0)
            mMap.addMarker(MarkerOptions().position(sydney).title("You are here (offline)"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        }
    }

    private val locationCallback = object : com.google.android.gms.location.LocationCallback() {
        override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
            locationResult.lastLocation?.let { handleLocationUpdate(it) }
        }
    }

    private val gpsLocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            // Handle GPS location update
            handleLocationUpdate(location)
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    private val networkLocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            handleLocationUpdate(location)
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    private fun relocateCameraToCurrentLocation() {
        // Check if currentLocation is not null
        currentLocation?.let { location ->
            val latLng = LatLng(location.latitude, location.longitude)
            mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    latLng,
                    16.5f
                )
            )
        }
    }

    private fun handleLocationUpdate(location: Location) {
        if (currentLocation == null) {
            currentLocation = location
            val latLng = LatLng(location.latitude, location.longitude)

            // Clear previous markers and circles
            mMap.clear()

            // Add a circle for current location with dark blue color
            val currentLocationCircleOptions = CircleOptions()
                .center(latLng)
                .radius(10.0) // Adjust the radius as needed
                .strokeColor(Color.BLUE) // Dark blue color for stroke
                .fillColor(Color.argb(128, 0, 0, 255)) // Light blue color for fill
            mMap.addCircle(currentLocationCircleOptions)

            // Add a circle for accuracy with light blue color and no border
            val accuracyCircleOptions = CircleOptions()
                .center(latLng)
                .radius(location.accuracy.toDouble()) // Radius equals accuracy
                .strokeColor(Color.TRANSPARENT) // No stroke color
                .fillColor(Color.argb(32, 0, 0, 255)) // Very light blue color for fill
            mMap.addCircle(accuracyCircleOptions)

            mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    latLng,
                    16.5f
                )
            ) // Adjust zoom level as needed
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocation() {
        // Update the location based on the current best location from GPS, network, and Fused Location Provider
        val gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        // Get last known location from the Fused Location Provider
        fusedLocationClient.lastLocation.addOnSuccessListener { fusedLocation ->
            // Choose the best location among GPS, network, and Fused Location Provider
            val bestLocation = getBestLocation(gpsLocation, networkLocation, fusedLocation)

            bestLocation?.let {
                handleLocationUpdate(it)
            }
        }

        // Re-schedule the task to update location every 5 seconds
        val handler = Handler()
        handler.postDelayed({
            updateLocation()
        }, updateInterval)
    }

    private fun getBestLocation(gpsLocation: Location?, networkLocation: Location?, fusedLocation: Location?): Location? {
        // Choose the best location based on accuracy
        var bestLocation: Location? = null

//        if (gpsLocation != null && (bestLocation == null || gpsLocation.accuracy < bestLocation.accuracy)) {
//            bestLocation = gpsLocation
//        }
//
//        if (networkLocation != null && (bestLocation == null || networkLocation.accuracy < bestLocation.accuracy)) {
//            bestLocation = networkLocation
//        }
//
//        if (fusedLocation != null && (bestLocation == null || fusedLocation.accuracy < bestLocation.accuracy)) {
//            bestLocation = fusedLocation
//        }
        bestLocation = gpsLocation
        return bestLocation
    }



    private fun isLocationPermissionGranted(): Boolean {
        return if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                requestcode
            )
            false
        } else {
            true
        }
    }

}
