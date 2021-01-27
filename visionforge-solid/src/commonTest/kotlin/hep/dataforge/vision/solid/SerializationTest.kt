package hep.dataforge.vision.solid

import hep.dataforge.names.Name
import hep.dataforge.names.toName
import hep.dataforge.vision.MutableVisionGroup
import hep.dataforge.vision.get
import hep.dataforge.vision.meta
import kotlin.test.Test
import kotlin.test.assertEquals


/**
 * Create and attach new proxied group
 */
fun SolidGroup.refGroup(
    name: String,
    templateName: Name = name.toName(),
    block: MutableVisionGroup.() -> Unit
): SolidReferenceGroup {
    val group = SolidGroup().apply(block)
    return ref(name, group, templateName)
}


class SerializationTest {
    @Test
    fun testCubeSerialization() {
        val cube = Box(100f, 100f, 100f).apply {
            color(222)
            x = 100
            z = -100
        }
        val string =  SolidManager.encodeToString(cube)
        println(string)
        val newCube = SolidManager.decodeFromString(string)
        assertEquals(cube.meta, newCube.meta)
    }

    @Test
    fun testProxySerialization() {
        val cube = Box(100f, 100f, 100f).apply {
            color(222)
            x = 100
            z = -100
        }
        val group = SolidGroup{
            ref("cube", cube)
            refGroup("pg", "pg.content".toName()){
                sphere(50){
                    x = -100
                }
            }
        }
        val string = SolidManager.encodeToString(group)
        println(string)
        val reconstructed = SolidManager.decodeFromString(string) as SolidGroup
        assertEquals(group["cube"]?.meta, reconstructed["cube"]?.meta)
    }

}