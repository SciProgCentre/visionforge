package hep.dataforge.vis.spatial

import hep.dataforge.meta.MetaItem
import hep.dataforge.meta.double
import hep.dataforge.meta.get
import hep.dataforge.meta.int
import hep.dataforge.values.ValueType
import javafx.scene.paint.Color
import javafx.scene.paint.Material
import javafx.scene.paint.PhongMaterial

object Materials {
    val RED = PhongMaterial().apply {
        diffuseColor = Color.DARKRED
        specularColor = Color.RED
    }

    val WHITE = PhongMaterial().apply {
        diffuseColor = Color.WHITE
        specularColor = Color.LIGHTBLUE
    }

    val GREY = PhongMaterial().apply {
        diffuseColor = Color.DARKGREY
        specularColor = Color.GREY
    }

    val BLUE = PhongMaterial(Color.BLUE)
}

/**
 * Infer color based on meta item
 */
fun MetaItem<*>.color(): Color {
    return when (this) {
        is MetaItem.ValueItem -> if (this.value.type == ValueType.STRING) {
            Color.web(this.value.string)
        } else {
            val int = value.number.toInt()
            val red = int and 0x00ff0000 shr 16
            val green = int and 0x0000ff00 shr 8
            val blue = int and 0x000000ff
            Color.rgb(red, green, blue)
        }
        is MetaItem.NodeItem -> {
            Color.rgb(
                node["red"]?.int ?: 0,
                node["green"]?.int ?: 0,
                node["blue"]?.int ?: 0,
                node["opacity"]?.double ?: 1.0
            )
        }
    }
}

/**
 * Infer FX material based on meta item
 */
fun MetaItem<*>?.material(): Material {
    return when (this) {
        null -> Materials.GREY
        is MetaItem.ValueItem -> PhongMaterial(color())
        is MetaItem.NodeItem -> PhongMaterial().apply {
            (node["color"]?: this@material).let { diffuseColor = it.color() }
            node["specularColor"]?.let { specularColor = it.color() }
        }
    }
}

