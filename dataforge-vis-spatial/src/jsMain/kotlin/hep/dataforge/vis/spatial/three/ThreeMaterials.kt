package hep.dataforge.vis.spatial.three

import hep.dataforge.meta.*
import hep.dataforge.values.ValueType
import hep.dataforge.vis.common.Colors
import hep.dataforge.vis.common.VisualObject
import hep.dataforge.vis.spatial.Material3D
import info.laht.threekt.materials.LineBasicMaterial
import info.laht.threekt.materials.Material
import info.laht.threekt.materials.MeshBasicMaterial
import info.laht.threekt.materials.MeshPhongMaterial
import info.laht.threekt.math.Color
import info.laht.threekt.objects.Mesh
import kotlin.math.max


object ThreeMaterials {
    val DEFAULT_COLOR = Color(Colors.darkgreen)
    val DEFAULT = MeshPhongMaterial().apply {
        color.set(DEFAULT_COLOR)
    }
    val DEFAULT_LINE_COLOR = Color(Colors.black)
    val DEFAULT_LINE = LineBasicMaterial().apply {
        color.set(DEFAULT_LINE_COLOR)
    }


    //    private val materialCache = HashMap<Meta, Material>()
    private val lineMaterialCache = HashMap<Meta?, Material>()


//    fun buildMaterial(meta: Meta): Material =
//        MeshBasicMaterial().apply {
//            color = meta["color"]?.color() ?: DEFAULT_COLOR
//            opacity = meta["opacity"]?.double ?: 1.0
//            transparent = meta["transparent"].boolean ?: (opacity < 1.0)
//            //node["specularColor"]?.let { specular = it.color() }
//            //side = 2
//        }

    fun getLineMaterial(meta: Meta?): Material = lineMaterialCache.getOrPut(meta) {
        LineBasicMaterial().apply {
            color = meta["color"]?.color() ?: DEFAULT_LINE_COLOR
            opacity = meta["opacity"].double ?: 1.0
            transparent = meta["transparent"].boolean ?: (opacity < 1.0)
            linewidth = meta["thickness"].double ?: 1.0
        }
    }

    fun rgbToString(rgb: Int): String {
        val string = rgb.toString(16).padStart(6, '0')
        return "#" + string.substring(max(0, string.length - 6))
    }

}

/**
 * Infer color based on meta item
 */
fun MetaItem<*>.color(): Color {
    return when (this) {
        is MetaItem.ValueItem -> if (this.value.type == ValueType.NUMBER) {
            val int = value.number.toInt()
            Color(int)
        } else {
            Color(this.value.string)
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

///**
// * Infer Three material based on meta item
// */
//fun Meta?.jsMaterial(): Material {
//    return if (this == null) {
//        ThreeMaterials.DEFAULT
//    } else {
//        ThreeMaterials.buildMaterial(this)
//    }
//}
//
//fun Meta?.jsLineMaterial(): Material {
//    return if (this == null) {
//        ThreeMaterials.DEFAULT_LINE
//    } else {
//        ThreeMaterials.buildLineMaterial(this)
//    }
//}


//fun Material3D?.jsMaterial(): Material = this?.config.jsMaterial()
//fun Material3D?.jsLineMaterial(): Material = this?.config.jsLineMaterial()

fun Mesh.updateMaterial(obj: VisualObject) {
    val meta = obj.properties[Material3D.MATERIAL_KEY].node?:EmptyMeta
    material = (material as? MeshBasicMaterial ?: MeshBasicMaterial()).apply {
        color = meta["color"]?.color() ?: ThreeMaterials.DEFAULT_COLOR
        opacity = meta["opacity"]?.double ?: 1.0
        transparent = meta["transparent"].boolean ?: (opacity < 1.0)
        needsUpdate = true
    }
}
