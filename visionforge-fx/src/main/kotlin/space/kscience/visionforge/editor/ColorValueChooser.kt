package space.kscience.visionforge.editor

import javafx.scene.control.ColorPicker
import javafx.scene.paint.Color
import org.slf4j.LoggerFactory
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.dataforge.values.Null
import space.kscience.dataforge.values.Value
import space.kscience.dataforge.values.asValue
import space.kscience.dataforge.values.string
import tornadofx.*

/**
 * Created by darksnake on 01-May-17.
 */
public class ColorValueChooser : ValueChooserBase<ColorPicker>() {
    private fun ColorPicker.setColor(value: Value?) {
        if (value != null && value != Null) {
            try {
                runLater {
                    this.value = Color.valueOf(value.string)
                }
            } catch (ex: Exception) {
                LoggerFactory.getLogger(javaClass).warn("Invalid color field value: $value")
            }
        }
    }


    override fun setDisplayValue(value: Value) {
        node.setColor(value)
    }

    override fun buildNode(): ColorPicker {
        val node = ColorPicker()
        node.styleClass.add("split-button")
        node.maxWidth = java.lang.Double.MAX_VALUE
        node.setColor(value)
        node.setOnAction { _ -> value = node.value.toString().asValue() }
        return node
    }

    public companion object : ValueChooser.Factory {
        override val name: Name = "color".asName()

        override fun invoke(meta: Meta): ValueChooser =
            ColorValueChooser()
    }
}
