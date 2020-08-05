package hep.dataforge.vision.spatial

import hep.dataforge.meta.*
import hep.dataforge.meta.descriptors.NodeDescriptor
import hep.dataforge.meta.descriptors.attributes
import hep.dataforge.names.asName
import hep.dataforge.names.plus
import hep.dataforge.values.ValueType
import hep.dataforge.values.asValue
import hep.dataforge.vision.Colors
import hep.dataforge.vision.spatial.Material3D.Companion.MATERIAL_COLOR_KEY
import hep.dataforge.vision.spatial.Material3D.Companion.MATERIAL_KEY
import hep.dataforge.vision.spatial.Material3D.Companion.MATERIAL_OPACITY_KEY
import hep.dataforge.vision.widgetType

class Material3D : Scheme() {

    /**
     * Primary web-color for the material
     */
    var color by string(key = COLOR_KEY)

    /**
     * Specular color for phong material
     */
    var specularColor by string(key = SPECULAR_COLOR_KEY)

    /**
     * Opacity
     */
    var opacity by float(1f, key = OPACITY_KEY)

    /**
     * Replace material by wire frame
     */
    var wireframe by boolean(false, WIREFRAME_KEY)

    companion object : SchemeSpec<Material3D>(::Material3D) {

        val MATERIAL_KEY = "material".asName()
        internal val COLOR_KEY = "color".asName()
        val MATERIAL_COLOR_KEY = MATERIAL_KEY + COLOR_KEY
        internal val SPECULAR_COLOR_KEY = "specularColor".asName()
        val MATERIAL_SPECULAR_COLOR_KEY = MATERIAL_KEY + SPECULAR_COLOR_KEY
        internal val OPACITY_KEY = "opacity".asName()
        val MATERIAL_OPACITY_KEY = MATERIAL_KEY + OPACITY_KEY
        internal val WIREFRAME_KEY = "wireframe".asName()
        val MATERIAL_WIREFRAME_KEY = MATERIAL_KEY + WIREFRAME_KEY

        val descriptor by lazy {
            //must be lazy to avoid initialization bug
            NodeDescriptor {
                value(COLOR_KEY) {
                    type(ValueType.STRING, ValueType.NUMBER)
                    default("#ffffff")
                    widgetType = "color"
                }
                value(OPACITY_KEY) {
                    type(ValueType.NUMBER)
                    default(1.0)
                    attributes {
                        this["min"] = 0.0
                        this["max"] = 1.0
                        this["step"] = 0.1
                    }
                    widgetType = "slider"
                }
                value(WIREFRAME_KEY) {
                    type(ValueType.BOOLEAN)
                    default(false)
                }
            }
        }
    }
}

/**
 * Set color as web-color
 */
fun VisualObject3D.color(webColor: String) {
    setItem(MATERIAL_COLOR_KEY, webColor.asValue())
}

/**
 * Set color as integer
 */
fun VisualObject3D.color(rgb: Int) {
    setItem(MATERIAL_COLOR_KEY, rgb.asValue())
}

fun VisualObject3D.color(r: UByte, g: UByte, b: UByte) = setItem(
    MATERIAL_COLOR_KEY,
    Colors.rgbToMeta(r, g, b)
)

/**
 * Web colors representation of the color in `#rrggbb` format or HTML name
 */
var VisualObject3D.color: String?
    get() = getItem(MATERIAL_COLOR_KEY)?.let { Colors.fromMeta(it) }
    set(value) {
        setItem(MATERIAL_COLOR_KEY, value?.asValue())
    }

val VisualObject3D.material: Material3D?
    get() = getItem(MATERIAL_KEY).node?.let { Material3D.wrap(it) }

fun VisualObject3D.material(builder: Material3D.() -> Unit) {
    val node = config[MATERIAL_KEY].node
    if (node != null) {
        Material3D.update(node, builder)
    } else {
        config[MATERIAL_KEY] = Material3D(builder)
    }
}

var VisualObject3D.opacity: Double?
    get() = getItem(MATERIAL_OPACITY_KEY).double
    set(value) {
        setItem(MATERIAL_OPACITY_KEY, value?.asValue())
    }