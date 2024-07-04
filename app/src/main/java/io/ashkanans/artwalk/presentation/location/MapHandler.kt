package io.ashkanans.artwalk.presentation.location

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.location.Location
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.SphericalUtil
import io.ashkanans.artwalk.R
import io.ashkanans.artwalk.domain.model.Place
import io.ashkanans.artwalk.domain.model.PlaceType
import io.ashkanans.artwalk.presentation.library.dashboard.LibraryDashboardFragment

@SuppressLint("PotentialBehaviorOverride")
class MapHandler(private val parentActivity: FragmentActivity) : GoogleMap.OnMarkerClickListener {

    var map: GoogleMap? = null
    private var directionPolygon: Polygon? = null
    private var currentDirectionAngle: Float = 0f
    private val markers = mutableListOf<Marker>()
    private var markersVisible = false

    private var currentLocationCircle: Circle? = null
    private var accuracyMarker: Marker? = null

    private var currentLocationMarker: Marker? = null

    private val polylines = mutableListOf<Polyline>()

    var placeTypes: List<PlaceType>? = null
        get() = field
        set(value) {
            field = value
        }
    var romeTouristicPlaces: List<Place>? = null
        get() = field
        set(value) {
            field = value
        }

    var predictedPlaces: List<Place>? = null
        get() = field
        set(value) {
            field = value
        }

    fun setMarkerListener() {
        map?.setOnMarkerClickListener { marker ->
            marker.showInfoWindow()
            true
        }

        map?.setOnInfoWindowClickListener { marker ->
            marker.title?.let {
                openLibraryDashboardFragment(it)
            }
        }
    }

    fun updateCamera(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.5f))
    }

    private fun createCurrentLocationBitmap(): Bitmap {
        val size = 40 // Set the size of the marker
        val paint = Paint()
        paint.color = Color.BLUE
        paint.style = Paint.Style.FILL

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

        return bitmap
    }

    private fun createAccuracyCircleBitmap(radius: Float): Bitmap {
        val size = (radius * 2).toInt()
        val paint = Paint()
        paint.color = Color.argb(32, 0, 0, 255)
        paint.style = Paint.Style.FILL

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

        return bitmap
    }

    fun drawCurrentLocation(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        val locationBitmapDescriptor =
            BitmapDescriptorFactory.fromBitmap(createCurrentLocationBitmap())
        val accuracyBitmapDescriptor =
            BitmapDescriptorFactory.fromBitmap(createAccuracyCircleBitmap(location.accuracy))

        if (currentLocationMarker == null) {
            val markerOptions = MarkerOptions()
                .position(latLng)
                .icon(locationBitmapDescriptor)
                .anchor(0.5f, 0.5f)
            currentLocationMarker = map?.addMarker(markerOptions)
        } else {
            currentLocationMarker?.setIcon(locationBitmapDescriptor)
            currentLocationMarker?.position = latLng
        }

        if (accuracyMarker == null) {
            val accuracyMarkerOptions = MarkerOptions()
                .position(latLng)
                .icon(accuracyBitmapDescriptor)
                .anchor(0.5f, 0.5f)
            accuracyMarker = map?.addMarker(accuracyMarkerOptions)
        } else {
            accuracyMarker?.setIcon(accuracyBitmapDescriptor)
            accuracyMarker?.position = latLng
        }
    }

    fun drawDirectionOnMap(location: Location, newDirection: Float) {
        val centerLatLng = LatLng(location.latitude, location.longitude)
        val radius = 50
        val startAngle = newDirection - 135
        val sweepAngle = 90
        val sectorOptions = PolygonOptions()
            .strokeColor(Color.TRANSPARENT)
            .fillColor(Color.argb(128, 255, 0, 0))

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

        for (i in startAngle.toInt()..(startAngle + sweepAngle).toInt()) {
            val point = SphericalUtil.computeOffset(centerLatLng, radius.toDouble(), i.toDouble())
            sectorOptions.add(point)
        }
        directionPolygon = map?.addPolygon(sectorOptions)
        currentDirectionAngle = newDirection
    }

    fun toggleTouristicLocations() {
        markersVisible = !markersVisible
        if (markersVisible) {
            drawPlacesMarkers()
        } else {
            for (marker in markers) {
                marker.remove()
            }
            markers.clear()
        }
    }

    fun drawPlacesMarkers() {
        if (!markersVisible) {
            markersVisible = true
        }
        // Clear existing markers
        for (marker in markers) {
            marker.remove()
        }
        markers.clear()

        // Show markers for selected place types
        romeTouristicPlaces?.let { places ->
            val selectedPlaceTypes =
                placeTypes?.filter { it.isChecked }?.map { it.type } ?: emptyList()

            for (touristicPlace in places) {
                val latLng =
                    LatLng(touristicPlace.location.latitude, touristicPlace.location.longitude)

                // Get the primary type
                val type = touristicPlace.primaryType

                // Check if the type is in the selected types
                if (type in selectedPlaceTypes) {
                    // Determine the color for the type and convert it to a hue value
                    val color = typeColorMap[type] ?: typeColorMap["default"]!!
                    val hsv = FloatArray(3)
                    Color.colorToHSV(color, hsv)
                    val hue = hsv[0]

                    // Create a marker with the hue
                    val marker = map?.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title(touristicPlace.displayName)
                            .icon(BitmapDescriptorFactory.defaultMarker(hue))
                    )

                    marker?.let {
                        markers.add(it)
                    }
                }
            }

            // Optionally, move the camera to the first touristic place in the list
            val filteredPlaces = places.filter { it.primaryType in selectedPlaceTypes }
            if (filteredPlaces.isNotEmpty()) {
                val firstPlace = filteredPlaces.first()
                val firstLatLng =
                    LatLng(firstPlace.location.latitude, firstPlace.location.longitude)
                map?.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLatLng, 12.0f))
            }
        }
    }

    fun drawPredictedPlacesMarkers() {
        if (!markersVisible) {
            markersVisible = true
        }

        // Clear existing markers and polylines
        for (marker in markers) {
            marker.remove()
        }
        markers.clear()

        // Clear existing polylines
        for (polyline in polylines) {
            polyline.remove()
        }
        polylines.clear()

        // Show markers for predicted places
        predictedPlaces?.let { places ->
            for (i in places.indices) {
                val touristicPlace = places[i]
                val latLng =
                    LatLng(touristicPlace.location.latitude, touristicPlace.location.longitude)

                // Get the primary type
                val type = touristicPlace.primaryType

                // Check if the type is in the selected types
                if (true) { // Replace with your condition to filter types
                    // Determine the color for the type and convert it to a hue value
                    val color = typeColorMap[type] ?: typeColorMap["default"]!!
                    val hsv = FloatArray(3)
                    Color.colorToHSV(color, hsv)
                    val hue = hsv[0]

                    // Create a marker with the hue
                    val marker = map?.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title(touristicPlace.displayName)
                            .icon(BitmapDescriptorFactory.defaultMarker(hue))
                    )

                    marker?.let {
                        markers.add(it)
                    }

                    // Draw polyline between consecutive markers
                    if (i > 0) {
                        val prevPlace = places[i - 1]
                        val prevLatLng =
                            LatLng(prevPlace.location.latitude, prevPlace.location.longitude)

                        // Define polyline options
                        val polylineOptions = PolylineOptions()
                            .add(prevLatLng)
                            .add(latLng)
                            .color(Color.BLUE) // Customize the color as needed
                            .width(5f) // Customize the width as needed

                        // Add polyline to the map
                        val polyline = map?.addPolyline(polylineOptions)
                        polyline?.let {
                            polylines.add(it)
                        }
                    }
                }
            }

            // Move camera to the first predicted place
            if (places.isNotEmpty()) {
                val firstPlace = places.first()
                val firstLatLng =
                    LatLng(firstPlace.location.latitude, firstPlace.location.longitude)
                map?.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLatLng, 15.0f))
            }
        }
    }

    private val typeColorMap: Map<String, Int> = mapOf(
        "italian_restaurant" to Color.parseColor("#FF6347"), // Tomato
        "restaurant" to Color.parseColor("#FFD700"), // Gold
        "mediterranean_restaurant" to Color.parseColor("#FFA500"), // Orange
        "seafood_restaurant" to Color.parseColor("#00BFFF"), // DeepSkyBlue
        "historical_landmark" to Color.parseColor("#8B4513"), // SaddleBrown
        "museum" to Color.parseColor("#4682B4"), // SteelBlue
        "park" to Color.parseColor("#32CD32"), // LimeGreen
        "tourist_attraction" to Color.parseColor("#FF69B4"), // HotPink
        "church" to Color.parseColor("#8A2BE2"), // BlueViolet
        "landmark" to Color.parseColor("#7FFF00"), // Chartreuse
        "art_gallery" to Color.parseColor("#FF4500"), // OrangeRed
        "dog_park" to Color.parseColor("#D2691E"), // Chocolate
        "rv_park" to Color.parseColor("#DAA520"), // GoldenRod
        "zoo" to Color.parseColor("#CD5C5C"), // IndianRed
        "store" to Color.parseColor("#808080"), // Gray
        "gift_shop" to Color.parseColor("#ADFF2F"), // GreenYellow
        "clothing_store" to Color.parseColor("#00CED1"), // DarkTurquoise
        "ice_cream_shop" to Color.parseColor("#FFB6C1"), // LightPink
        "event_venue" to Color.parseColor("#800080"), // Purple
        "movie_theater" to Color.parseColor("#A52A2A"), // Brown
        "performing_arts_theater" to Color.parseColor("#DC143C"), // Crimson
        "market" to Color.parseColor("#7B68EE"), // MediumSlateBlue
        "library" to Color.parseColor("#4B0082"), // Indigo
        "default" to Color.parseColor("#FF0000") // Red for default
    )


    private fun openLibraryDashboardFragment(caption: String) {
        val fragment = LibraryDashboardFragment().apply {
            arguments = Bundle().apply {
                putString("CAPTION", caption)
            }
        }
        parentActivity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        marker.title?.let {
            openLibraryDashboardFragment(it)
        }
        return true
    }
}
