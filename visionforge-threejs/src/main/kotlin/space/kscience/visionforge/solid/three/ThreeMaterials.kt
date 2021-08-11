package space.kscience.visionforge.solid.three

import info.laht.threekt.materials.LineBasicMaterial
import info.laht.threekt.materials.Material
import info.laht.threekt.materials.MeshBasicMaterial
import info.laht.threekt.math.Color
import info.laht.threekt.objects.Mesh
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.boolean
import space.kscience.dataforge.meta.double
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.dataforge.values.*
import space.kscience.visionforge.Colors
import space.kscience.visionforge.Vision
import space.kscience.visionforge.computePropertyNode
import space.kscience.visionforge.getStyleNodes
import space.kscience.visionforge.solid.SolidMaterial
import space.kscience.visionforge.solid.SolidReference


public object ThreeMaterials {
    public val DEFAULT_COLOR: Color = Color(Colors.darkgreen)
    public val DEFAULT: MeshBasicMaterial = MeshBasicMaterial().apply {
        color.set(DEFAULT_COLOR)
        cached = true
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

    internal fun buildMaterial(meta: Meta): Material = MeshBasicMaterial().apply {
        color = meta[SolidMaterial.COLOR_KEY]?.threeColor() ?: DEFAULT_COLOR
        opacity = meta[SolidMaterial.OPACITY_KEY]?.double ?: 1.0
        transparent = opacity < 1.0
        wireframe = meta[SolidMaterial.WIREFRAME_KEY].boolean ?: false
        needsUpdate = true
    }
//        val material = SolidMaterial.read(meta)
//        return meta[SolidMaterial.SPECULAR_COLOR_KEY]?.let { specularColor ->
//            MeshPhongMaterial().apply {
//                color = meta[SolidMaterial.COLOR_KEY]?.threeColor() ?: DEFAULT_COLOR
//                specular = specularColor.threeColor()
//                emissive = material.emissiveColor.threeColor() ?: specular
//                reflectivity = 0.5
//                refractionRatio = 1.0
//                shininess = 100.0
//                opacity = meta[SolidMaterial.OPACITY_KEY]?.double ?: 1.0
//                transparent = opacity < 1.0
//                wireframe = meta[SolidMaterial.WIREFRAME_KEY].boolean ?: false
//                needsUpdate = true
//            }
//        } ?: MeshBasicMaterial().apply {
//            color = meta[SolidMaterial.COLOR_KEY]?.threeColor() ?: DEFAULT_COLOR
//            opacity = meta[SolidMaterial.OPACITY_KEY]?.double ?: 1.0
//            transparent = opacity < 1.0
//            wireframe = meta[SolidMaterial.WIREFRAME_KEY].boolean ?: false
//            needsUpdate = true
//        }

    internal fun cacheMaterial(meta: Meta): Material = materialCache.getOrPut(meta) {
        buildMaterial(meta).apply {
            cached = true
        }
    }
}

/**
 * Compute color
 */
public fun Meta.threeColor(): Color = getValue(Name.EMPTY)?.let { value ->
    if (value.type == ValueType.NUMBER) {
        val int = value.int
        Color(int)
    } else {
        Color(value.string)
    }
} ?: Color(
    getValue(Colors.RED_KEY.asName())?.int ?: 0,
    getValue(Colors.GREEN_KEY.asName())?.int ?: 0,
    getValue(Colors.BLUE_KEY.asName())?.int ?: 0
)

private var Material.cached: Boolean
    get() = userData["cached"] == true
    set(value) {
        userData["cached"] = value
    }

public fun Mesh.updateMaterial(vision: Vision) {
    val ownMaterialMeta = vision.meta.getMeta(SolidMaterial.MATERIAL_KEY)
    if (ownMaterialMeta == null) {
        if (vision is SolidReference && vision.getStyleNodes(SolidMaterial.MATERIAL_KEY).isEmpty()) {
            updateMaterial(vision.prototype)
        } else {
            material = vision.computePropertyNode(SolidMaterial.MATERIAL_KEY)?.let {
                ThreeMaterials.cacheMaterial(it)
            } ?: ThreeMaterials.DEFAULT
        }
    } else {
        material = vision.computePropertyNode(SolidMaterial.MATERIAL_KEY)?.let {
            ThreeMaterials.buildMaterial(it)
        } ?: ThreeMaterials.DEFAULT
    }
}

public fun Mesh.updateMaterialProperty(vision: Vision, propertyName: Name) {
    if (material.cached || propertyName == SolidMaterial.MATERIAL_KEY) {
        //generate a new material since cached material should not be changed
        updateMaterial(vision)
    } else {
        when (propertyName) {
            SolidMaterial.MATERIAL_COLOR_KEY -> {
                material.asDynamic().color = vision.computePropertyNode(SolidMaterial.MATERIAL_COLOR_KEY)?.threeColor()
                    ?: ThreeMaterials.DEFAULT_COLOR
                material.needsUpdate = true
            }
            SolidMaterial.MATERIAL_OPACITY_KEY -> {
                val opacity = vision.getPropertyValue(
                    SolidMaterial.MATERIAL_OPACITY_KEY,
                    inherit = true,
                )?.double ?: 1.0
                material.opacity = opacity
                material.transparent = opacity < 1.0
                material.needsUpdate = true
            }
            SolidMaterial.MATERIAL_WIREFRAME_KEY -> {
                material.asDynamic().wireframe = vision.getPropertyValue(
                    SolidMaterial.MATERIAL_WIREFRAME_KEY,
                    inherit = true,
                )?.boolean ?: false
                material.needsUpdate = true
            }
            else -> console.warn("Unrecognized material property: $propertyName")
        }
    }
}