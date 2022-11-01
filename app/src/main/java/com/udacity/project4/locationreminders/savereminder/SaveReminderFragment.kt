package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity.RESULT_OK
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil

import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeoFenceConstants
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofenceClient: GeofencingClient
    private val runningQorLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    val geofencePendingIntent by lazy {
        PendingIntent.getBroadcast(requireContext(),
            0,
            Intent(requireContext(), GeofenceBroadcastReceiver::class.java).apply {
                action = GeoFenceConstants.ACTION_GEO_EVENT
            },
            PendingIntent.FLAG_MUTABLE)
    }
    val Fore_And_Back = 15
    val Fore_only = 20
    val requestLocationOn = 25
    private lateinit var reminderDTI: ReminderDataItem

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            _viewModel.navigationCommand.value =

                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }
        geofenceClient = GeofencingClient(requireActivity())
        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude
            val longitude = _viewModel.longitude.value

//            TODO: use the user entered reminder details to:
//             1) add a geofencing request
            reminderDTI =
                ReminderDataItem(title, description.value, location, latitude.value, longitude)

//             2) save the reminder to the local db
            if (
                _viewModel.validateEnteredData(reminderDTI)
            ) {
                if (foreAndBackApproved()) {
                    checkLocationAndStartGeofincing()
                } else {
                    requestLocaionPermissions()
                }
            }
        }
        binding.selectLocation.setOnClickListener {
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

    }


    @TargetApi(29)
    private fun foreAndBackApproved(): Boolean {
        val fore = ActivityCompat.checkSelfPermission(requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION) == PERMISSION_GRANTED
        val back = if (runningQorLater) ActivityCompat.checkSelfPermission(requireContext(),
            Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PERMISSION_GRANTED else true
        return fore && back
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    fun requestLocaionPermissions() {
        if (foreAndBackApproved()) {
            return
        }
        var permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val requestCode = if (runningQorLater) {
            permissions += Manifest.permission.ACCESS_BACKGROUND_LOCATION
            Fore_And_Back
        } else Fore_only
        this.requestPermissions(permissions,requestCode)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isEmpty() || grantResults[0] == PERMISSION_DENIED || (requestCode == Fore_And_Back && grantResults[1] == PERMISSION_DENIED))
        {
            Snackbar.make(
                binding.constLayout,
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.settings) {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
        }else{
            checkLocationAndStartGeofincing()
        }
    }

    private fun checkLocationAndStartGeofincing(resolve :Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve){
                try {
                    startIntentSenderForResult(exception.resolution.intentSender, requestLocationOn,
                        null, 0,0,0, null)

                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d("SaveReminder : CheckLocation", "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    requireView(),
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkLocationAndStartGeofincing()
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful ) {

                Log.i("Successful", "$it")

                addgeofence()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == requestLocationOn){
            if(resultCode == RESULT_OK){
                addgeofence()
            }
            else{
                checkLocationAndStartGeofincing(false)
            }
        }
    }
    private fun addgeofence() {
        val geofence = Geofence.Builder().apply {
            setRequestId(reminderDTI.id)
            setCircularRegion(reminderDTI.latitude!!,reminderDTI.longitude!!,GeoFenceConstants.RADIUS_IN_METERS)
            setExpirationDuration(Geofence.NEVER_EXPIRE)
            setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
        }.build()
        val geoRequest = GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofence(geofence)
        }.build()
        geofenceClient.addGeofences(geoRequest,geofencePendingIntent).run {
            addOnSuccessListener {
                _viewModel.validateAndSaveReminder(reminderDTI)
            }
            addOnFailureListener {
                Snackbar.make(binding.constLayout,"UNKNOWN ERROR",Snackbar.LENGTH_LONG).show()
            }
        }
    }

}
