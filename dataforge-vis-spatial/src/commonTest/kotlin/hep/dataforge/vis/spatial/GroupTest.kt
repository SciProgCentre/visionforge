package hep.dataforge.vis.spatial

import hep.dataforge.vis.common.Colors
import hep.dataforge.vis.common.get
import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals

class GroupTest {
    @Test
    fun testGroupWithComposite() {
        val group = VisualGroup3D().apply {
            union("union") {
                box(100, 100, 100) {
                    z = 100
                    rotationX = PI / 4
                    rotationY = PI / 4
                }
                box(100, 100, 100)
                material {
                    color(Colors.lightgreen)
                    opacity = 0.3f
                }
            }
            intersect("intersect") {
                box(100, 100, 100) {
                    z = 100
                    rotationX = PI / 4
                    rotationY = PI / 4
                }
                box(100, 100, 100)
                y = 300
                color(Colors.red)
            }
            subtract("subtract") {
                box(100, 100, 100) {
                    z = 100
                    rotationX = PI / 4
                    rotationY = PI / 4
                }
                box(100, 100, 100)
                y = -300
                color(Colors.blue)
            }
        }

        assertEquals(3, group.count())
        assertEquals(300.0, (group["intersect"] as VisualObject3D).y.toDouble())
        assertEquals(-300.0, (group["subtract"] as VisualObject3D).y.toDouble())
    }
}