package io.ashkanans.artwalk

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import io.ashkanans.artwalk.databinding.FragmentPlaceDetailsBinding
import kotlinx.coroutines.launch
import services.api.google.place.details.PlaceDetails
import services.api.google.place.details.PlaceDetailsApplication
import services.api.google.place.details.PlaceDetailsServiceImpl
import services.api.google.place.searchText.PlaceSearchServiceImpl

class PlaceDetailsFragment : Fragment() {
    private var _binding: FragmentPlaceDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var app: PlaceDetailsApplication // Assuming you have an application context to access services
    private lateinit var apiKey: String // Your actual API key

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlaceDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize services
        apiKey = "AIzaSyCGygp0SRJldfPq7nWt7kPNtaJ168VZH7E" // Replace with your actual API key
        val placeSearchService = PlaceSearchServiceImpl(apiKey)
        val placeDetailsService = PlaceDetailsServiceImpl(apiKey)
        app = PlaceDetailsApplication(placeSearchService, placeDetailsService)

        // Retrieve landmarkName from arguments
        val landmarkName = arguments?.getString("landmarkName")

        // Start a coroutine to fetch place details
        viewLifecycleOwner.lifecycleScope.launch {
            if (!landmarkName.isNullOrEmpty()) {
                try {
                    val placeId = app.searchPlaceId(landmarkName)
                    if (placeId != null) {
                        val placeDetails = app.getPlaceDetails(placeId)
                        if (placeDetails != null) {
                            // Update UI with place details
                            updateUI(placeDetails)
                        } else {
                            // Handle case where place details are not found
                            showToast("Place details not found.")
                        }
                    } else {
                        // Handle case where place ID is not found
                        showToast("Place ID not found.")
                    }
                } catch (e: Exception) {
                    // Handle exceptions, e.g., network errors
                    showToast("Error fetching place details: ${e.message}")
                }
            } else {
                // Handle case where landmarkName is null or empty
                showToast("Invalid landmark name.")
            }
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun updateUI(placeDetails: PlaceDetails) {

        binding.placeName.text = arguments?.getString("landmarkName")

        // Update formatted address
        binding.placeAddress.text = placeDetails.formattedAddress

        // Update rating
        binding.placeRating.text = "Rating: ${placeDetails.rating}"

        if (placeDetails.rating != 0.0) {
            binding.placeRatingBar.rating = placeDetails.rating.toFloat()
            binding.placeRatingBar.visibility = View.VISIBLE
        } else {
            binding.placeRatingBar.visibility = View.GONE
        }

        // Update Google Maps URI and website URI
        if (placeDetails.googleMapsUri.isNotEmpty()) {
            val googleMapsUrl = placeDetails.googleMapsUri
            val googleMapsText = "Open in Google Maps"
            val spannableGoogleMaps = SpannableString(googleMapsText)
            spannableGoogleMaps.setSpan(object : ClickableSpan() {
                override fun onClick(view: View) {
                    openUrlInBrowser(googleMapsUrl)
                }
            }, 0, googleMapsText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            binding.placeGoogleMapsUrl.text = spannableGoogleMaps
            binding.placeGoogleMapsUrl.movementMethod = LinkMovementMethod.getInstance()
            binding.placeGoogleMapsUrl.visibility = View.VISIBLE
        } else {
            binding.placeGoogleMapsUrl.visibility = View.GONE
        }

        // Update website URI
        if (!placeDetails.websiteUri.isNullOrEmpty()) {
            val websiteUrl = placeDetails.websiteUri
            val websiteText = "Open in Browser"
            val spannableWebsite = SpannableString(websiteText)
            spannableWebsite.setSpan(object : ClickableSpan() {
                override fun onClick(view: View) {
                    openUrlInBrowser(websiteUrl)
                }
            }, 0, websiteText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            binding.placeWebsiteUrl.text = spannableWebsite
            binding.placeWebsiteUrl.movementMethod = LinkMovementMethod.getInstance()
            binding.placeWebsiteUrl.visibility = View.VISIBLE
        } else {
            binding.placeWebsiteUrl.visibility = View.GONE
        }

        val locationText =
            "Latitude: ${placeDetails.location.latitude}, Longitude: ${placeDetails.location.longitude}"
        binding.placeLocation.text = locationText

        val typesText = placeDetails.types.joinToString(", ")
        binding.placeTypes.text = "Types: $typesText"

        if (placeDetails.userRatingCount != null) {
            binding.placeUserRatingCount.text = "${placeDetails.userRatingCount} user ratings"
            binding.placeUserRatingCount.visibility = View.VISIBLE
        } else {
            binding.placeUserRatingCount.visibility = View.GONE
        }

        if (placeDetails.types != null && placeDetails.types.isNotEmpty()) {
            val types = placeDetails.types.joinToString(", ") { type ->
                type.split("_").joinToString(" ") { it.capitalize() }
            }
            binding.placeTypes.text = "${types}"
            binding.placeTypes.visibility = View.VISIBLE
        } else {
            binding.placeTypes.visibility = View.GONE
        }

        if (placeDetails.location != null) {
            binding.placeLocation.text =
                "latitude: ${placeDetails.location.latitude}, \nlongitude: ${placeDetails.location.longitude}"
            binding.placeLocation.visibility = View.VISIBLE
        } else {
            binding.placeLocation.visibility = View.GONE
        }

        if (placeDetails.businessStatus != null) {
            binding.placeBusinessStatus.text = "Business status: ${placeDetails.businessStatus}"
            binding.placeBusinessStatus.visibility = View.VISIBLE
        } else {
            binding.placeBusinessStatus.visibility = View.GONE
        }

        if (placeDetails.plusCode != null) {
            binding.placePlusCode.text = "Plus Code: ${placeDetails.plusCode.globalCode}"
            binding.placePlusCode.visibility = View.VISIBLE
        } else {
            binding.placePlusCode.visibility = View.GONE
        }

        if (placeDetails.editorialSummary.text.isNotEmpty()) {
            binding.placeEditorialSummary.text = "Summary: ${placeDetails.editorialSummary.text}"
            binding.placeEditorialSummary.visibility = View.VISIBLE
        } else {
            binding.placeEditorialSummary.visibility = View.GONE
        }
    }

    private fun openUrlInBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
