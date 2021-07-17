package space.kscience.visionforge.solid

import space.kscience.dataforge.meta.MetaItemNode
import space.kscience.dataforge.meta.getIndexed
import space.kscience.dataforge.meta.node
import space.kscience.dataforge.meta.toMetaItem
import space.kscience.dataforge.misc.DFExperimental
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

        val json = Solids.jsonForSolids.encodeToJsonElement(Convex.serializer(), convex)
        val meta = json.toMetaItem().node!!

        val points = meta.getIndexed("points").values.map { (it as MetaItemNode<*>).node.point3D() }
        assertEquals(8, points.count())

        assertEquals(8, convex.points.size)
    }

}