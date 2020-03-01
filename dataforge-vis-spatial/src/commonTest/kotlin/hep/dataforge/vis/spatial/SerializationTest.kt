package hep.dataforge.vis.spatial

import hep.dataforge.vis.spatial.Visual3D.Companion.json
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlin.test.Test
import kotlin.test.assertEquals

class SerializationTest {
    @ImplicitReflectionSerializer
    @Test
    fun testCubeSerialization() {
        val cube = Box(100f, 100f, 100f).apply {
            color(222)
            x = 100
            z = -100
        }
        val string = json.stringify(Box.serializer(), cube)
        println(string)
        val newCube = json.parse(Box.serializer(), string)
        assertEquals(cube.config, newCube.config)
    }
}