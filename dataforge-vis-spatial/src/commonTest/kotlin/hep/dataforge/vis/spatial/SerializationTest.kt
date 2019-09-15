package hep.dataforge.vis.spatial

import hep.dataforge.context.Global
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlin.test.Test
import kotlin.test.assertEquals

class SerializationTest {
    @ImplicitReflectionSerializer
    @Test
    fun testCubeSerialization(){
        val cube = Box(100f,100f,100f).apply{
            color(222)
        }
        val meta = cube.toMeta()
        println(meta)
        val newCube = Box(Global,null, meta)
        assertEquals(cube.toMeta(),newCube.toMeta())
    }
}