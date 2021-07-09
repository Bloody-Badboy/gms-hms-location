package dev.arpan.location.engine

internal interface ILocationFetcher {

    fun checkDeviceLocationSettings(callback: LocationSettingCallback)

    fun startLocationUpdates()

    fun stopLocationUpdates()
}
