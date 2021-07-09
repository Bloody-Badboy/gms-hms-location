package dev.arpan.location.engine.demo

import android.Manifest
import android.app.Activity
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import dev.arpan.location.engine.LocationData
import dev.arpan.location.engine.LocationFetcher
import dev.arpan.location.engine.LocationReceiver
import dev.arpan.location.engine.LocationSettingCallback
import dev.arpan.location.engine.ResolvableApiException
import dev.arpan.location.engine.demo.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onLocationPermissionGranted()
        } else {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private val intentSenderLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            startLocationUpdates()
        } else {
            Toast.makeText(this, "GPS not enabled!", Toast.LENGTH_SHORT).show()
        }
    }

    private val fetcher: LocationFetcher by lazy {
        LocationFetcher(
            context = this,
            updateInterval = 4000L,
            locationReceiver = object : LocationReceiver {
                override fun onReceived(locationData: LocationData) {
                    binding.tvLocation.text = locationData.toString()
                    getAddress(locationData)
                }
            }
        )
    }

    private val getCoder: Geocoder by lazy { Geocoder(this, Locale.getDefault()) }
    private var geoCoderJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.button.setOnClickListener {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun onLocationPermissionGranted() {
        fetcher.checkDeviceLocationSettings(object : LocationSettingCallback {
            override fun onResultResolutionRequired(resolvable: ResolvableApiException) {
                intentSenderLauncher.launch(
                    IntentSenderRequest.Builder(resolvable.getResolution()).build()
                )
            }

            override fun onResultSettingsChangeUnavailable() {
                Timber.d("onResultSettingsChangeUnavailable")
            }

            override fun onResultSuccess() {
                Timber.d("onResultSuccess")
                startLocationUpdates()
            }
        })
    }

    private fun startLocationUpdates() {
        fetcher.startLocationUpdates()
    }

    private fun getAddress(latLong: LocationData) {
        geoCoderJob?.cancel()
        geoCoderJob = lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    getCoder.getFromLocation(latLong.latitude, latLong.longitude, 1)
                }
            }
            if (result.isSuccess) {
                val addresses = result.getOrThrow()
                if (addresses.isEmpty()) {
                    binding.tvAddress.text = "Sorry, no address found"
                } else {
                    binding.tvAddress.text = addresses[0].displayableAddress
                }
            }
        }
    }
}

private val Address.displayableAddress: String
    get() {
        val featureName = featureName
        val addressLines = ArrayList<String>()

        for (i in 0..maxAddressLineIndex) {
            addressLines.add(getAddressLine(i))
        }

        var displayableAddress = addressLines.joinToString()

        try {
            if (featureName != null && featureName.matches(".*\\d+.*".toRegex())) {
                displayableAddress =
                    displayableAddress.replaceFirst("$featureName,".toRegex(), "")
                        .trim { it <= ' ' }
            }
        } catch (ignored: Throwable) {
        }

        return displayableAddress.trim { it <= ' ' }
    }
