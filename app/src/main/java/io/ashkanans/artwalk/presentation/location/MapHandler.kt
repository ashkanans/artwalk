package io.ashkanans.artwalk.presentation.location

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.location.Location
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.SphericalUtil

class MapHandler(private val map: GoogleMap) {
    private var directionPolygon: Polygon? = null
    private var currentDirectionAngle: Float = 0f
    private val markers = mutableListOf<Marker>()
    private var markersVisible = false

    private var currentLocationCircle: Circle? = null
    private var accuracyMarker: Marker? = null

    private var currentLocationMarker: Marker? = null

    val romeTouristicPlaces = listOf(
        Location(41.8902, 12.4922, "Colosseum"),
        Location(41.9029, 12.4534, "Vatican City"),
        Location(41.9022, 12.4547, "St. Peter's Basilica"),
        Location(41.8896, 12.4769, "Roman Forum"),
        Location(41.9029, 12.4964, "Trevi Fountain"),
        Location(41.9031, 12.4854, "Pantheon"),
        Location(41.8903, 12.4923, "Piazza Navona"),
        Location(41.9028, 12.4849, "Spanish Steps"),
        Location(41.9009, 12.4833, "Campo de' Fiori"),
        Location(41.8931, 12.4833, "Piazza Venezia"),
        Location(41.8992, 12.4763, "Villa Borghese"),
        Location(41.8891, 12.4817, "Capitoline Hill"),
        Location(41.8979, 12.4717, "Castel Sant'Angelo"),
        Location(41.8914, 12.4923, "Circus Maximus"),
        Location(41.9023, 12.4915, "Quirinal Palace"),
        Location(41.8964, 12.4823, "Galleria Borghese"),
        Location(41.8897, 12.4714, "Aventine Hill"),
        Location(41.8993, 12.4767, "Piazza del Popolo"),
        Location(41.8923, 12.4845, "Baths of Caracalla"),
        Location(41.9058, 12.4822, "Trastevere")
    )

    fun updateCamera(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.5f))
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
            currentLocationMarker = map.addMarker(markerOptions)
        } else {
            currentLocationMarker?.setIcon(locationBitmapDescriptor)
            currentLocationMarker?.position = latLng
        }

        if (accuracyMarker == null) {
            val accuracyMarkerOptions = MarkerOptions()
                .position(latLng)
                .icon(accuracyBitmapDescriptor)
                .anchor(0.5f, 0.5f)
            accuracyMarker = map.addMarker(accuracyMarkerOptions)
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
        directionPolygon = map.addPolygon(sectorOptions)
        currentDirectionAngle = newDirection
    }

    fun toggleTouristicLocations() {
        if (markersVisible) {
            // Hide markers
            for (marker in markers) {
                marker.remove()
            }
            markers.clear()
        } else {
            // Show markers
            for (touristicPlace in romeTouristicPlaces) {
                val latLng = LatLng(touristicPlace.latitude, touristicPlace.longitude)
                val marker =
                    map.addMarker(MarkerOptions().position(latLng).title(touristicPlace.name))
                if (marker != null) {
                    markers.add(marker)
                }
            }

            // Optionally, move the camera to the first touristic place in the list
//            if (romeTouristicPlaces.isNotEmpty()) {
//                val firstPlace = romeTouristicPlaces.first()
//                val firstLatLng = LatLng(firstPlace.latitude, firstPlace.longitude)
//                map.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLatLng, 12.0f))
//            }
        }
        markersVisible = !markersVisible
    }
}
