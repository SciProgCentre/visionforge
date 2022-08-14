package space.kscience.visionforge.solid.three

import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.float
import space.kscience.dataforge.meta.get
import three.core.BufferGeometry
import three.core.Layers
import three.core.Object3D
import three.external.controls.OrbitControls
import three.materials.Material
import three.math.Vector3
import three.objects.Mesh
import three.textures.Texture
import kotlin.contracts.contract
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

public fun Layers.check(layer: Int): Boolean = (mask shr (layer) and 0x00000001) > 0


internal fun isMesh(object3D: Object3D): Boolean{
    contract {
        returns(true) implies (object3D is Mesh)
    }
    return object3D.asDynamic().isMesh as? Boolean ?: false
}

internal fun Object3D.takeIfMesh(): Mesh? {
    val d = asDynamic()
    return if(d.isMesh as Boolean){
        d.unsafeCast<Mesh>()
    } else {
        null
    }
}