package com.airmouse3d.model

import kotlinx.serialization.Serializable

/**
 * A single motion update, JSON-serialized and sent as one UDP datagram to the PC receiver.
 *
 * Field names and types intentionally mirror `pc_receiver::model::motion_data::MotionPayload`
 * (dx: f64, dy: f64, scroll: f64, click: bool, clickType: String, timestamp: u64) exactly --
 * there is no document/wrapper structure anymore, the serialized JSON *is* the packet payload.
 */
@Serializable
data class MotionSample(
    val dx: Double = 0.0,
    val dy: Double = 0.0,
    val scroll: Double = 0.0,
    val click: Boolean = false,
    val clickType: String = ClickType.NONE.name,
    val timestamp: Long = 0L,
) {
    companion object {
        val IDLE = MotionSample()
    }
}
