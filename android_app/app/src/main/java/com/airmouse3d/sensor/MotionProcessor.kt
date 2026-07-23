package com.airmouse3d.sensor

import com.airmouse3d.model.AppSettings
import kotlin.math.hypot
import kotlin.math.min

/**
 * Turns raw gyroscope angular velocity into stable, filtered, mouse-like cursor deltas.
 *
 * The phone is held flat like a physical mouse, screen up, top edge pointing away from the
 * user -- the device's natural axes are then: +X to the phone's right, +Y toward the top edge
 * (away from the user), +Z straight up out of the screen. Android reports gyroscope rotation
 * with the right-hand rule about each axis (positive = counter-clockwise viewed from the
 * positive end of the axis). For a phone lying flat:
 *  - `gyroX` (rotation about the right-pointing axis) is the *pitch* axis: negative when the
 *    far/top edge dips down, positive when the near edge dips -- i.e. tilting forward/backward.
 *  - `gyroY` (rotation about the away-pointing axis) is the *roll* axis: positive when the
 *    right edge dips down, negative when the left edge dips -- i.e. tilting left/right.
 *
 * Composed with `pc_receiver::cursor::mapper`'s fixed `screen_x = -dy, screen_y = dx` remap
 * (screen_y positive = down), the direction mapping below yields:
 *  - Tilt **right** -> cursor **right**, tilt left -> left.
 *  - Tilt the **far edge down** -> cursor **down**, near edge down -> up. (Vertical is inverted
 *    on purpose, per the "flip up/down only" request; horizontal is unchanged.)
 *
 * Using *angular velocity* rather than absolute tilt angle is what makes "phone at rest ->
 * cursor stops immediately" trivially true (zero rotation rate -> zero output): you must keep
 * rotating to keep moving, holding a fixed tilt does not drift the cursor away.
 *
 * Pipeline per sample, each stage deterministic and independently testable:
 *   shake rejection -> 1€ adaptive smoothing -> soft dead zone -> pointer acceleration ->
 *   sensitivity scale -> per-frame clamp.
 *
 * The two upgrades that make this feel like a real mouse rather than a laggy tilt-pad:
 *  1. [OneEuroFilter] smooths adaptively -- steady and jitter-free when aiming slowly, low-lag
 *     when flicking fast -- instead of a single fixed-cutoff compromise.
 *  2. Pointer acceleration (ballistics): gain rises with hand speed, so slow tilts stay precise
 *     for fine targeting while fast tilts cover the whole screen quickly, exactly like a mouse
 *     driven through the OS pointer-speed curve.
 */
class MotionProcessor(
    minCutoffHz: Double = DEFAULT_MIN_CUTOFF_HZ,
    beta: Double = DEFAULT_BETA,
) {
    private val xFilter = OneEuroFilter(minCutoffHz, beta)
    private val yFilter = OneEuroFilter(minCutoffHz, beta)

    /**
     * @param gyroX angular velocity in rad/s about the phone's right-pointing (pitch) axis --
     *   negative when the far/top edge tilts down.
     * @param gyroY angular velocity in rad/s about the phone's away-pointing (roll) axis --
     *   positive when the right edge tilts down.
     * @param dtSeconds time since the previous sample.
     * @return filtered (dx, dy) cursor delta, or (0.0, 0.0) when the phone is at rest, within the
     *   dead zone, or a shake was detected and rejected.
     */
    fun process(gyroX: Double, gyroY: Double, dtSeconds: Double, settings: AppSettings): Pair<Double, Double> {
        val safeDt = dtSeconds.coerceIn(MIN_DT_SECONDS, MAX_DT_SECONDS)
        val deadZone = settings.deadZone.toDouble()
        val sensitivity = settings.sensitivity.toDouble()

        // 1. Shake rejection: a violent, implausibly fast rotation is treated as noise, not a
        //    pointing gesture. Reset the filters so the spike doesn't bleed into later samples.
        if (hypot(gyroX, gyroY) > SHAKE_REJECT_THRESHOLD_RAD_S) {
            xFilter.reset()
            yFilter.reset()
            return 0.0 to 0.0
        }

        // 2. Adaptive 1€ smoothing (see OneEuroFilter): precision at rest, responsiveness in
        //    motion.
        val fx = xFilter.filter(gyroX, safeDt)
        val fy = yFilter.filter(gyroY, safeDt)

        // 3. Soft dead zone: below the threshold -> exactly zero (a still phone never drifts);
        //    just above -> output ramps up from zero (we subtract the threshold) so crossing it
        //    has no visible jump, unlike a hard cutoff.
        val speed = hypot(fx, fy)
        if (speed < deadZone) return 0.0 to 0.0
        val keep = (speed - deadZone) / speed
        val ux = fx * keep
        val uy = fy * keep

        // 4. Pointer acceleration: gain grows with hand speed (capped), so slow careful tilts
        //    stay precise while fast flicks cover the screen -- the hallmark of a real mouse.
        val accel = min(1.0 + ACCEL_GAIN * speed, MAX_ACCEL_MULTIPLIER)
        val gain = sensitivity * accel

        // 5. Direction mapping (vertical inverted per the flip request) + a final safety clamp so
        //    a single frame can never move the cursor further than MAX_OUTPUT_PER_FRAME.
        val rawDx = -ux * gain
        val rawDy = -uy * gain

        val dx = rawDx.coerceIn(-MAX_OUTPUT_PER_FRAME, MAX_OUTPUT_PER_FRAME)
        val dy = rawDy.coerceIn(-MAX_OUTPUT_PER_FRAME, MAX_OUTPUT_PER_FRAME)

        return dx to dy
    }

    fun reset() {
        xFilter.reset()
        yFilter.reset()
    }

    companion object {
        // 1€ filter tuning. minCutoff sets steady-state smoothness; beta sets how fast the
        // filter "opens up" during quick motion. These are deliberately gentle: strong jitter
        // rejection when still, near-zero lag on flicks.
        const val DEFAULT_MIN_CUTOFF_HZ = 1.2
        const val DEFAULT_BETA = 0.05

        const val SHAKE_REJECT_THRESHOLD_RAD_S = 6.0

        // Pointer-acceleration curve: effective gain = sensitivity * min(1 + ACCEL_GAIN*speed,
        // MAX_ACCEL_MULTIPLIER), with speed in rad/s. At rest -> ~1x (fine control); fast flick
        // -> up to MAX_ACCEL_MULTIPLIER x (quick screen traversal).
        const val ACCEL_GAIN = 0.6
        const val MAX_ACCEL_MULTIPLIER = 4.0

        const val MAX_OUTPUT_PER_FRAME = 80.0
        private const val MIN_DT_SECONDS = 0.0005
        private const val MAX_DT_SECONDS = 0.1
    }
}
