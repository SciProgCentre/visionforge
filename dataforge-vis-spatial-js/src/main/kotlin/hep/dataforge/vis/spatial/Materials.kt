package hep.dataforge.vis.spatial

import hep.dataforge.meta.MetaItem
import hep.dataforge.meta.double
import hep.dataforge.meta.get
import hep.dataforge.meta.int
import hep.dataforge.values.ValueType
import hep.dataforge.vis.common.Colors
import info.laht.threekt.materials.Material
import info.laht.threekt.materials.MeshPhongMaterial
import info.laht.threekt.math.Color


object Materials {
    val DEFAULT = MeshPhongMaterial().apply {
        this.color.set(Colors.darkgreen)
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

/**
 * Infer FX material based on meta item
 */
fun MetaItem<*>?.material(): Material {
    return when (this) {
        null -> Materials.DEFAULT
        is MetaItem.ValueItem -> MeshPhongMaterial().apply {
            color = this@material.color()
        }
        is MetaItem.NodeItem -> MeshPhongMaterial().apply {
            (node["color"] ?: this@material).let { color = it.color() }
            opacity = node["opacity"]?.double ?: 1.0
            node["specularColor"]?.let { specular = it.color() }
        }
    }
}

