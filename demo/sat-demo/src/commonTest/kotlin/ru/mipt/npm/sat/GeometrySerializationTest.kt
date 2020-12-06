package ru.mipt.npm.sat

import hep.dataforge.context.Global
import hep.dataforge.vision.solid.SolidManager
import kotlin.test.Test
import kotlin.test.assertEquals

class GeometrySerializationTest {
    @Test
    fun testSerialization(){
        val geometry = visionOfSatellite()
        val manager = Global.plugins.fetch(SolidManager)
        val string = manager.visionManager.encodeToString(geometry)
        val reconstructed = manager.visionManager.decodeFromString(string)
        assertEquals(geometry.config,reconstructed.config)
    }
}