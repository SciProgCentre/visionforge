package hep.dataforge.vis.spatial

import hep.dataforge.meta.get
import hep.dataforge.meta.getIndexed
import hep.dataforge.meta.node
import kotlin.test.Test
import kotlin.test.assertEquals

class ConvexTest {
    @Suppress("UNUSED_VARIABLE")
    @Test
    fun testConvexBuilder() {
        val group = VisualGroup3D().apply {
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

        val convex = group.first() as Convex

        val meta = convex.toMeta()

        val pointsNode = convex.toMeta()["points"].node

        assertEquals(8, pointsNode?.items?.count())
        val points = pointsNode?.getIndexed("points")

        assertEquals(8, convex.points.size)
    }

}