package dev.arpan.location.engine

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import dev.arpan.location.engine.util.isLocationPermissionGranted
import dev.arpan.location.engine.util.isLocationPermissionPermanentlyDenied
import timber.log.Timber

class LocationSettingsActivity : Activity() {
    companion object {
        private const val REQUEST_CODE_LOCATION_PERMISSION = 100
        private const val REQUEST_CODE_LOCATION_SETTINGS = 101

        const val RESULT_PERMISSION_DENIED = 0
        const val RESULT_PERMANENTLY_PERMISSION_DENIED = 1
        const val RESULT_GPS_NOT_ENABLED = 2
    }

    private val fetcher: LocationFetcher by lazy(LazyThreadSafetyMode.NONE) {
        LocationFetcher(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isLocationPermissionGranted) {
            requestLocationPermission()
        } else {
            checkDeviceLocationSettings()
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_CODE_LOCATION_PERMISSION
        )
    }

    private fun checkDeviceLocationSettings() {
        fetcher.checkDeviceLocationSettings(object : LocationSettingCallback {
            override fun onResultResolutionRequired(resolvable: ResolvableApiException) {
                Timber.d("Resolution required")
                resolvable.startResolutionForResult(
                    this@LocationSettingsActivity,
                    REQUEST_CODE_LOCATION_SETTINGS
                )
            }

            override fun onResultSettingsChangeUnavailable() {
                Timber.e("Location settings change unavailable")
                setResult(RESULT_GPS_NOT_ENABLED)
                finish()
            }

            override fun onResultSuccess() {
                Timber.d("Location settings changed")
                setResult(RESULT_OK)
                finish()
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkDeviceLocationSettings()
        } else {
            if (isLocationPermissionPermanentlyDenied) {
                Timber.d("Location permission permanently denied")
                setResult(RESULT_PERMANENTLY_PERMISSION_DENIED)
                finish()
            } else {
                Timber.d("Location permission denied")
                setResult(RESULT_PERMISSION_DENIED)
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_LOCATION_SETTINGS && resultCode == RESULT_OK) {
            Timber.d("Location settings changed")
            setResult(RESULT_OK)
            finish()
        } else {
            setResult(RESULT_GPS_NOT_ENABLED)
            finish()
        }
    }
}
