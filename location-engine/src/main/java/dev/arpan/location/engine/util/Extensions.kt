package dev.arpan.location.engine.util

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dev.arpan.location.engine.LocationFetcher
import dev.arpan.location.engine.LocationReceiver
import dev.arpan.location.engine.model.Location
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

val Context.isLocationPermissionGranted
    get() = ContextCompat.checkSelfPermission(
        this,
        ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

val Activity.isLocationPermissionPermanentlyDenied
    get() = !ActivityCompat.shouldShowRequestPermissionRationale(
        this,
        ACCESS_FINE_LOCATION
    )

@ExperimentalCoroutinesApi
@RequiresPermission(anyOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
fun LocationFetcher.asFlow() = callbackFlow {
    startLocationUpdates(object : LocationReceiver {
        override fun onReceived(error: Throwable?, location: Location?) {
            if (error != null) {
                close(error)
            }
            if (location != null) {
                if (!isClosedForSend) {
                    trySend(location)
                }
            }
        }
    })
    awaitClose {
        stopLocationUpdates()
    }
}
