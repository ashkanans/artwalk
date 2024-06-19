import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import io.ashkanans.artwalk.R
import io.ashkanans.artwalk.databinding.FragmentMapsBinding
import io.ashkanans.artwalk.presentation.location.LocationHandler
import io.ashkanans.artwalk.presentation.location.MapHandler
import io.ashkanans.artwalk.presentation.location.SensorHandler
import io.ashkanans.artwalk.presentation.location.configurations.ConfigAdapter
import io.ashkanans.artwalk.presentation.location.configurations.ConfigModel

class MapsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var configModel: ArrayList<ConfigModel>
    private lateinit var configAdapter: ConfigAdapter

    private lateinit var mMap: GoogleMap
    private lateinit var binding: FragmentMapsBinding
    private var currentLocation: Location? = null
    private val updateInterval = 5000L

    private lateinit var locationHandler: LocationHandler
    private lateinit var sensorHandler: SensorHandler
    private lateinit var mapHandler: MapHandler

    private var isCardVisible = false

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
        val view = binding.root

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.location_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupHandlers()
        setupUIInteractions()

        loadCards()

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

        configModel.add(ConfigModel("test 1", "test 1", "test 1", R.drawable.google_maps))
        configModel.add(ConfigModel("test 2", "test 2", "test 2", R.drawable.icons8_logo_di_google))
        configModel.add(
            ConfigModel(
                "test 3",
                "test 3",
                "test 3",
                com.google.android.gms.base.R.drawable.googleg_disabled_color_18
            )
        )
        configModel.add(
            ConfigModel(
                "test 4",
                "test 4",
                "test 4",
                com.google.android.gms.base.R.drawable.common_google_signin_btn_icon_dark
            )
        )
        configModel.add(
            ConfigModel(
                "test 5",
                "test 5",
                "test 5",
                com.google.android.gms.auth.api.R.drawable.common_google_signin_btn_icon_light_focused
            )
        )
        configModel.add(
            ConfigModel(
                "test 6",
                "test 6",
                "test 6",
                com.google.android.gms.base.R.drawable.common_google_signin_btn_text_light_focused
            )
        )

        configAdapter = ConfigAdapter(requireContext(), configModel)

        binding.viewPager.adapter = configAdapter

        binding.viewPager.setPadding(100, 0, 100, 0)
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
    }

    private fun toggleCardVisibility() {
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
        mapHandler = MapHandler(mMap)
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
