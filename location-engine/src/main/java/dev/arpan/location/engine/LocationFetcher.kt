package dev.arpan.location.engine

import android.content.Context
import dev.arpan.location.engine.impl.GoogleLocationProvider
import dev.arpan.location.engine.impl.HMSLocationProvider
import dev.arpan.location.engine.util.ApiAvailabilityUtil

class LocationFetcher(
    context: Context,
    request: LocationRequest
) : ILocationFetcher {

    private val locationFetcher: ILocationFetcher by lazy {
        if (ApiAvailabilityUtil.isGmsAvailable(context)) {
            GoogleLocationProvider(
                context,
                request.toGoogleLocationRequest()
            )
        } else {
            HMSLocationProvider(
                context,
                request.toHMSLocationRequest()
            )
        }
    }

    override fun checkDeviceLocationSettings(callback: LocationSettingCallback) {
        locationFetcher.checkDeviceLocationSettings(callback)
    }

    override fun startLocationUpdates(receiver: LocationReceiver) {
        locationFetcher.startLocationUpdates(receiver)
    }

    override fun stopLocationUpdates() {
        locationFetcher.stopLocationUpdates()
    }
}
