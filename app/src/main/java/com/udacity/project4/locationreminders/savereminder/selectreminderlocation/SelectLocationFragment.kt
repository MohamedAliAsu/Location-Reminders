package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {
    val FOREGROUND_LOCATION = 11
    private lateinit var mfusedLocation: FusedLocationProviderClient

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private lateinit var poi: PointOfInterest

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this
        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)


        ((childFragmentManager.findFragmentById(R.id.map)) as SupportMapFragment).getMapAsync(this)

//        TODO: add style to the map
//        TODO: put a marker to location that the user selected
        binding.confirm.setOnClickListener {
            if (::poi.isInitialized) {
                _viewModel.apply {
                    reminderSelectedLocationStr.value = poi.name
                    latitude.value = poi.latLng.latitude
                    longitude.value = poi.latLng.longitude
                    selectedPOI.value = poi


                    navigationCommand.value =

                        NavigationCommand.Back

                }
            } else Snackbar.make(binding.root, "Please select a location", Snackbar.LENGTH_LONG)
                .show()
        }

//        TODO: call this function after the user confirms on the selected location


        return binding.root
    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map.setOnPoiClickListener {
            map.clear()
            map.addMarker(MarkerOptions()
                .position(it.latLng)
                .title(it.name)
            )?.showInfoWindow()
           poi = it

        }
        map.setOnMapLongClickListener {

            map.clear()
            map.addMarker(MarkerOptions().position(it).title("selected location"))?.showInfoWindow()
            poi = PointOfInterest(it,"selected Place","lat: ${it.latitude} long: ${it.longitude}")

        }
        mfusedLocation= LocationServices.getFusedLocationProviderClient(requireActivity())
        try {
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(),R.raw.mapstyling))
        }
        catch (ex:Resources.NotFoundException){
            Toast.makeText(requireContext(),"couldn't parse map styling",Toast.LENGTH_LONG).show()
        }
        val myHome = LatLng(30.090367, 31.270625)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(myHome,15f))
        if (permissionApproved()) {
            setUserLocation()
        } else {
            requestPermission()
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


    private fun permissionApproved(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            setUserLocation()

        } else {
            this.requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                FOREGROUND_LOCATION
            )
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isEmpty() || (grantResults[0] == PackageManager.PERMISSION_DENIED)) {
            Snackbar.make(binding.root,
                getString(R.string.location_required_error),
                Snackbar.LENGTH_INDEFINITE).setAction(R.string.settings) {
                startActivity(Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }.show()
        } else {
            setUserLocation()
        }
    }
    @SuppressLint("MissingPermission")
    private fun setUserLocation() {
        map.isMyLocationEnabled = true
        Log.d("MapsActivity", "getLastLocation Called")
        mfusedLocation.lastLocation.addOnCompleteListener{
            if(it.isSuccessful){
                it.result?.let {

                map.moveCamera(CameraUpdateFactory.newLatLng(LatLng(it.latitude,it.longitude)))
                map.addMarker(MarkerOptions().position(LatLng(it.latitude,it.longitude)))
            }}
        }


    }


}

