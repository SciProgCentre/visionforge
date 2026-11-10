package space.kscience.visionforge.demo.factory.styles

import space.kscience.dataforge.meta.Meta
import space.kscience.visionforge.Colors
import space.kscience.visionforge.demo.factory.model.FactorySceneConfig
import space.kscience.visionforge.setStyle
import space.kscience.visionforge.solid.SolidGroup
import space.kscience.visionforge.solid.SolidMaterial
import space.kscience.visionforge.solid.invoke

public object MogStyles {

    public fun rgbToHex(rgb: List<Double>): String {
        val r = (rgb.getOrElse(0) { 0.5 } * 255.0).toInt().coerceIn(0, 255)
        val g = (rgb.getOrElse(1) { 0.5 } * 255.0).toInt().coerceIn(0, 255)
        val b = (rgb.getOrElse(2) { 0.5 } * 255.0).toInt().coerceIn(0, 255)
        return Colors.rgbToString(r.toUByte(), g.toUByte(), b.toUByte())
    }

    public fun materialStyleName(material: String): String = "material_$material"
    public fun gostStyleName(gostGroup: String): String = "gost_$gostGroup"
    public fun stateStyleName(state: String): String = "state_$state"

    public fun applyStyles(root: SolidGroup, config: FactorySceneConfig) {
        // Material styles
        for ((matName, spec) in config.materials) {
            val hex = rgbToHex(spec.rgb)
            val mat = SolidMaterial {
                color(hex)
                opacity = 1.0f
                type = "phong"
            }
            root.setStyle(
                styleName = materialStyleName(matName),
                style = Meta {
                    "material" put mat.toMeta()
                }
            )
        }

        // GOST 14202 styles
        for ((gostKey, spec) in config.gost14202) {
            val hex = rgbToHex(spec.rgb)
            val mat = SolidMaterial {
                color(hex)
                opacity = 1.0f
                type = "phong"
            }
            root.setStyle(
                styleName = gostStyleName(gostKey),
                style = Meta {
                    "material" put mat.toMeta()
                }
            )
        }

        // State styles
        for ((stateKey, spec) in config.states) {
            val hex = rgbToHex(spec.rgb)
            val mat = SolidMaterial {
                color(hex)
                opacity = if (stateKey == "flush") 0.85f else 1.0f
                type = "phong"
            }
            root.setStyle(
                styleName = stateStyleName(stateKey),
                style = Meta {
                    "material" put mat.toMeta()
                }
            )
        }
    }
}
