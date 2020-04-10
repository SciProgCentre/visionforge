package ru.mipt.npm.muon.monitor

import kotlin.test.*

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