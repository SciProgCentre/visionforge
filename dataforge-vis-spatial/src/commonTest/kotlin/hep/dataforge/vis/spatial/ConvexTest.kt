package hep.dataforge.vis.spatial

import hep.dataforge.meta.get
import hep.dataforge.meta.getAll
import hep.dataforge.meta.node
import hep.dataforge.names.toName
import hep.dataforge.vis.common.DisplayGroup
import kotlin.test.Test
import kotlin.test.assertEquals

class ConvexTest {
    @Test
    fun testConvexBuilder() {
        val group = DisplayGroup().apply {
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

        val pointsNode = convex.properties["points"].node

        assertEquals(8, pointsNode?.items?.count())
        val points = pointsNode?.getAll("point".toName())

        assertEquals(8, convex.points.size)
    }

}