package com.airmouse3d.sensor

import kotlin.math.PI
import kotlin.math.abs

/**
 * The "1€ (One Euro) filter" (Casiez, Roussel & Vogel, CHI 2012): an adaptive low-pass filter
 * designed specifically for filtering noisy input for interactive pointing. It dynamically
 * trades smoothing against lag based on how fast the signal is changing:
 *
 *  - When the input barely changes (careful, precise aiming) it uses a **low** cutoff -> strong
 *    smoothing -> jitter and hand tremor are removed, so the cursor sits rock-steady on a target.
 *  - When the input changes fast (a deliberate flick) it raises the cutoff -> little smoothing
 *    -> minimal lag, so the cursor keeps up with the hand instead of dragging behind it.
 *
 * This is strictly better for a pointer than the fixed-cutoff low-pass it replaces here: a fixed
 * cutoff can only sit at one point on the smoothness-vs-lag curve — steady but laggy, or snappy
 * but jittery — while this gets both. Fully deterministic, no machine learning.
 *
 * @param minCutoffHz cutoff when the signal is essentially still. Sets steady-state smoothness;
 *   lower is steadier (and would be laggier if this were the only term — it isn't).
 * @param beta how strongly the cutoff rises with signal speed. Sets responsiveness to fast
 *   motion; higher means less lag on quick flicks.
 * @param dCutoffHz cutoff for the internal speed (derivative) estimate. 1 Hz is the paper's
 *   standard default and rarely needs changing.
 */
class OneEuroFilter(
    private val minCutoffHz: Double,
    private val beta: Double,
    private val dCutoffHz: Double = 1.0,
) {
    private val valueFilter = LowPassFilter()
    private val derivativeFilter = LowPassFilter()
    private var lastInput = 0.0

    fun filter(input: Double, dtSeconds: Double): Double {
        // Estimate the signal's rate of change, then smooth that estimate too (it's noisy).
        // valueFilter.initialized doubles as "have we seen a previous sample yet".
        val rawDerivative = if (valueFilter.initialized) (input - lastInput) / dtSeconds else 0.0
        lastInput = input
        val speed = derivativeFilter.filter(rawDerivative, alpha(dCutoffHz, dtSeconds))

        // Speed-dependent cutoff: still -> minCutoff (heavy smoothing); fast -> raised cutoff
        // (light smoothing, low lag).
        val cutoff = minCutoffHz + beta * abs(speed)
        return valueFilter.filter(input, alpha(cutoff, dtSeconds))
    }

    fun reset() {
        valueFilter.reset()
        derivativeFilter.reset()
        lastInput = 0.0
    }

    /** Exponential-smoothing factor for a given cutoff frequency and elapsed time. */
    private fun alpha(cutoffHz: Double, dtSeconds: Double): Double {
        val tau = 1.0 / (2.0 * PI * cutoffHz)
        return 1.0 / (1.0 + tau / dtSeconds)
    }
}
