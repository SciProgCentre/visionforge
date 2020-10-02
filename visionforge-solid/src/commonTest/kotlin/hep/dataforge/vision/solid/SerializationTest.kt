package hep.dataforge.vision.solid

import hep.dataforge.names.Name
import hep.dataforge.names.toName
import hep.dataforge.vision.MutableVisionGroup
import hep.dataforge.vision.Vision
import hep.dataforge.vision.get
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Create and attach new proxied group
 */
fun SolidGroup.proxyGroup(
    name: String,
    templateName: Name = name.toName(),
    block: MutableVisionGroup.() -> Unit
): Proxy {
    val group = SolidGroup().apply(block)
    return proxy(name, group, templateName)
}


class SerializationTest {
    @Test
    fun testCubeSerialization() {
        val cube = Box(100f, 100f, 100f).apply {
            color(222)
            x = 100
            z = -100
        }
        val string =  cube.encodeToString()
        println(string)
        val newCube = Vision.decodeFromString(string)
        assertEquals(cube.config, newCube.config)
    }

    @Test
    fun testProxySerialization() {
        val cube = Box(100f, 100f, 100f).apply {
            color(222)
            x = 100
            z = -100
        }
        val group = SolidGroup{
            proxy("cube", cube)
            proxyGroup("pg", "pg.content".toName()){
                sphere(50){
                    x = -100
                }
            }
        }
        val string = group.encodeToString()
        println(string)
        val reconstructed = SolidGroup.decodeFromString(string)
        assertEquals(group["cube"]?.config, reconstructed["cube"]?.config)
    }

}