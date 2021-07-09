package dev.arpan.location.engine

import android.content.Context
import dev.arpan.location.engine.impl.GoogleLocationProvider
import dev.arpan.location.engine.impl.HMSLocationProvider

class LocationFetcher(
    context: Context,
    updateInterval: Long = 10000L,
    locationReceiver: LocationReceiver? = null
) : ILocationFetcher {

    private val locationFetcher: ILocationFetcher =
        if (ApiAvailabilityUtil.isGmsAvailable(context)) {
            GoogleLocationProvider(context, updateInterval, locationReceiver)
        } else {
            HMSLocationProvider(context, updateInterval, locationReceiver)
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
