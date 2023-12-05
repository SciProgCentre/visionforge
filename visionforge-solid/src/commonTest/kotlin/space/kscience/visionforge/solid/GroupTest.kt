package space.kscience.visionforge.solid

import space.kscience.visionforge.Colors
import space.kscience.visionforge.getChild
import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals

class GroupTest {
    @Test
    fun testGroupWithComposite() {
        val group = testSolids.solidGroup{
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

        assertEquals(3, group.items.count())
        assertEquals(300.0, (group.children.getChild("intersect") as Solid).y.toDouble())
        assertEquals(-300.0, (group.children.getChild("subtract") as Solid).y.toDouble())
    }
}