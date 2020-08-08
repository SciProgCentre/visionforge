package hep.dataforge.vision.solid

import hep.dataforge.vision.Colors
import hep.dataforge.vision.get
import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals

class GroupTest {
    @Test
    fun testGroupWithComposite() {
        val group = SolidGroup().apply {
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

        assertEquals(3, group.children.count())
        assertEquals(300.0, (group["intersect"] as Solid).y.toDouble())
        assertEquals(-300.0, (group["subtract"] as Solid).y.toDouble())
    }
}