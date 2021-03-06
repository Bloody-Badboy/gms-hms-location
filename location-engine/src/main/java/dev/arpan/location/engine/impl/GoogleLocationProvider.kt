package dev.arpan.location.engine.impl

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.IntentSender
import android.os.Looper
import androidx.annotation.RequiresPermission
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import dev.arpan.location.engine.GoogleResolvableApiException
import dev.arpan.location.engine.ILocationFetcher
import dev.arpan.location.engine.LocationReceiver
import dev.arpan.location.engine.LocationSettingCallback
import dev.arpan.location.engine.ResolvableApiException
import dev.arpan.location.engine.model.Location
import timber.log.Timber

internal class GoogleLocationProvider(
    private val context: Context,
    private val locationRequest: LocationRequest
) : ILocationFetcher {

    private val locationSettingsRequest = LocationSettingsRequest.Builder()
        .addLocationRequest(locationRequest)
        .setAlwaysShow(true)
        .build()

    private val fusedLocationProviderClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private var locationReceiver: LocationReceiver? = null
    private var isRequestingLocationUpdates = false

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult?.let {
                if (it.locations.isNotEmpty()) {
                    val location = it.locations[0]
                    locationReceiver?.onReceived(null, Location.fromLocation(location))
                }
            }
        }
    }

    override fun checkDeviceLocationSettings(callback: LocationSettingCallback) {
        LocationServices.getSettingsClient(context)
            .checkLocationSettings(locationSettingsRequest)
            .addOnCompleteListener { task ->
                try {
                    val response = task.getResult(ApiException::class.java)
                    callback.onResultSuccess()
                } catch (e: ApiException) {
                    Timber.e("ApiException -> statusCode:${e.statusCode} message:${e.message}")
                    if (e.statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                        try {
                            callback.onResultResolutionRequired(
                                ResolvableApiException(
                                    e as GoogleResolvableApiException
                                )
                            )
                        } catch (ignored: IntentSender.SendIntentException) {
                        } catch (ignored: ClassCastException) {
                        }
                    } else {
                        callback.onResultSettingsChangeUnavailable()
                    }
                }
            }
    }

    @RequiresPermission(anyOf = [ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION])
    override fun startLocationUpdates(receiver: LocationReceiver) {
        locationReceiver = receiver
        if (!isRequestingLocationUpdates) {
            try {
                fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.myLooper()!!
                ).addOnSuccessListener {
                    isRequestingLocationUpdates = !isRequestingLocationUpdates
                }.addOnFailureListener { exception ->
                    locationReceiver?.onReceived(exception, null)
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
                locationReceiver?.onReceived(e, null)
            }
        }
    }

    override fun stopLocationUpdates() {
        if (isRequestingLocationUpdates) {
            try {
                fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                isRequestingLocationUpdates = !isRequestingLocationUpdates
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }
}
