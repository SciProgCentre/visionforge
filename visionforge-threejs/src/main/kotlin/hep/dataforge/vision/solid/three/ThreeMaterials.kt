package hep.dataforge.vision.solid.three

import hep.dataforge.meta.*
import hep.dataforge.values.ValueType
import hep.dataforge.values.int
import hep.dataforge.values.string
import hep.dataforge.vision.Colors
import hep.dataforge.vision.Vision
import hep.dataforge.vision.solid.SolidMaterial
import info.laht.threekt.materials.LineBasicMaterial
import info.laht.threekt.materials.Material
import info.laht.threekt.materials.MeshBasicMaterial
import info.laht.threekt.materials.MeshPhongMaterial
import info.laht.threekt.math.Color


public object ThreeMaterials {
    public val DEFAULT_COLOR: Color = Color(Colors.darkgreen)
    public val DEFAULT: MeshBasicMaterial = MeshBasicMaterial().apply {
        color.set(DEFAULT_COLOR)
    }
    public val DEFAULT_LINE_COLOR: Color = Color(Colors.black)
    public val DEFAULT_LINE: LineBasicMaterial = LineBasicMaterial().apply {
        color.set(DEFAULT_LINE_COLOR)
    }

    public val SELECTED_MATERIAL: LineBasicMaterial = LineBasicMaterial().apply {
        color.set(Colors.ivory)
        linewidth = 8.0
    }

    public val HIGHLIGHT_MATERIAL: LineBasicMaterial = LineBasicMaterial().apply {
        color.set(Colors.blue)
        linewidth = 8.0
    }

    private val lineMaterialCache = HashMap<Meta, LineBasicMaterial>()

    private fun buildLineMaterial(meta: Meta): LineBasicMaterial = LineBasicMaterial().apply {
        color = meta[SolidMaterial.COLOR_KEY]?.getColor() ?: DEFAULT_LINE_COLOR
        opacity = meta[SolidMaterial.OPACITY_KEY].double ?: 1.0
        transparent = opacity < 1.0
        linewidth = meta["thickness"].double ?: 1.0
    }

    public fun getLineMaterial(meta: Meta?, cache: Boolean): LineBasicMaterial {
        if (meta == null) return DEFAULT_LINE
        return if (cache) {
            lineMaterialCache.getOrPut(meta) { buildLineMaterial(meta) }
        } else {
            buildLineMaterial(meta)
        }
    }

    private val materialCache = HashMap<Meta, Material>()

    private fun buildMaterial(meta: Meta): Material {
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

    public fun getMaterial(vision3D: Vision, cache: Boolean): Material {
        val meta = vision3D.getProperty(SolidMaterial.MATERIAL_KEY).node ?: return DEFAULT
        return if (cache) {
            materialCache.getOrPut(meta) { buildMaterial(meta) }
        } else {
            buildMaterial(meta)
        }
    }

}

/**
 * Infer color based on meta item
 */
public fun MetaItem<*>.getColor(): Color {
    return when (this) {
        is MetaItem.ValueItem -> if (this.value.type == ValueType.NUMBER) {
            val int = value.int
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

