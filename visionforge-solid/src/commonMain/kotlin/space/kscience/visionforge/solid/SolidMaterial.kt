package space.kscience.visionforge.solid

import space.kscience.dataforge.meta.*
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.value
import space.kscience.dataforge.meta.set
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.dataforge.names.plus
import space.kscience.visionforge.*
import space.kscience.visionforge.solid.SolidMaterial.Companion.MATERIAL_COLOR_KEY
import space.kscience.visionforge.solid.SolidMaterial.Companion.MATERIAL_KEY
import space.kscience.visionforge.solid.SolidMaterial.Companion.MATERIAL_OPACITY_KEY

@VisionBuilder
public class SolidMaterial : Scheme() {

    /**
     * Primary web-color for the material
     */
    public val color: ColorAccessor = ColorAccessor(meta, COLOR_KEY)

    /**
     * Specular color for phong material
     */
    public val specularColor: ColorAccessor = ColorAccessor(meta, SPECULAR_COLOR_KEY)

    public val emissiveColor: ColorAccessor = ColorAccessor(meta, EMISSIVE_COLOR_KEY)

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
        public val TYPE_KEY: Name = "type".asName()
        public val SPECULAR_COLOR_KEY: Name = "specularColor".asName()
        public val EMISSIVE_COLOR_KEY: Name = "emissiveColor".asName()
        public val OPACITY_KEY: Name = "opacity".asName()
        public val MATERIAL_OPACITY_KEY: Name = MATERIAL_KEY + OPACITY_KEY
        public val WIREFRAME_KEY: Name = "wireframe".asName()
        public val MATERIAL_COLOR_KEY: Name = MATERIAL_KEY + COLOR_KEY
        public val MATERIAL_EMISSIVE_COLOR_KEY: Name = MATERIAL_KEY + EMISSIVE_COLOR_KEY
        public val MATERIAL_SPECULAR_COLOR_KEY: Name = MATERIAL_KEY + SPECULAR_COLOR_KEY
        public val MATERIAL_WIREFRAME_KEY: Name = MATERIAL_KEY + WIREFRAME_KEY

        public override val descriptor: MetaDescriptor by lazy {
            //must be lazy to avoid initialization bug
            MetaDescriptor {
                inherited = true

                value(TYPE_KEY, ValueType.STRING){
                    inherited = true
                    allowedValues = listOf("default".asValue(), "simple".asValue())
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
    get() = ColorAccessor(properties.root(true), MATERIAL_COLOR_KEY)

public var Solid.material: SolidMaterial?
    get() = SolidMaterial.read(properties.getProperty(MATERIAL_KEY))
    set(value) = properties.setProperty(MATERIAL_KEY, value?.meta)

@VisionBuilder
public fun Solid.material(builder: SolidMaterial.() -> Unit) {
    properties.getProperty(MATERIAL_KEY).updateWith(SolidMaterial, builder)
}

public var Solid.opacity: Number?
    get() = properties.getValue(MATERIAL_OPACITY_KEY, inherit = true)?.number
    set(value) {
        properties.setValue(MATERIAL_OPACITY_KEY, value?.asValue())
    }