package space.kscience.visionforge.solid.three

import info.laht.threekt.core.BufferGeometry
import info.laht.threekt.core.Layers
import info.laht.threekt.external.controls.OrbitControls
import info.laht.threekt.materials.Material
import info.laht.threekt.math.Vector3
import info.laht.threekt.objects.Mesh
import info.laht.threekt.textures.Texture
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.float
import space.kscience.dataforge.meta.get
import kotlin.math.PI

public val Meta.vector: Vector3 get() = Vector3(this["x"].float ?: 0f, this["y"].float ?: 0f, this["z"].float ?: 0f)


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