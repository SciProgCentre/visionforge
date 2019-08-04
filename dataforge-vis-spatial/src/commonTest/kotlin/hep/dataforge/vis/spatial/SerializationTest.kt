package hep.dataforge.vis.spatial

import hep.dataforge.context.Global
import kotlin.test.Test
import kotlin.test.assertEquals

class SerializationTest {
    @Test
    fun testCubeSerialization(){
        val cube = Box(null,100f,100f,100f).apply{
            color(222)
        }
        val meta = cube.toMeta()
        println(meta)
        val newCube = Box(Global,null, meta)
        assertEquals(cube,newCube)
    }
}