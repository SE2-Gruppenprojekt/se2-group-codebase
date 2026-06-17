package at.aau.serg.android.ui.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ShakeDetectorTest {

    @Test
    fun shakeTriggers() {
        var count = 0
        val detector = ShakeDetector { count++ }

        detector.handleValues(30f, 0f, 0f, now = 1000L)
        assertEquals(1, count)

        detector.handleValues(30f, 0f, 0f, now = 2000L)
        assertEquals(2, count)
    }

    @Test
    fun noShakeBelowThreshold() {
        var count = 0
        val detector = ShakeDetector { count++ }

        detector.handleValues(5f, 5f, 5f, now = 0L)
        assertEquals(0, count)
    }
}
