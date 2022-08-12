package space.kscience.visionforge.solid.three

import info.laht.threekt.materials.LineBasicMaterial
import info.laht.threekt.materials.Material
import info.laht.threekt.materials.MeshBasicMaterial
import info.laht.threekt.materials.MeshStandardMaterial
import info.laht.threekt.math.Color
import info.laht.threekt.objects.Mesh
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.dataforge.names.plus
import space.kscience.visionforge.Colors
import space.kscience.visionforge.Vision
import space.kscience.visionforge.getStyleNodes
import space.kscience.visionforge.solid.ColorAccessor
import space.kscience.visionforge.solid.SolidMaterial
import space.kscience.visionforge.solid.SolidReference


public object ThreeMaterials {
    public val DEFAULT_COLOR: Color = Color(Colors.darkgreen)

    public val DEFAULT: MeshStandardMaterial = MeshStandardMaterial().apply {
        color.set(DEFAULT_COLOR)
        cached = true
    }

    public val BLACK_COLOR: Color = Color(Colors.black)

    public val DEFAULT_EMISSIVE_COLOR: Color = BLACK_COLOR

    public val DEFAULT_LINE_COLOR: Color get() = BLACK_COLOR

    public val DEFAULT_LINE: LineBasicMaterial = LineBasicMaterial().apply {
        color.set(DEFAULT_LINE_COLOR)
        cached = true
    }

    public val SELECTED_MATERIAL: LineBasicMaterial = LineBasicMaterial().apply {
        color.set(Colors.ivory)
        linewidth = 8.0
        cached = true
    }

    public val HIGHLIGHT_MATERIAL: LineBasicMaterial = LineBasicMaterial().apply {
        color.set(Colors.blue)
        linewidth = 8.0
        cached = true
    }

    private val lineMaterialCache = HashMap<Meta, LineBasicMaterial>()

    private fun buildLineMaterial(meta: Meta): LineBasicMaterial = LineBasicMaterial().apply {
        color = meta[SolidMaterial.COLOR_KEY]?.threeColor() ?: DEFAULT_LINE_COLOR
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

    internal fun buildMaterial(meta: Meta): Material = when (meta[SolidMaterial.TYPE_KEY]?.string) {
        "simple" -> MeshBasicMaterial().apply {
            color = meta[SolidMaterial.COLOR_KEY]?.threeColor() ?: DEFAULT_COLOR
            wireframe = meta[SolidMaterial.WIREFRAME_KEY].boolean ?: false
        }
        else -> MeshStandardMaterial().apply {
            color = meta[SolidMaterial.COLOR_KEY]?.threeColor() ?: DEFAULT_COLOR
            emissive = meta[SolidMaterial.EMISSIVE_COLOR_KEY]?.threeColor() ?: DEFAULT_EMISSIVE_COLOR
            wireframe = meta[SolidMaterial.WIREFRAME_KEY].boolean ?: false
        }
    }.apply {
        opacity = meta[SolidMaterial.OPACITY_KEY]?.double ?: 1.0
        transparent = opacity < 1.0
        needsUpdate = true
    }

    internal fun cacheMaterial(meta: Meta): Material = materialCache.getOrPut(meta) {
        buildMaterial(meta).apply {
            cached = true
        }
    }
}

/**
 * Compute color
 */
public fun Meta.threeColor(): Color? {
    if(isEmpty()) return null
    val value = value
    return if (isLeaf) {
        when {
            value == null -> null
            value === Null -> null
            value.type == ValueType.NUMBER -> Color(value.int)
            else -> Color(value.string)
        }
    } else {
        Color(
            getValue(Colors.RED_KEY.asName())?.int ?: 0,
            getValue(Colors.GREEN_KEY.asName())?.int ?: 0,
            getValue(Colors.BLUE_KEY.asName())?.int ?: 0
        )
    }
}

public fun ColorAccessor.threeColor(): Color? {
    val value = value
    return when {
        value == null -> null
        value === Null -> null
        value.type == ValueType.NUMBER -> Color(value.int)
        else -> Color(value.string)
    }
}

private var Material.cached: Boolean
    get() = userData["cached"] == true
    set(value) {
        userData["cached"] = value
    }

public fun Mesh.updateMaterial(vision: Vision) {
    val ownMaterialMeta = vision.properties.own?.get(SolidMaterial.MATERIAL_KEY)
    if (ownMaterialMeta == null) {
        if (vision is SolidReference && vision.getStyleNodes(SolidMaterial.MATERIAL_KEY).isEmpty()) {
            updateMaterial(vision.prototype)
        } else {
            material = ThreeMaterials.cacheMaterial(vision.properties.getProperty(SolidMaterial.MATERIAL_KEY))
        }
    } else {
        material = ThreeMaterials.buildMaterial(vision.properties.getProperty(SolidMaterial.MATERIAL_KEY))
    }
}

public fun Mesh.updateMaterialProperty(vision: Vision, propertyName: Name) {
    if (
        material.cached
        || propertyName == SolidMaterial.MATERIAL_KEY
        || propertyName == SolidMaterial.MATERIAL_KEY + SolidMaterial.TYPE_KEY
    ) {
        //generate a new material since cached material should not be changed
        updateMaterial(vision)
    } else {
        when (propertyName) {
            SolidMaterial.MATERIAL_COLOR_KEY -> {
                material.asDynamic().color = vision.properties.getProperty(SolidMaterial.MATERIAL_COLOR_KEY).threeColor()
                    ?: ThreeMaterials.DEFAULT_COLOR
            }
            SolidMaterial.SPECULAR_COLOR_KEY -> {
                material.asDynamic().specular = vision.properties.getProperty(SolidMaterial.SPECULAR_COLOR_KEY).threeColor()
                    ?: ThreeMaterials.DEFAULT_COLOR
            }
            SolidMaterial.MATERIAL_EMISSIVE_COLOR_KEY -> {
                material.asDynamic().emissive = vision.properties.getProperty(SolidMaterial.MATERIAL_EMISSIVE_COLOR_KEY)
                    .threeColor()
                    ?: ThreeMaterials.BLACK_COLOR
            }
            SolidMaterial.MATERIAL_OPACITY_KEY -> {
                val opacity = vision.properties.getValue(
                    SolidMaterial.MATERIAL_OPACITY_KEY,
                    inherit = true,
                )?.double ?: 1.0
                material.opacity = opacity
                material.transparent = opacity < 1.0
            }
            SolidMaterial.MATERIAL_WIREFRAME_KEY -> {
                material.asDynamic().wireframe = vision.properties.getValue(
                    SolidMaterial.MATERIAL_WIREFRAME_KEY,
                    inherit = true,
                )?.boolean ?: false
            }
            else -> console.warn("Unrecognized material property: $propertyName")
        }
        material.needsUpdate = true
    }
}