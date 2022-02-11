package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.udacity.project4.base.NavigationCommand


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding


    private val REQUEST_LOCATION_PERMISSION: Int = 1
    private val TAG = this::class.java.simpleName
    private lateinit var map: GoogleMap
    private lateinit var poiList: MutableList<PointOfInterest>

    private var fusedLocationClient: FusedLocationProviderClient? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        _viewModel.onClear()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        poiList = mutableListOf<PointOfInterest>()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        binding.saveButton.setOnClickListener {
            onLocationSelected()
        }
        return binding.root
    }

    private fun onLocationSelected() {
        if (poiList.isEmpty()) {
            Toast.makeText(
                requireActivity(),
                "Please select a point of interest",
                Toast.LENGTH_SHORT
            ).show();
        } else {
            val poi = poiList.first()
            _viewModel.setPOIMarker(poi)
            findNavController().popBackStack()
        }
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.setMyLocationEnabled(true)
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        enableMyLocation()

//        map.addMarker(MarkerOptions().position(home).title("My Home"))
        if (isPermissionGranted()) {
            // Shows my location only if the permission is granted
            map.isMyLocationEnabled = true

            // zoom to the user location after taking his permission
            fusedLocationClient!!.lastLocation
                .addOnSuccessListener { location: Location? ->
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                location!!.latitude,
                                location.longitude
                            ), 14F
                        )
                    )
                }
        }
        setMapGeneralClick(map)
        setMapPoiClick(map)
        setMapStyle(map)
    }

    private fun setMapGeneralClick(map: GoogleMap)  {
        map.setOnMapClickListener { pos ->

            map.clear() //clear all beforehand...
            _viewModel.onClear()

            // Move camera to the selected location
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pos, 14F
                )
            )

            val latLng = LatLng(pos.latitude, pos.longitude)
            val title = "spot at ${latLng}"
            val marker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(title)
            )
            marker?.let {
                it.showInfoWindow()
            }

            val poi = PointOfInterest(latLng, UUID.randomUUID().toString(), title)

            poiList.add(poi)
        }
    }

    private fun setMapPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->

            map.clear() //clear all beforehand...
            _viewModel.onClear()

            val marker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            marker?.let {
                it.showInfoWindow()
            }

            poiList.add(poi)
        }
    }

    fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions
                    .loadRawResourceStyle(requireActivity(), R.raw.map_style)
            )

            if (!success) {
                Log.d(TAG, "failed")
            }
        } catch (ex: Exception) {
            Toast.makeText(requireActivity(), "error: $ex", Toast.LENGTH_SHORT).show();
        }
    }


}
