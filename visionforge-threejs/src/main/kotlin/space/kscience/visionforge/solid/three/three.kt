package space.kscience.visionforge.solid.three

import info.laht.threekt.core.BufferGeometry
import info.laht.threekt.core.Layers
import info.laht.threekt.external.controls.OrbitControls
import info.laht.threekt.materials.Material
import info.laht.threekt.math.Euler
import info.laht.threekt.math.Vector3
import info.laht.threekt.objects.Mesh
import info.laht.threekt.textures.Texture
import space.kscience.dataforge.meta.MetaItem
import space.kscience.dataforge.meta.float
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.node
import space.kscience.visionforge.solid.*
import kotlin.math.PI

public val Solid.euler: Euler get() = Euler(rotationX, rotationY, rotationZ, rotationOrder.name)

public val MetaItem.vector: Vector3 get() = Vector3(node["x"].float ?: 0f, node["y"].float ?: 0f, node["z"].float ?: 0f)


internal fun Double.toRadians() = this * PI / 180


internal fun Any.dispose() {
    when (this) {
        is BufferGeometry -> dispose()
        is Material -> dispose()
        is Mesh -> {
            geometry.dispose()
            material.dispose()
        }
        is OrbitControls -> dispose()
        is Texture -> dispose()
    }
}

public fun Layers.check(layer: Int): Boolean = (mask shr(layer) and 0x00000001) > 0