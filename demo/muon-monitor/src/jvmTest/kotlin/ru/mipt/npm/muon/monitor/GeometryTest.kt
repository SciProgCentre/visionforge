package ru.mipt.npm.muon.monitor

import org.junit.Test
import kotlin.test.assertTrue

class GeometryTest {

    @Test
    fun testLoadGeometry(){
        assertTrue { readMonitorConfig().isNotBlank() }
    }

    @Test
    fun testLoadModel(){
        assertTrue { Monitor.pixels.isNotEmpty() }
    }
}