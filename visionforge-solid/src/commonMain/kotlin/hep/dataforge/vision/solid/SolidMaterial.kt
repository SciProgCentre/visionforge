package hep.dataforge.vision.solid

import hep.dataforge.meta.*
import hep.dataforge.meta.descriptors.NodeDescriptor
import hep.dataforge.meta.descriptors.attributes
import hep.dataforge.names.Name
import hep.dataforge.names.asName
import hep.dataforge.names.plus
import hep.dataforge.values.ValueType
import hep.dataforge.values.asValue
import hep.dataforge.vision.Colors
import hep.dataforge.vision.solid.SolidMaterial.Companion.MATERIAL_COLOR_KEY
import hep.dataforge.vision.solid.SolidMaterial.Companion.MATERIAL_KEY
import hep.dataforge.vision.solid.SolidMaterial.Companion.MATERIAL_OPACITY_KEY
import hep.dataforge.vision.widgetType

public class SolidMaterial : Scheme() {

    /**
     * Primary web-color for the material
     */
    public var color: String? by string(key = COLOR_KEY)

    /**
     * Specular color for phong material
     */
    public var specularColor: String? by string(key = SPECULAR_COLOR_KEY)

    /**
     * Opacity
     */
    public var opacity: Float by float(1f, key = OPACITY_KEY)

    /**
     * Replace material by wire frame
     */
    public var wireframe: Boolean by boolean(false, WIREFRAME_KEY)

    public companion object : SchemeSpec<SolidMaterial>(::SolidMaterial) {

        public val MATERIAL_KEY: Name = "material".asName()
        internal val COLOR_KEY = "color".asName()
        public val MATERIAL_COLOR_KEY: Name = MATERIAL_KEY + COLOR_KEY
        internal val SPECULAR_COLOR_KEY = "specularColor".asName()
        public val MATERIAL_SPECULAR_COLOR_KEY: Name = MATERIAL_KEY + SPECULAR_COLOR_KEY
        internal val OPACITY_KEY = "opacity".asName()
        public val MATERIAL_OPACITY_KEY: Name = MATERIAL_KEY + OPACITY_KEY
        internal val WIREFRAME_KEY = "wireframe".asName()
        public val MATERIAL_WIREFRAME_KEY: Name = MATERIAL_KEY + WIREFRAME_KEY

        public val descriptor: NodeDescriptor by lazy {
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
public fun Solid.color(webColor: String) {
    setItem(MATERIAL_COLOR_KEY, webColor.asValue())
}

/**
 * Set color as integer
 */
public fun Solid.color(rgb: Int) {
    setItem(MATERIAL_COLOR_KEY, rgb.asValue())
}

public fun Solid.color(r: UByte, g: UByte, b: UByte): Unit = setItem(
    MATERIAL_COLOR_KEY,
    Colors.rgbToString(r, g, b).asValue()
)

/**
 * Web colors representation of the color in `#rrggbb` format or HTML name
 */
public var Solid.color: String?
    get() = getItem(MATERIAL_COLOR_KEY)?.let { Colors.fromMeta(it) }
    set(value) {
        setItem(MATERIAL_COLOR_KEY, value?.asValue())
    }

public val Solid.material: SolidMaterial?
    get() = getItem(MATERIAL_KEY).node?.let { SolidMaterial.wrap(it) }

public fun Solid.material(builder: SolidMaterial.() -> Unit) {
    val node = config[MATERIAL_KEY].node
    if (node != null) {
        SolidMaterial.update(node, builder)
    } else {
        config[MATERIAL_KEY] = SolidMaterial(builder)
    }
}

public var Solid.opacity: Double?
    get() = getItem(MATERIAL_OPACITY_KEY).double
    set(value) {
        setItem(MATERIAL_OPACITY_KEY, value?.asValue())
    }