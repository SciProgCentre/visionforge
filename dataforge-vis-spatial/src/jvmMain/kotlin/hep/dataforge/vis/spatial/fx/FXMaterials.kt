package hep.dataforge.vis.spatial.fx

import hep.dataforge.meta.MetaItem
import hep.dataforge.meta.double
import hep.dataforge.meta.get
import hep.dataforge.meta.int
import hep.dataforge.values.ValueType
import javafx.scene.paint.Color
import javafx.scene.paint.Material
import javafx.scene.paint.PhongMaterial

object FXMaterials {
    val RED = PhongMaterial().apply {
        diffuseColor = Color.DARKRED
        specularColor = Color.WHITE
    }

    val WHITE = PhongMaterial().apply {
        diffuseColor = Color.WHITE
        specularColor = Color.LIGHTBLUE
    }

    val GREY = PhongMaterial().apply {
        diffuseColor = Color.DARKGREY
        specularColor = Color.WHITE
    }

    val BLUE = PhongMaterial(Color.BLUE)
}

/**
 * Infer color based on meta item
 * @param opacity default opacity
 */
fun MetaItem<*>.color(opacity: Double = 1.0): Color {
    return when (this) {
        is MetaItem.ValueItem -> if (this.value.type == ValueType.NUMBER) {
            val int = value.number.toInt()
            val red = int and 0x00ff0000 shr 16
            val green = int and 0x0000ff00 shr 8
            val blue = int and 0x000000ff
            Color.rgb(red, green, blue)
        } else {
            Color.web(this.value.string)
        }
        is MetaItem.NodeItem -> {
            Color.rgb(
                node["red"]?.int ?: 0,
                node["green"]?.int ?: 0,
                node["blue"]?.int ?: 0,
                node["opacity"]?.double ?: opacity
            )
        }
    }
}

/**
 * Infer FX material based on meta item
 */
fun MetaItem<*>?.material(): Material {
    return when (this) {
        null -> FXMaterials.GREY
        is MetaItem.ValueItem -> PhongMaterial(color())
        is MetaItem.NodeItem -> PhongMaterial().apply {
            val opacity = node["opacity"].double ?: 1.0
            diffuseColor = node["color"]?.color(opacity) ?: Color.DARKGREY
            specularColor = node["specularColor"]?.color(opacity) ?: Color.WHITE
        }
    }
}

