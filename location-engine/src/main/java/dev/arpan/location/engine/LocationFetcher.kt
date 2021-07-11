package dev.arpan.location.engine

import android.content.Context
import dev.arpan.location.engine.impl.GoogleLocationProvider
import dev.arpan.location.engine.impl.HMSLocationProvider
import dev.arpan.location.engine.util.ApiAvailabilityUtil

class LocationFetcher(
    context: Context,
    request: LocationRequest,
    receiver: LocationReceiver? = null
) : ILocationFetcher {

    private val locationFetcher: ILocationFetcher =
        if (ApiAvailabilityUtil.isGmsAvailable(context)) {
            GoogleLocationProvider(
                context,
                request.toGoogleLocationRequest(),
                receiver
            )
        } else {
            HMSLocationProvider(
                context,
                request.toHMSLocationRequest(),
                receiver
            )
        }

    override fun checkDeviceLocationSettings(callback: LocationSettingCallback) {
        locationFetcher.checkDeviceLocationSettings(callback)
    }

    override fun startLocationUpdates() {
        locationFetcher.startLocationUpdates()
    }

    override fun stopLocationUpdates() {
        locationFetcher.stopLocationUpdates()
    }
}
