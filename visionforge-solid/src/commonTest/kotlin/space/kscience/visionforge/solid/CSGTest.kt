package space.kscience.visionforge.solid

import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals

class CSGTest {

    @Test
    fun testUnionVolume() {
        // два непересекающихся куба — объём суммируется
        val boxA = Box(1f, 1f, 1f).apply { x = -2f }
        val boxB = Box(1f, 1f, 1f).apply { x = 2f }

        val result = boxA.toCSGSolid().union(boxB.toCSGSolid())

        val expectedVolume = boxA.toCSGSolid().calculateVolume() + boxB.toCSGSolid().calculateVolume()
        assertEquals(expectedVolume, result.calculateVolume(), 0.01)
    }

    @Test
    fun testSubtractVolume() {
        // куб 1.5 минус шар 0.5
        // объём куба = 1.728, объём шара = (4/3)*PI*0.5³ ≈ 0.5236
        // ожидаем ≈ 1.204
        val box = Box(1.5f, 1.5f, 1.5f)
        val sphere = Sphere(0.5f)

        val result = box.toCSGSolid().subtract(sphere.toCSGSolid())

        val expectedVolume = 1.5 * 1.5 * 1.5 - (4.0 / 3.0) * PI * 0.5 * 0.5 * 0.5
        assertEquals(expectedVolume, result.calculateVolume(), 0.05)
    }

    @Test
    fun testIntersectVolume() {
        // два куба 1x1x1 со смещением 0.5 — пересечение 0.5x1x1 = 0.5
        val boxA = Box(1f, 1f, 1f).apply { x = -0.25 }
        val boxB = Box(1f, 1f, 1f).apply { x = 0.25 }

        val result = boxA.toCSGSolid().intersect(boxB.toCSGSolid())

        assertEquals(0.5, result.calculateVolume(), 0.05)
    }
}