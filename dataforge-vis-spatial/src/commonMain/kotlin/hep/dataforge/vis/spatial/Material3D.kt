package hep.dataforge.vis.spatial

import hep.dataforge.meta.*
import hep.dataforge.names.asName
import hep.dataforge.names.plus
import hep.dataforge.vis.common.Colors
import hep.dataforge.vis.common.VisualObject
import hep.dataforge.vis.spatial.Material3D.Companion.COLOR_KEY
import hep.dataforge.vis.spatial.Material3D.Companion.MATERIAL_KEY
import hep.dataforge.vis.spatial.Material3D.Companion.OPACITY_KEY

class Material3D(override val config: Config) : Specific {

    var color by string()

    var opacity by float(1f)

    companion object : Specification<Material3D> {
        override fun wrap(config: Config): Material3D = Material3D(config)

        val MATERIAL_KEY = "material".asName()
        val COLOR_KEY = MATERIAL_KEY + "color"
        val OPACITY_KEY = MATERIAL_KEY + "opacity"

    }
}

fun VisualObject.color(rgb: String) {
    setProperty(COLOR_KEY, rgb)
}

fun VisualObject.color(rgb: Int) = color(Colors.rgbToString(rgb))

fun VisualObject.color(r: UByte, g: UByte, b: UByte) = color( Colors.rgbToString(r,g,b))

var VisualObject.color: String?
    get() = getProperty(COLOR_KEY).string
    set(value) {
        if (value != null) {
            color(value)
        }
    }

var VisualObject.material: Material3D?
    get() = getProperty(MATERIAL_KEY).node?.let { Material3D.wrap(it) }
    set(value) = setProperty(MATERIAL_KEY, value?.config)

fun VisualObject.material(builder: Material3D.() -> Unit) {
    material = Material3D.build(builder)
}

var VisualObject.opacity: Double?
    get() = getProperty(OPACITY_KEY).double
    set(value) {
        setProperty(OPACITY_KEY, value)
    }