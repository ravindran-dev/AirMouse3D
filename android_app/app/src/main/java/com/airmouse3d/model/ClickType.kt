package com.airmouse3d.model

/**
 * The mouse action encoded by a touch gesture on the on-screen click pad.
 *
 * Kept as an explicit enum (rather than only a boolean) so the wire format can
 * evolve to a full left/right/middle click without breaking the existing Rust
 * PC receiver, which only reads the legacy [MotionSample.click] boolean today.
 */
enum class ClickType {
    NONE,
    LEFT,
    RIGHT,
    MIDDLE;

    companion object {
        fun fromWire(value: String?): ClickType =
            entries.firstOrNull { it.name == value } ?: NONE
    }
}
