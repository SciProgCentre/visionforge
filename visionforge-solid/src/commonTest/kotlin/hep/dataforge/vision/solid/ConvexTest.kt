package hep.dataforge.vision.solid

import hep.dataforge.meta.*

import kotlin.test.Test
import kotlin.test.assertEquals

class ConvexTest {
    @OptIn(DFExperimental::class)
    @Suppress("UNUSED_VARIABLE")
    @Test
    fun testConvexBuilder() {
        val group = SolidGroup().apply {
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

        val convex = group.children.values.first() as Convex

        val json = SolidManager.jsonForSolids.encodeToJsonElement(Convex.serializer(), convex)
        val meta = json.toMetaItem().node!!

        val points = meta.getIndexed("points").values.map { (it as NodeItem<*>).node.point3D() }
        assertEquals(8, points.count())

        assertEquals(8, convex.points.size)
    }

}