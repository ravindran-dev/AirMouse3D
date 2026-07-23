package com.airmouse3d.sensor

import com.airmouse3d.model.ClickType
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cross-thread mailbox for the touch-driven parts of the motion payload: scroll (two-finger
 * drag on the Home screen) and click (tap/double-tap/long-press on the click pad). Gyroscope
 * motion is continuously sampled and filtered by [SensorFusionManager]; scroll and click are
 * discrete UI events instead, so they are accumulated here on the main thread by Compose
 * gesture callbacks and drained by the foreground service's upload loop on each tick.
 *
 * Backed by [AtomicReference] rather than a Mutex/coroutine primitive: both sides only ever
 * do a single lock-free get-and-set, so plain atomics are simpler and cheaper than a suspend
 * lock here.
 */
@Singleton
class GestureInputBus @Inject constructor() {
    private val scrollAccumulator = AtomicReference(0.0)
    private val pendingClick = AtomicReference(ClickType.NONE)

    fun addScroll(delta: Double) {
        scrollAccumulator.getAndUpdate { it + delta }
    }

    fun triggerClick(type: ClickType) {
        pendingClick.set(type)
    }

    /** Called once per upload tick: returns and clears the accumulated scroll delta. */
    fun drainScroll(): Double = scrollAccumulator.getAndSet(0.0)

    /** Called once per upload tick: returns and clears any pending click (one-shot). */
    fun drainClick(): ClickType = pendingClick.getAndSet(ClickType.NONE)
}
