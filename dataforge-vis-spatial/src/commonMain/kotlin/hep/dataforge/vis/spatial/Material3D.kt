package hep.dataforge.vis.spatial

import hep.dataforge.meta.*
import hep.dataforge.meta.descriptors.NodeDescriptor
import hep.dataforge.names.asName
import hep.dataforge.names.plus
import hep.dataforge.values.ValueType
import hep.dataforge.values.asValue
import hep.dataforge.vis.Colors
import hep.dataforge.vis.spatial.Material3D.Companion.MATERIAL_COLOR_KEY
import hep.dataforge.vis.spatial.Material3D.Companion.MATERIAL_KEY
import hep.dataforge.vis.spatial.Material3D.Companion.MATERIAL_OPACITY_KEY

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
        val SPECULAR_COLOR_KEY = "specularColor".asName()
        internal val OPACITY_KEY = "opacity".asName()
        val MATERIAL_OPACITY_KEY = MATERIAL_KEY + OPACITY_KEY
        internal val WIREFRAME_KEY = "wireframe".asName()
        val MATERIAL_WIREFRAME_KEY = MATERIAL_KEY + WIREFRAME_KEY

        val descriptor by lazy {
            //must be lazy to avoid initialization bug
            NodeDescriptor {
                defineValue(VisualObject3D.VISIBLE_KEY) {
                    type(ValueType.BOOLEAN)
                    default(true)
                }
                defineNode(MATERIAL_KEY) {
                    defineValue(COLOR_KEY) {
                        type(ValueType.STRING, ValueType.NUMBER)
                        default("#ffffff")
                    }
                    defineValue(OPACITY_KEY) {
                        type(ValueType.NUMBER)
                        default(1.0)
                    }
                    defineValue(WIREFRAME_KEY) {
                        type(ValueType.BOOLEAN)
                        default(false)
                    }
                }
            }
        }
    }
}

/**
 * Set color as web-color
 */
fun VisualObject3D.color(webColor: String) {
    setProperty(MATERIAL_COLOR_KEY, webColor.asValue())
}

/**
 * Set color as integer
 */
fun VisualObject3D.color(rgb: Int) {
    setProperty(MATERIAL_COLOR_KEY, rgb.asValue())
}

fun VisualObject3D.color(r: UByte, g: UByte, b: UByte) = setProperty(
    MATERIAL_COLOR_KEY,
    Colors.rgbToMeta(r, g, b)
)

/**
 * Web colors representation of the color in `#rrggbb` format or HTML name
 */
var VisualObject3D.color: String?
    get() = getProperty(MATERIAL_COLOR_KEY)?.let { Colors.fromMeta(it) }
    set(value) {
        setProperty(MATERIAL_COLOR_KEY, value?.asValue())
    }

val VisualObject3D.material: Material3D?
    get() = getProperty(MATERIAL_KEY).node?.let { Material3D.wrap(it) }

fun VisualObject3D.material(builder: Material3D.() -> Unit) {
    val node = config[MATERIAL_KEY].node
    if (node != null) {
        Material3D.update(node, builder)
    } else {
        config[MATERIAL_KEY] = Material3D(builder)
    }
}

var VisualObject3D.opacity: Double?
    get() = getProperty(MATERIAL_OPACITY_KEY).double
    set(value) {
        setProperty(MATERIAL_OPACITY_KEY, value?.asValue())
    }