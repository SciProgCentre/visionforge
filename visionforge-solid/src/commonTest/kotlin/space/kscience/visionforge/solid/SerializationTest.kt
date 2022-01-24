package space.kscience.visionforge.solid

import space.kscience.dataforge.names.Name
import space.kscience.visionforge.MutableVisionGroup
import space.kscience.visionforge.get
import kotlin.test.Test
import kotlin.test.assertEquals


/**
 * Create and attach new proxied group
 */
fun SolidGroup.refGroup(
    name: String,
    templateName: Name = Name.parse(name),
    block: MutableVisionGroup.() -> Unit
): SolidReferenceGroup {
    val group = SolidGroup().apply(block)
    return newRef(name, group, templateName = templateName)
}


class SerializationTest {
    @Test
    fun testCubeSerialization() {
        val cube = Box(100f, 100f, 100f).apply {
            color(222)
            x = 100
            z = -100
        }
        val string = Solids.encodeToString(cube)
        println(string)
        val newCube = Solids.decodeFromString(string)
        assertEquals(cube.meta, newCube.meta)
    }

    @Test
    fun testProxySerialization() {
        val cube = Box(100f, 100f, 100f).apply {
            color(222)
            x = 100
            z = -100
        }
        val group = SolidGroup {
            newRef("cube", cube)
            refGroup("pg", Name.parse("pg.content")) {
                sphere(50) {
                    x = -100
                }
            }
        }
        val string = Solids.encodeToString(group)
        println(string)
        val reconstructed = Solids.decodeFromString(string) as SolidGroup
        assertEquals(group["cube"]?.meta, reconstructed["cube"]?.meta)
    }

}