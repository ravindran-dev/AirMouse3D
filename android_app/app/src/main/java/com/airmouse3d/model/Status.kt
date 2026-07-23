package com.airmouse3d.model

/** State of the required motion sensors on this device. */
enum class SensorStatus {
    UNKNOWN,
    AVAILABLE,
    UNAVAILABLE,
}

/** Lifecycle state of the air-mouse foreground tracking session. */
enum class TrackingState {
    STOPPED,
    STARTING,
    RUNNING,
    STOPPING,
}
