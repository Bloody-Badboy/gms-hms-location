package dev.arpan.location.engine

interface LocationReceiver {
    fun onReceived(locationData: LocationData)
}
