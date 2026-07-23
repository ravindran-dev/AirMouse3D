package com.airmouse3d.utils

object Constants {
    /** Must match `pc_receiver::config::UDP_LISTEN_PORT`. Used as the default port when a
     *  scanned QR code only encodes a bare IP with no explicit port. */
    const val DEFAULT_PC_UDP_PORT = 7890

    /** Upload cadence for motion updates. A direct local UDP send is cheap enough to run at
     *  the fast end of the spec's 10-20ms range for the snappiest, most mouse-like feel. */
    const val MOTION_UPLOAD_INTERVAL_MS = 10L

    /** Requested sensor sampling period. 5ms ~= 200Hz, oversampling the 10ms upload loop
     *  so every upload tick has a fresh filtered reading. */
    const val SENSOR_SAMPLING_PERIOD_US = 5_000

    const val NOTIFICATION_CHANNEL_ID = "airmouse_tracking_channel"
    const val NOTIFICATION_ID = 1001

    const val DATASTORE_SETTINGS_NAME = "airmouse_settings"
    const val DATASTORE_CONNECTION_NAME = "airmouse_connection"

    /** How long we can go without an ack from the PC before considering the link lost. */
    const val CONNECTION_ACK_TIMEOUT_MS = 3_000L
}
