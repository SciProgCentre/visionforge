package hep.dataforge.vision.solid.three

import hep.dataforge.meta.*
import hep.dataforge.values.ValueType
import hep.dataforge.vision.Colors
import hep.dataforge.vision.Vision
import hep.dataforge.vision.solid.SolidMaterial
import info.laht.threekt.materials.LineBasicMaterial
import info.laht.threekt.materials.Material
import info.laht.threekt.materials.MeshBasicMaterial
import info.laht.threekt.materials.MeshPhongMaterial
import info.laht.threekt.math.Color


object ThreeMaterials {
    val DEFAULT_COLOR = Color(Colors.darkgreen)
    val DEFAULT = MeshBasicMaterial().apply {
        color.set(DEFAULT_COLOR)
    }
    val DEFAULT_LINE_COLOR = Color(Colors.black)
    val DEFAULT_LINE = LineBasicMaterial().apply {
        color.set(DEFAULT_LINE_COLOR)
    }

    val SELECTED_MATERIAL = LineBasicMaterial().apply {
        color.set(Colors.ivory)
        linewidth = 8.0
    }


    val HIGHLIGHT_MATERIAL = LineBasicMaterial().apply {
        color.set(Colors.blue)
        linewidth = 8.0
    }

    fun getLineMaterial(meta: Meta?): LineBasicMaterial {
        if (meta == null) return DEFAULT_LINE
        return LineBasicMaterial().apply {
            color = meta[SolidMaterial.COLOR_KEY]?.getColor() ?: DEFAULT_LINE_COLOR
            opacity = meta[SolidMaterial.OPACITY_KEY].double ?: 1.0
            transparent = opacity < 1.0
            linewidth = meta["thickness"].double ?: 1.0
        }
    }

    fun getMaterial(vision3D: Vision): Material {
        val meta = vision3D.getItem(SolidMaterial.MATERIAL_KEY).node ?: return ThreeMaterials.DEFAULT
        return if (meta[SolidMaterial.SPECULAR_COLOR_KEY] != null) {
            MeshPhongMaterial().apply {
                color = meta[SolidMaterial.COLOR_KEY]?.getColor() ?: DEFAULT_COLOR
                specular = meta[SolidMaterial.SPECULAR_COLOR_KEY]!!.getColor()
                opacity = meta[SolidMaterial.OPACITY_KEY]?.double ?: 1.0
                transparent = opacity < 1.0
                wireframe = meta[SolidMaterial.WIREFRAME_KEY].boolean ?: false
                needsUpdate = true
            }
        } else {
            MeshBasicMaterial().apply {
                color = meta[SolidMaterial.COLOR_KEY]?.getColor() ?: DEFAULT_COLOR
                opacity = meta[SolidMaterial.OPACITY_KEY]?.double ?: 1.0
                transparent = opacity < 1.0
                wireframe = meta[SolidMaterial.WIREFRAME_KEY].boolean ?: false
                needsUpdate = true
            }
        }
    }

}

/**
 * Infer color based on meta item
 */
fun MetaItem<*>.getColor(): Color {
    return when (this) {
        is MetaItem.ValueItem -> if (this.value.type == ValueType.NUMBER) {
            val int = value.number.toInt()
            Color(int)
        } else {
            Color(this.value.string)
        }
        is MetaItem.NodeItem -> {
            Color(
                node[Colors.RED_KEY]?.int ?: 0,
                node[Colors.GREEN_KEY]?.int ?: 0,
                node[Colors.BLUE_KEY]?.int ?: 0
            )
        }
    }
}

