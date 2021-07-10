package dev.arpan.location.engine.demo

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import dev.arpan.location.engine.LocationFetcher
import dev.arpan.location.engine.LocationReceiver
import dev.arpan.location.engine.LocationSettingsActivity
import dev.arpan.location.engine.demo.databinding.ActivityMainBinding
import dev.arpan.location.engine.model.Location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        when (activityResult.resultCode) {
            RESULT_OK -> {
                fetcher.startLocationUpdates()
            }
            LocationSettingsActivity.RESULT_PERMISSION_DENIED -> {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
            LocationSettingsActivity.RESULT_PERMANENTLY_PERMISSION_DENIED -> {
                Toast.makeText(this, "Location permission permanently denied", Toast.LENGTH_SHORT)
                    .show()
            }
            LocationSettingsActivity.RESULT_GPS_NOT_ENABLED -> {
                Toast.makeText(this, "GPS not enabled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val fetcher: LocationFetcher by lazy {
        LocationFetcher(
            context = this,
            updateInterval = 4000L,
            locationReceiver = object : LocationReceiver {
                override fun onReceived(location: Location) {
                    binding.tvLocation.text = location.toString()
                    getAddress(location)
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
            activityResultLauncher.launch(Intent(this, LocationSettingsActivity::class.java))
        }
    }

    private fun getAddress(latLong: Location) {
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
