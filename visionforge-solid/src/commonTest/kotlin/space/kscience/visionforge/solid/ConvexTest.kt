package space.kscience.visionforge.solid

import space.kscience.dataforge.meta.getIndexed
import space.kscience.dataforge.meta.toMeta
import kotlin.test.Test
import kotlin.test.assertEquals

class ConvexTest {
    @Test
    fun testConvexBuilder() {
        val group = testSolids.solidGroup {
            convex {
                point(50, 50, -50)
                point(50, -50, -50)
                point(-50, -50, -50)
                point(-50, 50, -50)
                point(50, 50, 50)
                point(50, -50, 50)
                point(-50, -50, 50)
                point(-50, 50, 50)
            }
        }

        val convex = group.items.values.first() as Convex

        val json = Solids.jsonForSolids.encodeToJsonElement(Convex.serializer(), convex)
        val meta = json.toMeta()

        val points = meta.getIndexed("points").values.map { it.point3D() }
        assertEquals(8, points.count())

        assertEquals(8, convex.points.size)
    }

}