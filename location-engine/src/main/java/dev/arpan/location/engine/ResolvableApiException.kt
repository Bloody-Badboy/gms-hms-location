package dev.arpan.location.engine

import android.app.Activity
import android.app.PendingIntent
import android.content.IntentSender.SendIntentException

typealias GoogleResolvableApiException = com.google.android.gms.common.api.ResolvableApiException
typealias HMSResolvableApiException = com.huawei.hms.common.ResolvableApiException

class ResolvableApiException(private val resolvableApiException: Exception) {

    @Throws(SendIntentException::class)
    fun startResolutionForResult(activity: Activity, code: Int) {
        when (resolvableApiException) {
            is HMSResolvableApiException -> {
                resolvableApiException.startResolutionForResult(activity, code)
            }
            is GoogleResolvableApiException -> {
                resolvableApiException.startResolutionForResult(activity, code)
            }
            else -> {
                throw IllegalArgumentException(
                    "$resolvableApiException must be ${HMSResolvableApiException::class.java} or" +
                        " ${GoogleResolvableApiException::class.java}"
                )
            }
        }
    }

    fun getResolution(): PendingIntent {
        return when (resolvableApiException) {
            is HMSResolvableApiException -> {
                resolvableApiException.resolution
            }
            is GoogleResolvableApiException -> {
                resolvableApiException.resolution
            }
            else -> {
                throw IllegalArgumentException(
                    "$resolvableApiException must be ${HMSResolvableApiException::class.java} or" +
                        " ${GoogleResolvableApiException::class.java}"
                )
            }
        }
    }
}
