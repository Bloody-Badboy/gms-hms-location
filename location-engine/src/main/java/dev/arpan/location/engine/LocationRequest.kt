package dev.arpan.location.engine

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.android.gms.location.LocationRequest as GoogleLocationRequest
import com.huawei.hms.location.LocationRequest as HMSLocationRequest

@Parcelize
data class LocationRequest(
    var priority: Int = PRIORITY_HIGH_ACCURACY,
    var interval: Long = 10000L,
    var fastestInterval: Long = (interval / 6.0).toLong(),
    var smallestDisplacement: Float = 0.0f
) : Parcelable {
    companion object {
        const val PRIORITY_HIGH_ACCURACY = 100
        const val PRIORITY_BALANCED_POWER_ACCURACY = 102
        const val PRIORITY_LOW_POWER = 104
        const val PRIORITY_NO_POWER = 105
    }

    fun toHMSLocationRequest(): HMSLocationRequest {
        val request = HMSLocationRequest()
        request.priority = priority
        request.interval = interval
        request.fastestInterval = fastestInterval
        request.smallestDisplacement = smallestDisplacement
        return request
    }

    fun toGoogleLocationRequest(): GoogleLocationRequest {
        val request = GoogleLocationRequest.create()
        request.priority = priority
        request.interval = interval
        request.fastestInterval = fastestInterval
        request.smallestDisplacement = smallestDisplacement
        return request
    }
}
