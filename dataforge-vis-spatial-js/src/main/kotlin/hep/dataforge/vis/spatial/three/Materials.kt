package hep.dataforge.vis.spatial.three

import hep.dataforge.meta.*
import hep.dataforge.values.ValueType
import hep.dataforge.vis.common.Colors
import info.laht.threekt.materials.Material
import info.laht.threekt.materials.MeshBasicMaterial
import info.laht.threekt.materials.MeshPhongMaterial
import info.laht.threekt.math.Color


object Materials {
    val DEFAULT_COLOR = Color(Colors.darkgreen)
    val DEFAULT = MeshPhongMaterial().apply {
        this.color.set(DEFAULT_COLOR)
    }
}

/**
 * Infer color based on meta item
 */
fun MetaItem<*>.color(): Color {
    return when (this) {
        is MetaItem.ValueItem -> if (this.value.type == ValueType.STRING) {
            Color(this.value.string)
        } else {
            val int = value.number.toInt()
//            val red = int and 0x00ff0000 shr 16
//            val green = int and 0x0000ff00 shr 8
//            val blue = int and 0x000000ff
            Color(int)
        }
        is MetaItem.NodeItem -> {
            Color(
                node["red"]?.int ?: 0,
                node["green"]?.int ?: 0,
                node["blue"]?.int ?: 0
            )
        }
    }
}

private val materialCache = HashMap<Meta, Material>()

/**
 * Infer Three material based on meta item
 */
fun Meta?.jsMaterial(): Material {
    return if (this == null) {
        Materials.DEFAULT
    } else
    //TODO add more options for material
        return materialCache.getOrPut(this) {
            MeshBasicMaterial().apply {
                color = get("color")?.color() ?: Materials.DEFAULT_COLOR
                opacity = get("opacity")?.double ?: 1.0
                transparent = get("transparent").boolean ?: (opacity < 1.0)
                //node["specularColor"]?.let { specular = it.color() }
                side = 2
            }
        }
}

