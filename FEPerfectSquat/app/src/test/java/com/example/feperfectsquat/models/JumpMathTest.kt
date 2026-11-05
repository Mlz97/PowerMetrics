package com.example.feperfectsquat.models

import org.junit.Assert.assertEquals
import org.junit.Test

class JumpMathTest {

    @Test
    fun sayersPeakPowerMatchesTheFormula() {
        // 60.7 * 41.2 + 45.3 * 82 - 2055 = 2500.84 + 3714.6 - 2055 = 4160.44
        assertEquals(4160, sayersPeakPowerW(41.2, 82.0))
    }

    @Test
    fun flightTimeComesFromJumpHeight() {
        // t = 2 * sqrt(2h / g) = 2 * sqrt(0.824 / 9.81) = 0.5796 s
        assertEquals(580, flightTimeMs(41.2))
    }
}
