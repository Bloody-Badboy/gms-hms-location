package dev.arpan.location.engine

import dev.arpan.location.engine.model.Location

interface LocationReceiver {
    fun onReceived(error: Throwable?, location: Location?)
}
