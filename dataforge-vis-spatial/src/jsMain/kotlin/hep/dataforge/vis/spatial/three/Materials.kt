package hep.dataforge.vis.spatial.three

import hep.dataforge.meta.*
import hep.dataforge.values.ValueType
import hep.dataforge.vis.common.Colors
import hep.dataforge.vis.spatial.Material3D
import info.laht.threekt.materials.LineBasicMaterial
import info.laht.threekt.materials.Material
import info.laht.threekt.materials.MeshBasicMaterial
import info.laht.threekt.materials.MeshPhongMaterial
import info.laht.threekt.math.Color


object Materials {
    val DEFAULT_COLOR = Color(Colors.darkgreen)
    val DEFAULT = MeshPhongMaterial().apply {
        color.set(DEFAULT_COLOR)
    }
    val DEFAULT_LINE_COLOR = Color(Colors.black)
    val DEFAULT_LINE = LineBasicMaterial().apply {
        color.set(DEFAULT_LINE_COLOR)
    }


    private val materialCache = HashMap<Meta, Material>()
    private val lineMaterialCache = HashMap<Meta, Material>()

    fun getMaterial(meta: Meta): Material = materialCache.getOrPut(meta) {
        MeshBasicMaterial().apply {
            color = meta["color"]?.color() ?: DEFAULT_COLOR
            opacity = meta["opacity"]?.double ?: 1.0
            transparent = meta["transparent"].boolean ?: (opacity < 1.0)
            //node["specularColor"]?.let { specular = it.color() }
            //side = 2
        }
    }

    fun getLineMaterial(meta: Meta): Material = lineMaterialCache.getOrPut(meta) {
        LineBasicMaterial().apply {
            color = meta["color"]?.color() ?: DEFAULT_LINE_COLOR
            opacity = meta["opacity"].double ?: 1.0
            transparent = meta["transparent"].boolean ?: (opacity < 1.0)
            linewidth = meta["thickness"].double ?: 1.0
        }
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
 * Infer Three material based on meta item
 */
fun Meta?.jsMaterial(): Material {
    return if (this == null) {
        Materials.DEFAULT
    } else {
        Materials.getMaterial(this)
    }
}

fun Meta?.jsLineMaterial(): Material {
    return if (this == null) {
        Materials.DEFAULT_LINE
    } else{
        Materials.getLineMaterial(this)
    }
}


fun Material3D?.jsMaterial(): Material = this?.config.jsMaterial()
fun Material3D?.jsLineMaterial(): Material = this?.config.jsLineMaterial()

