package io.ashkanans.artwalk.presentation.location

import android.graphics.Color
import android.location.Location
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.SphericalUtil

class MapHandler(private val map: GoogleMap) {
    private var directionPolygon: Polygon? = null
    private var currentDirectionAngle: Float = 0f

    fun updateCamera(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.5f))
    }

    fun drawCurrentLocation(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        map.clear()
        val currentLocationCircleOptions = CircleOptions()
            .center(latLng)
            .radius(10.0)
            .strokeColor(Color.BLUE)
            .fillColor(Color.argb(128, 0, 0, 255))
        map.addCircle(currentLocationCircleOptions)

        val accuracyCircleOptions = CircleOptions()
            .center(latLng)
            .radius(location.accuracy.toDouble())
            .strokeColor(Color.TRANSPARENT)
            .fillColor(Color.argb(32, 0, 0, 255))
        map.addCircle(accuracyCircleOptions)

        updateCamera(location)
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
}
