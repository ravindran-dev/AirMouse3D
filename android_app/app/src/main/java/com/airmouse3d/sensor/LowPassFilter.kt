package com.airmouse3d.sensor

/**
 * First-order exponential smoothing primitive: `output += alpha * (input - output)`.
 *
 * The smoothing factor `alpha` is supplied per call rather than fixed at construction, so
 * callers can vary it with the actual elapsed time and — for an adaptive filter like
 * [OneEuroFilter] — with the signal's speed. `alpha` ranges over [0, 1]: 1 passes the input
 * straight through (no smoothing), small values smooth heavily.
 */
class LowPassFilter {
    var initialized = false
        private set
    var lastValue = 0.0
        private set

    fun filter(input: Double, alpha: Double): Double {
        lastValue = if (!initialized) {
            initialized = true
            input
        } else {
            lastValue + alpha * (input - lastValue)
        }
        return lastValue
    }

    fun reset() {
        initialized = false
        lastValue = 0.0
    }
}
