package space.kscience.visionforge.solid

import kotlin.test.Test
import kotlin.test.assertEquals

class CompositeTest {

    @Test
    fun testCompositeBuilder(){
        lateinit var composite: Composite
        SolidGroup {
            composite = composite(CompositeType.INTERSECT) {
                y = 300
                box(100, 100, 100) {
                    z = 50
                }
                sphere(50) {
                    detail = 32
                }
                material {
                    color("pink")
                }
            }
        }

        assertEquals("pink", composite.color.string)
    }
}