package hep.dataforge.vision.solid

import hep.dataforge.names.toName
import hep.dataforge.vision.Vision
import hep.dataforge.vision.get
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
        val string =  cube.stringify()
        println(string)
        val newCube = Vision.parseJson(string)
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
        val string = group.stringify()
        println(string)
        val reconstructed = SolidGroup.parseJson(string)
        assertEquals(group["cube"]?.config, reconstructed["cube"]?.config)
    }

}