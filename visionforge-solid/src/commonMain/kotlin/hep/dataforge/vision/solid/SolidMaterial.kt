package hep.dataforge.vision.solid

import hep.dataforge.meta.*
import hep.dataforge.meta.descriptors.NodeDescriptor
import hep.dataforge.meta.descriptors.attributes
import hep.dataforge.names.Name
import hep.dataforge.names.asName
import hep.dataforge.names.plus
import hep.dataforge.values.Value
import hep.dataforge.values.ValueType
import hep.dataforge.values.asValue
import hep.dataforge.values.string
import hep.dataforge.vision.*
import hep.dataforge.vision.solid.SolidMaterial.Companion.MATERIAL_COLOR_KEY
import hep.dataforge.vision.solid.SolidMaterial.Companion.MATERIAL_KEY
import hep.dataforge.vision.solid.SolidMaterial.Companion.MATERIAL_OPACITY_KEY

@VisionBuilder
public class ColorAccessor(private val parent: MutableItemProvider, private val colorKey: Name) {
    public var value: Value?
        get() = parent.getItem(colorKey).value
        set(value) {
            parent[colorKey] = value
        }
}

public var ColorAccessor?.string: String?
    get() = this?.value?.string
    set(value) {
        this?.value = value?.asValue()
    }

/**
 * Set [webcolor](https://en.wikipedia.org/wiki/Web_colors) as string
 */
public operator fun ColorAccessor?.invoke(webColor: String) {
    this?.value = webColor.asValue()
}

/**
 * Set color as RGB integer
 */
public operator fun ColorAccessor?.invoke(rgb: Int) {
    this?.value = Colors.rgbToString(rgb).asValue()
}

/**
 * Set color as RGB
 */
public operator fun ColorAccessor?.invoke(r: UByte, g: UByte, b: UByte) {
    this?.value = Colors.rgbToString(r, g, b).asValue()
}

@VisionBuilder
public class SolidMaterial : Scheme() {

    /**
     * Primary web-color for the material
     */
    public var color: ColorAccessor = ColorAccessor(config, COLOR_KEY)

    /**
     * Specular color for phong material
     */
    public var specularColor: ColorAccessor = ColorAccessor(config, SPECULAR_COLOR_KEY)

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
        public val COLOR_KEY: Name = "color".asName()
        public val MATERIAL_COLOR_KEY: Name = MATERIAL_KEY + COLOR_KEY
        public val SPECULAR_COLOR_KEY: Name = "specularColor".asName()
        public val MATERIAL_SPECULAR_COLOR_KEY: Name = MATERIAL_KEY + SPECULAR_COLOR_KEY
        public val OPACITY_KEY: Name = "opacity".asName()
        public val MATERIAL_OPACITY_KEY: Name = MATERIAL_KEY + OPACITY_KEY
        public val WIREFRAME_KEY: Name = "wireframe".asName()
        public val MATERIAL_WIREFRAME_KEY: Name = MATERIAL_KEY + WIREFRAME_KEY

        public override val descriptor: NodeDescriptor by lazy {
            //must be lazy to avoid initialization bug
            NodeDescriptor {
                value(COLOR_KEY) {
                    type(ValueType.STRING, ValueType.NUMBER)
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

public val Solid.color: ColorAccessor
    get() = ColorAccessor(
        allProperties(inherit = true),
        MATERIAL_COLOR_KEY
    )

public var Solid.material: SolidMaterial?
    get() = getProperty(MATERIAL_KEY, inherit = true).node?.let { SolidMaterial.read(it) }
    set(value) = setProperty(MATERIAL_KEY, value?.config)

@VisionBuilder
public fun Solid.material(builder: SolidMaterial.() -> Unit) {
    val node = allProperties(
        inherit = true,
        includeStyles = true,
        includeDefaults = true
    ).getItem(MATERIAL_KEY).node
    if (node != null) {
        SolidMaterial.update(node, builder)
    } else {
        setProperty(MATERIAL_KEY, SolidMaterial(builder))
    }
}

public var Solid.opacity: Number?
    get() = getProperty(MATERIAL_OPACITY_KEY, inherit = true).number
    set(value) {
        setProperty(MATERIAL_OPACITY_KEY, value?.asValue())
    }