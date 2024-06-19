package io.ashkanans.artwalk.presentation.location

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import io.ashkanans.artwalk.R
import io.ashkanans.artwalk.databinding.FragmentMapsBinding

class MapsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: FragmentMapsBinding
    private var currentLocation: Location? = null
    private val updateInterval = 5000L

    private lateinit var locationHandler: LocationHandler
    private lateinit var sensorHandler: SensorHandler
    private lateinit var mapHandler: MapHandler

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_CAMERA_PERMISSION = 2
    }

    override fun onResume() {
        super.onResume()
        sensorHandler.startSensorUpdates()
    }

    override fun onPause() {
        super.onPause()
        sensorHandler.stopSensorUpdates()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapsBinding.inflate(inflater, container, false)
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.location_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        locationHandler = LocationHandler(requireContext(), updateInterval) { location ->
            currentLocation = location
            mapHandler.drawCurrentLocation(location)
        }

        sensorHandler = SensorHandler(requireContext()) { direction ->
            currentLocation?.let { mapHandler.drawDirectionOnMap(it, direction) }
        }

        binding.relocateButton.setOnClickListener {
            currentLocation?.let { mapHandler.updateCamera(it) }
        }

        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mapHandler = MapHandler(mMap)
        if (locationHandler.checkLocationPermission()) {
            locationHandler.startLocationUpdates()
        } else {
            val sydney = LatLng(-34.0, 151.0)
            mMap.addMarker(MarkerOptions().position(sydney).title("You are here (offline)"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        }
    }
}
