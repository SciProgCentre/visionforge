package hep.dataforge.vis.spatial

import hep.dataforge.io.toMeta
import hep.dataforge.meta.MetaItem
import hep.dataforge.meta.getIndexed
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

        val json = Visual3DPlugin.json.toJson(Convex.serializer(), convex)
        val meta = json.toMeta()

        val points = meta.getIndexed("points").values.map { (it as MetaItem.NodeItem<*>).node.point3D()}
        assertEquals(8, points.count())

        assertEquals(8, convex.points.size)
    }

}