package com.airmouse3d.model

/**
 * User-tunable motion parameters, persisted via DataStore and read live by
 * [com.airmouse3d.sensor.MotionProcessor].
 *
 * @param sensitivity multiplier applied to filtered angular velocity before it becomes dx/dy.
 *   The only motion parameter exposed to the user.
 * @param deadZone minimum angular-velocity magnitude (rad/s) required before any movement is
 *   emitted; below this, tiny hand tremor and sensor noise are suppressed to zero. Fixed at
 *   [DEFAULT_DEAD_ZONE] -- not user-tunable, to keep the UI to just click/scroll/sensitivity.
 */
data class AppSettings(
    val sensitivity: Float = DEFAULT_SENSITIVITY,
    val deadZone: Float = DEFAULT_DEAD_ZONE,
) {
    companion object {
        // dx/dy sent by this app are applied on the PC side with no further rescaling (see
        // pc_receiver's cursor::mapper), so this multiplier maps directly to on-screen pixels
        // per tick -- tuned down from an earlier value that assumed a second gain stage on the
        // receiver that no longer exists.
        const val DEFAULT_SENSITIVITY = 8f
        const val DEFAULT_DEAD_ZONE = 0.06f

        val SENSITIVITY_RANGE = 1f..25f
    }
}
