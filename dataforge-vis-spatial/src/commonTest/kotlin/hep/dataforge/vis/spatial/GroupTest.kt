package hep.dataforge.vis.spatial

import hep.dataforge.vis.common.Colors
import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals

class GroupTest {
    @Test
    fun testGroupWithComposite(){
        val group = VisualGroup3D().apply{
            union {
                box(100, 100, 100) {
                    z = 100
                    rotationX = PI / 4
                    rotationY = PI / 4
                }
                box(100, 100, 100)
                material {
                    "color" to Colors.lightgreen
                    "opacity" to 0.3
                }
            }
            intersect{
                box(100, 100, 100) {
                    z = 100
                    rotationX = PI / 4
                    rotationY = PI / 4
                }
                box(100, 100, 100)
                y = 300
                color(Colors.red)
            }
            subtract{
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
        assertEquals(300.0,group.toList()[1].y.toDouble())
        assertEquals(-300.0,group.toList()[2].y.toDouble())
    }
}