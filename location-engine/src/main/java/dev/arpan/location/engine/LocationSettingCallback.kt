package dev.arpan.location.engine

interface LocationSettingCallback {

    fun onResultResolutionRequired(resolvable: ResolvableApiException)

    fun onResultSettingsChangeUnavailable()

    fun onResultSuccess()
}
