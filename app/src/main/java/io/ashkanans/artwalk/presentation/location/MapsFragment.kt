package io.ashkanans.artwalk.presentation.location

import android.app.AlertDialog
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import io.ashkanans.artwalk.R
import io.ashkanans.artwalk.databinding.FragmentMapsBinding
import io.ashkanans.artwalk.domain.model.DataModel
import io.ashkanans.artwalk.presentation.location.configurations.ConfigAdapter
import io.ashkanans.artwalk.presentation.location.configurations.ConfigAdapter.Companion.VIEW_TYPE_ONE
import io.ashkanans.artwalk.presentation.location.configurations.ConfigAdapter.Companion.VIEW_TYPE_TWO
import io.ashkanans.artwalk.presentation.location.configurations.ConfigModel

class MapsFragment : Fragment(), OnMapReadyCallback {
    private lateinit var viewModel: MapsViewModel
    private var progressDialog: AlertDialog? = null

    private lateinit var configModel: ArrayList<ConfigModel>
    private lateinit var configAdapter: ConfigAdapter

    private lateinit var mMap: GoogleMap
    private lateinit var binding: FragmentMapsBinding
    private var currentLocation: Location? = null
    private val updateInterval = 5000L

    private lateinit var locationHandler: LocationHandler
    private lateinit var sensorHandler: SensorHandler
    lateinit var mapHandler: MapHandler

    private var isCardVisible = false

    override fun onResume() {
        super.onResume()
        sensorHandler.startSensorUpdates()
    }

    override fun onPause() {
        super.onPause()
        sensorHandler.stopSensorUpdates()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(MapsViewModel::class.java)
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                showProgressDialog()
            } else {
                hideProgressDialog()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.predictions.observe(viewLifecycleOwner) { predictions ->
            predictions?.let {
                // Handle displaying predictions on the map
                mapHandler.predictedPlaces = it
                mapHandler.drawPredictedPlacesMarkers()
            }
        }
    }

    private fun showProgressDialog() {
        if (progressDialog == null) {
            val builder = AlertDialog.Builder(requireContext())
            val inflater = layoutInflater
            val dialogView = inflater.inflate(R.layout.dialog_progress, null)
            builder.setView(dialogView)
            builder.setCancelable(false)

            val cancelButton = dialogView.findViewById<Button>(R.id.cancel_button)
            cancelButton.setOnClickListener {
                viewModel.cancelPrediction()
                hideProgressDialog()
            }

            progressDialog = builder.create()
        }
        progressDialog?.show()
    }

    private fun hideProgressDialog() {
        progressDialog?.dismiss()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapsBinding.inflate(inflater, container, false)
        val view = binding.root

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.location_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mapHandler = MapHandler(this.requireActivity())
        setupUIInteractions()
        setupHandlers()

        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                val title = configModel[position].title
            }

            override fun onPageSelected(position: Int) {}

            override fun onPageScrollStateChanged(state: Int) {}
        })
        return view
    }

    private fun loadCards() {
        configModel = ArrayList()

        val placeTypes = mapHandler.placeTypes


        configModel.add(
            ConfigModel(
                "Maps",
                "Google Maps",
                "",
                R.drawable.google_maps,
                VIEW_TYPE_ONE
            )
        )
        configModel.add(
            ConfigModel(
                "test 2",
                "test 2",
                "test 2",
                R.drawable.icons8_logo_di_google,
                VIEW_TYPE_TWO
            )
        )

        configAdapter = ConfigAdapter(requireContext(), configModel, placeTypes, this)

        binding.viewPager.adapter = configAdapter

        binding.viewPager.setPadding(60, 0, 60, 0)
    }

    private fun setupHandlers() {
        locationHandler = LocationHandler(requireContext(), updateInterval) { location ->
            currentLocation = location
            mapHandler.drawCurrentLocation(location)
        }

        sensorHandler = SensorHandler(requireContext()) { direction ->
            currentLocation?.let { mapHandler.drawDirectionOnMap(it, direction) }
        }
    }

    private fun setupUIInteractions() {
        binding.relocateButton.setOnClickListener {
            currentLocation?.let { mapHandler.updateCamera(it) }
        }

        binding.showLocationsButton.setOnClickListener {
            mapHandler.toggleTouristicLocations()
        }

        binding.arrowButton.setOnClickListener {
            toggleCardVisibility()
        }

        getPlaceTypes()
        getPlaces()
    }

    private fun getPlaces() {
        DataModel.getPlaceModel { placeModel ->
            if (placeModel != null) {
                mapHandler.romeTouristicPlaces = placeModel.dataModel
            } else {
                println("Place Types Model is null")
            }
        }
    }

    private fun getPlaceTypes() {
        DataModel.getPlaceTypesModel { placeTypesModel ->
            if (placeTypesModel != null) {
                mapHandler.placeTypes = placeTypesModel.dataModel
            } else {
                println("Place Types Model is null")
            }
        }
    }

    fun toggleCardVisibility() {
        loadCards()
        isCardVisible = !isCardVisible
        if (isCardVisible) {
            slideUp(binding.cardView)
        } else {
            slideDown(binding.cardView)
        }
    }

    private fun slideDown(view: View) {
        view.visibility = View.VISIBLE
        view.animate()
            .translationY(view.height.toFloat())
            .setInterpolator(AccelerateInterpolator())
            .setDuration(350)
            .withEndAction {
                view.visibility = View.GONE
            }
    }

    private fun slideUp(view: View) {
        view.visibility = View.VISIBLE
        view.animate()
            .translationY(0f)
            .setInterpolator(DecelerateInterpolator())
            .setDuration(350)
            .withEndAction {
                view.visibility = View.VISIBLE
            }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mapHandler.map = mMap
        mapHandler.setMarkerListener()
        if (locationHandler.checkLocationPermission()) {
            locationHandler.startLocationUpdates()
        } else {
            val sydney = LatLng(-34.0, 151.0)
            mMap.addMarker(MarkerOptions().position(sydney).title("You are here (offline)"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up resources
        locationHandler.stopLocationUpdates()
        // Optionally, clear references to handlers to avoid memory leaks
    }
}
