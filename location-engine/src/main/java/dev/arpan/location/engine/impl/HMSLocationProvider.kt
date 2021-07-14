package dev.arpan.location.engine.impl

import android.content.Context
import android.content.IntentSender
import android.os.Looper
import com.huawei.hms.common.ApiException
import com.huawei.hms.location.LocationCallback
import com.huawei.hms.location.LocationRequest
import com.huawei.hms.location.LocationResult
import com.huawei.hms.location.LocationServices
import com.huawei.hms.location.LocationSettingsRequest
import com.huawei.hms.location.LocationSettingsStatusCodes
import dev.arpan.location.engine.HMSResolvableApiException
import dev.arpan.location.engine.ILocationFetcher
import dev.arpan.location.engine.LocationReceiver
import dev.arpan.location.engine.LocationSettingCallback
import dev.arpan.location.engine.ResolvableApiException
import dev.arpan.location.engine.model.Location
import timber.log.Timber

internal class HMSLocationProvider(
    private val context: Context,
    private val locationRequest: LocationRequest
) : ILocationFetcher {

    private val locationSettingsRequest = LocationSettingsRequest.Builder()
        .addLocationRequest(locationRequest)
        .setAlwaysShow(true)
        .build()

    private val fusedLocationProviderClient: com.huawei.hms.location.FusedLocationProviderClient =
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
            .addOnSuccessListener {
                callback.onResultSuccess()
            }.addOnFailureListener { e ->
                if (e is ApiException) {
                    Timber.e("ApiException -> statusCode:${e.statusCode} message:${e.message}")
                    if (e.statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                        try {
                            callback.onResultResolutionRequired(
                                ResolvableApiException(e as HMSResolvableApiException)
                            )
                        } catch (ignored: IntentSender.SendIntentException) {
                        } catch (ignored: ClassCastException) {
                        }
                    } else {
                        callback.onResultSettingsChangeUnavailable()
                    }
                } else {
                    callback.onResultSettingsChangeUnavailable()
                }
            }
    }

    override fun startLocationUpdates(receiver: LocationReceiver) {
        locationReceiver = receiver
        if (!isRequestingLocationUpdates) {
            try {
                fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.myLooper()
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
