package hep.dataforge.vis.spatial

import hep.dataforge.vis.get
import hep.dataforge.vis.spatial.Visual3D.Companion.json
import kotlin.test.Test
import kotlin.test.assertEquals

class SerializationTest {
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

    @Test
    fun testProxySerialization() {
        val cube = Box(100f, 100f, 100f).apply {
            color(222)
            x = 100
            z = -100
        }

        val group = VisualGroup3D().apply {
            proxy("cube", cube)
        }
        val string = group.stringify()
        println(string)
        val reconstructed = VisualGroup3D.parseJson(string)
        assertEquals(group["cube"]?.config, reconstructed["cube"]?.config)
    }
}