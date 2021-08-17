package space.kscience.visionforge.solid

import javafx.scene.paint.Color
import javafx.scene.paint.Material
import javafx.scene.paint.PhongMaterial
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.double
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.int
import space.kscience.dataforge.values.ValueType
import space.kscience.dataforge.values.int
import space.kscience.dataforge.values.string
import space.kscience.visionforge.Colors
import space.kscience.visionforge.solid.FXMaterials.GREY

public object FXMaterials {
    public val RED: PhongMaterial = PhongMaterial().apply {
        diffuseColor = Color.DARKRED
        specularColor = Color.WHITE
    }

    public val WHITE: PhongMaterial = PhongMaterial().apply {
        diffuseColor = Color.WHITE
        specularColor = Color.LIGHTBLUE
    }

    public val GREY: PhongMaterial = PhongMaterial().apply {
        diffuseColor = Color.DARKGREY
        specularColor = Color.WHITE
    }

    public val BLUE: PhongMaterial = PhongMaterial(Color.BLUE)

}

/**
 * Infer color based on meta item
 * @param opacity default opacity
 */
public fun Meta.color(opacity: Double = 1.0): Color = value?.let {
    if (it.type == ValueType.NUMBER) {
        val int = it.int
        val red = int and 0x00ff0000 shr 16
        val green = int and 0x0000ff00 shr 8
        val blue = int and 0x000000ff
        Color.rgb(red, green, blue, opacity)
    } else {
        Color.web(it.string)
    }
} ?: Color.rgb(
    this[Colors.RED_KEY]?.int ?: 0,
    this[Colors.GREEN_KEY]?.int ?: 0,
    this[Colors.BLUE_KEY]?.int ?: 0,
    this[SolidMaterial.OPACITY_KEY]?.double ?: opacity
)

/**
 * Infer FX material based on meta item
 */
public fun Meta?.material(): Material {
    if (this == null) return GREY
    return value?.let {
        PhongMaterial(color())
    } ?: PhongMaterial().apply {
        val opacity = get(SolidMaterial.OPACITY_KEY).double ?: 1.0
        diffuseColor = get(SolidMaterial.COLOR_KEY)?.color(opacity) ?: Color.DARKGREY
        specularColor = get(SolidMaterial.SPECULAR_COLOR_KEY)?.color(opacity) ?: Color.WHITE
    }
}

