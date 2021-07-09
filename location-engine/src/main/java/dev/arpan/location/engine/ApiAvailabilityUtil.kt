package dev.arpan.location.engine

import android.content.Context
import com.google.android.gms.common.GoogleApiAvailability
import com.huawei.hms.api.HuaweiApiAvailability

object ApiAvailabilityUtil {
    fun isHmsAvailable(context: Context): Boolean {
        val result = HuaweiApiAvailability.getInstance().isHuaweiMobileServicesAvailable(context)
        return result == com.huawei.hms.api.ConnectionResult.SUCCESS
    }

    fun isGmsAvailable(context: Context): Boolean {
        val result = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        return com.google.android.gms.common.ConnectionResult.SUCCESS == result
    }
}
