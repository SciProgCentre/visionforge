package space.kscience.visionforge.solid

import space.kscience.dataforge.meta.*
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.ValueRestriction
import space.kscience.dataforge.meta.descriptors.value
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.plus
import space.kscience.visionforge.VisionBuilder
import space.kscience.visionforge.hide
import space.kscience.visionforge.inherited
import space.kscience.visionforge.solid.SolidMaterial.Companion.MATERIAL_COLOR_KEY
import space.kscience.visionforge.solid.SolidMaterial.Companion.MATERIAL_KEY
import space.kscience.visionforge.solid.SolidMaterial.Companion.MATERIAL_OPACITY_KEY
import space.kscience.visionforge.widgetType

/**
 * A scheme for vision material
 */
@VisionBuilder
public class SolidMaterial : Scheme() {

    public var type: String by string("default", key = TYPE_KEY)

    /**
     * Primary web-color for the material
     */
    public val color: ColorAccessor = ColorAccessor(meta.view(COLOR_KEY))

    /**
     * Specular color for phong material
     */
    public val specularColor: ColorAccessor = ColorAccessor(meta.view(SPECULAR_COLOR_KEY))

    public val emissiveColor: ColorAccessor = ColorAccessor(meta.view(EMISSIVE_COLOR_KEY))

    /**
     * Opacity
     */
    public var opacity: Float by float(1f, key = OPACITY_KEY)

    /**
     * Replace material by wire frame
     */
    public var wireframe: Boolean by boolean(false, WIREFRAME_KEY)

    public companion object : SchemeSpec<SolidMaterial>(::SolidMaterial) {

        public val MATERIAL_KEY: Name = Name.of("material")
        public val COLOR_KEY: Name = Name.of("color")
        public val TYPE_KEY: Name = Name.of("type")
        public val SPECULAR_COLOR_KEY: Name = Name.of("specularColor")
        public val EMISSIVE_COLOR_KEY: Name = Name.of("emissiveColor")
        public val OPACITY_KEY: Name = Name.of("opacity")
        public val MATERIAL_OPACITY_KEY: Name = MATERIAL_KEY + OPACITY_KEY
        public val WIREFRAME_KEY: Name = Name.of("wireframe")
        public val MATERIAL_COLOR_KEY: Name = MATERIAL_KEY + COLOR_KEY
        public val MATERIAL_EMISSIVE_COLOR_KEY: Name = MATERIAL_KEY + EMISSIVE_COLOR_KEY
        public val MATERIAL_SPECULAR_COLOR_KEY: Name = MATERIAL_KEY + SPECULAR_COLOR_KEY
        public val MATERIAL_WIREFRAME_KEY: Name = MATERIAL_KEY + WIREFRAME_KEY

        public val EDGES_KEY: Name = Name.of("edges")
        public val ENABLED_KEY: Name = Name.of("enabled")
        public val EDGES_ENABLED_KEY: Name = EDGES_KEY + ENABLED_KEY
        public val EDGES_MATERIAL_KEY: Name = EDGES_KEY + MATERIAL_KEY

        public override val descriptor: MetaDescriptor by lazy {
            //must be lazy to avoid initialization bug
            MetaDescriptor {
                inherited = true
                valueRestriction = ValueRestriction.ABSENT

                value(TYPE_KEY, ValueType.STRING) {
                    inherited = true
                    allowedValues = listOf("default", "basic", "lambert", "phong").map { it.asValue() }
                    default("default")
                }

                value(COLOR_KEY, ValueType.STRING, ValueType.NUMBER) {
                    inherited = true
                    widgetType = "color"
                }

                value(SPECULAR_COLOR_KEY, ValueType.STRING, ValueType.NUMBER) {
                    inherited = true
                    widgetType = "color"
                    hide()
                }

                value(EMISSIVE_COLOR_KEY, ValueType.STRING, ValueType.NUMBER) {
                    inherited = true
                    widgetType = "color"
                    hide()
                }

                value(OPACITY_KEY, ValueType.NUMBER) {
                    inherited = true
                    default(1.0)
                    attributes["min"] = 0.0
                    attributes["max"] = 1.0
                    attributes["step"] = 0.1
                    widgetType = "slider"
                }

                value(WIREFRAME_KEY, ValueType.BOOLEAN) {
                    inherited = true
                    default(false)
                }
            }
        }
    }
}

public val Solid.color: ColorAccessor
    get() = ColorAccessor(mutableProperty(MATERIAL_COLOR_KEY, inherited = true))

public var Solid.material: SolidMaterial?
    get() = readProperty(MATERIAL_KEY)?.let { SolidMaterial.read(it)}
    set(value) = properties.set(MATERIAL_KEY, value?.meta)

public fun Solid.material(builder: SolidMaterial.() -> Unit) {
    mutableProperty(MATERIAL_KEY, inherited = false, useStyles = false).updateWith(SolidMaterial, builder)
}

public var Solid.opacity: Number?
    get() = readProperty(MATERIAL_OPACITY_KEY, inherited = true).number
    set(value) {
        properties.setValue(MATERIAL_OPACITY_KEY, value?.asValue())
    }


public fun Solid.edges(enabled: Boolean = true, block: SolidMaterial.() -> Unit = {}) {
    properties[SolidMaterial.EDGES_ENABLED_KEY] = enabled
    SolidMaterial.write(mutableProperty(SolidMaterial.EDGES_MATERIAL_KEY)).apply(block)
}