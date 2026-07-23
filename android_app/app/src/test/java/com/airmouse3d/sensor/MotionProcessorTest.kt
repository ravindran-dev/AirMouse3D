package com.airmouse3d.sensor

import com.airmouse3d.model.AppSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MotionProcessorTest {

    private val settings = AppSettings(sensitivity = 10f, deadZone = 0.05f)

    private fun MotionProcessor.settle(gyroX: Double, gyroY: Double, iterations: Int = 200): Pair<Double, Double> {
        var out = 0.0 to 0.0
        repeat(iterations) {
            out = process(gyroX = gyroX, gyroY = gyroY, dtSeconds = 0.005, settings = settings)
        }
        return out
    }

    @Test
    fun `phone at rest produces zero output`() {
        val processor = MotionProcessor()
        val (dx, dy) = processor.process(gyroX = 0.0, gyroY = 0.0, dtSeconds = 0.01, settings = settings)
        assertEquals(0.0, dx, 0.0001)
        assertEquals(0.0, dy, 0.0001)
    }

    @Test
    fun `tiny jitter below dead zone is suppressed`() {
        val processor = MotionProcessor()
        // Feed several samples so the filter settles, then check a sub-threshold value.
        repeat(20) {
            processor.process(gyroX = 0.01, gyroY = 0.01, dtSeconds = 0.005, settings = settings)
        }
        val (dx, dy) = processor.process(gyroX = 0.01, gyroY = 0.01, dtSeconds = 0.005, settings = settings)
        assertEquals(0.0, dx, 0.0001)
        assertEquals(0.0, dy, 0.0001)
    }

    @Test
    fun `sustained rotation above dead zone produces non-zero output`() {
        val processor = MotionProcessor()
        val (dx, dy) = processor.settle(gyroX = 1.0, gyroY = 1.0)
        assertTrue("expected dx to converge to a non-zero value", kotlin.math.abs(dx) > 1.0)
        assertTrue("expected dy to converge to a non-zero value", kotlin.math.abs(dy) > 1.0)
    }

    @Test
    fun `larger sustained input yields larger output than smaller input`() {
        // gyroX alone drives dx (dy depends only on gyroY -- see MotionProcessor's class doc).
        val smallDx = MotionProcessor().settle(gyroX = 0.3, gyroY = 0.0).first
        val largeDx = MotionProcessor().settle(gyroX = 1.5, gyroY = 0.0).first
        assertTrue(kotlin.math.abs(largeDx) > kotlin.math.abs(smallDx))
    }

    @Test
    fun `pointer acceleration makes fast motion more than proportionally faster`() {
        // With a ballistic curve, output should grow super-linearly with input speed: a 10x
        // faster tilt should move the cursor *more* than 10x as far per frame.
        val lowDead = AppSettings(sensitivity = 10f, deadZone = 0.01f)
        fun settleFirst(gyro: Double): Double {
            val p = MotionProcessor()
            var v = 0.0
            repeat(200) { v = p.process(gyroX = gyro, gyroY = 0.0, dtSeconds = 0.005, settings = lowDead).first }
            return kotlin.math.abs(v)
        }
        val slow = settleFirst(0.2)
        val fast = settleFirst(2.0)
        assertTrue("expected super-linear (accelerated) growth", fast / slow > 2.0 / 0.2)
    }

    @Test
    fun `tilting the far edge down moves the cursor down`() {
        // Far edge dipping = negative gyroX. After the up/down flip that must yield dx > 0,
        // which pc_receiver maps to screen_y > 0 = downward cursor motion.
        val dx = MotionProcessor().settle(gyroX = -1.0, gyroY = 0.0).first
        assertTrue("far-edge-down should drive the cursor down (dx > 0)", dx > 0.0)
    }

    @Test
    fun `tilting right moves the cursor right`() {
        // Tilt right = positive gyroY -> dy < 0, which pc_receiver maps to screen_x = -dy > 0 =
        // rightward cursor motion (horizontal is unchanged by the up/down flip).
        val dy = MotionProcessor().settle(gyroX = 0.0, gyroY = 1.0).second
        assertTrue("tilt-right should drive the cursor right (dy < 0)", dy < 0.0)
    }

    @Test
    fun `violent shake is rejected and yields zero output`() {
        val processor = MotionProcessor()
        val (dx, dy) = processor.process(gyroX = 20.0, gyroY = 20.0, dtSeconds = 0.005, settings = settings)
        assertEquals(0.0, dx, 0.0001)
        assertEquals(0.0, dy, 0.0001)
    }

    @Test
    fun `reset clears accumulated filter state`() {
        val processor = MotionProcessor()
        processor.settle(gyroX = 1.0, gyroY = 1.0)
        processor.reset()
        val (dx, dy) = processor.process(gyroX = 0.0, gyroY = 0.0, dtSeconds = 0.005, settings = settings)
        assertEquals(0.0, dx, 0.0001)
        assertEquals(0.0, dy, 0.0001)
    }

    @Test
    fun `output never exceeds the per-frame safety clamp`() {
        val processor = MotionProcessor()
        val hotSettings = AppSettings(sensitivity = 1000f, deadZone = 0f)
        var dx = 0.0
        var dy = 0.0
        repeat(500) {
            val result = processor.process(gyroX = 3.0, gyroY = 3.0, dtSeconds = 0.005, settings = hotSettings)
            dx = result.first
            dy = result.second
        }
        assertTrue(kotlin.math.abs(dx) <= MotionProcessor.MAX_OUTPUT_PER_FRAME + 0.0001)
        assertTrue(kotlin.math.abs(dy) <= MotionProcessor.MAX_OUTPUT_PER_FRAME + 0.0001)
    }
}
