package space.kscience.visionforge.solid

import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.request
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.plus
import space.kscience.visionforge.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail


/**
 * Create and attach new proxied group
 */
fun SolidGroup.refGroup(
    name: String,
    templateName: Name = Name.parse(name),
    block: SolidGroup.() -> Unit
): SolidReference {
    val group = SolidGroup().apply(block)
    return newRef(name, group, prototypeName = templateName)
}

private fun failOnOrphan(vision: Vision, prefix: Name = Name.EMPTY) {
    if (vision.parent == null) fail("Parent is not defined for $vision with name '$prefix'")
    if (vision is VisionGroup<*>) {
        vision.visions.forEach { (token, child) ->
            failOnOrphan(child, prefix + token)
        }
    }
}


class SerializationTest {
    companion object {
        private val visionManager = Context("test") {
            plugin(VisionManager)
        }.request(VisionManager)
    }
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
        assertEquals(cube.properties, newCube.properties)
    }

    @Test
    fun testProxySerialization() {
        val cube = Box(100f, 100f, 100f).apply {
            color(222)
            x = 100
            z = -100
        }
        val group = testSolids.solidGroup {
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
        reconstructed.setAsRoot(visionManager)
        failOnOrphan(reconstructed)
        assertEquals(group["cube"]?.properties, reconstructed["cube"]?.properties)
    }

    @Test
    fun lightSerialization() {
        val group = testSolids.solidGroup {
            ambientLight {
                color(Colors.white)
                intensity = 100.0
            }
        }
        val serialized = Solids.encodeToString(group)

        val reconstructed = Solids.decodeFromString(serialized) as SolidGroup
        reconstructed.setAsRoot(visionManager)
        failOnOrphan(reconstructed)
        assertEquals(100.0, (reconstructed["@ambientLight"] as AmbientLightSource).intensity.toDouble())
    }

}