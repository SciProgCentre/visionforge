package space.kscience.visionforge.solid

import space.kscience.dataforge.meta.Meta
import kotlin.math.PI
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * GeometryBuilder that computes closed-mesh volume for verification.
 */
private class VolumeCollectorBuilder : GeometryBuilder<Unit> {
    private var signedVolume = 0.0

    /**
     * Accumulates signed volume contribution from each triangular face.
     */
    override fun face(
        vertex1: FloatVector3D,
        vertex2: FloatVector3D,
        vertex3: FloatVector3D,
        normal: FloatVector3D?,
        meta: Meta
    ) {
        val x1 = vertex1.x.toDouble(); val y1 = vertex1.y.toDouble(); val z1 = vertex1.z.toDouble()
        val x2 = vertex2.x.toDouble(); val y2 = vertex2.y.toDouble(); val z2 = vertex2.z.toDouble()
        val x3 = vertex3.x.toDouble(); val y3 = vertex3.y.toDouble(); val z3 = vertex3.z.toDouble()
        // Signed volume contribution of the tetrahedron formed with the origin
        signedVolume += (x1 * (y2 * z3 - y3 * z2) +
                x2 * (y3 * z1 - y1 * z3) +
                x3 * (y1 * z2 - y2 * z1)) / 6.0
    }

    /**
     * Builds the final result.
     */
    override fun build() {}

    /**
     * Returns the absolute volume of the closed triangulated mesh.
     */
    fun volume(): Double = abs(signedVolume)
}

class CSGTest {

    /**
     * Tests that union of two non-overlapping boxes produces correct total volume.
     */
    @Test
    fun testCompositeUnionVolume() {
        val group = SolidGroup()

        val composite = group.union {
            box(1f, 1f, 1f) {
                x = -2f
            }
            box(1f, 1f, 1f) {
                x = 2f
            }
        }

        val collector = VolumeCollectorBuilder()

        collector.composite(composite)

        val volume = collector.volume()
        assertEquals(2.0, volume, 0.01)
    }

    /**
     * Tests that subtraction of sphere from box produces correct remaining volume.
     */
    @Test
    fun testCompositeSubtractVolume() {

        val composite = SolidGroup().subtract {
            box(1.5f, 1.5f, 1.5f)
            sphere(0.5f)
        }

        val collector = VolumeCollectorBuilder()

        collector.composite(composite)

        val volume = collector.volume()
        val expectedVolume = 1.5 * 1.5 * 1.5 - (4.0 / 3.0) * PI * 0.5 * 0.5 * 0.5
        assertEquals(expectedVolume, volume, 0.05)
    }

    /**
     * Rotation preserves volume: two separated boxes each rotated 45° around Y still
     * have no overlap, so union volume must equal the sum of their individual volumes (2.0).
     */
    @Test
    fun testUnionOfRotatedBoxesVolume() {
        val composite = SolidGroup().union {
            box(1f, 1f, 1f) {
                x = -3f
                rotationY = PI / 4
            }
            box(1f, 1f, 1f) {
                x = 3f
                rotationY = PI / 4
            }
        }

        val collector = VolumeCollectorBuilder()
        collector.composite(composite)

        // Rotation is volume-preserving, and the boxes are far apart, so total = 1 + 1
        assertEquals(2.0, collector.volume(), 0.01)
    }

}