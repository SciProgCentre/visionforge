package hep.dataforge.vision.editor

import hep.dataforge.meta.Meta
import hep.dataforge.names.Name
import hep.dataforge.names.asName
import hep.dataforge.values.Null
import hep.dataforge.values.Value
import hep.dataforge.values.asValue
import hep.dataforge.values.string
import javafx.scene.control.ColorPicker
import javafx.scene.paint.Color
import org.slf4j.LoggerFactory
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

    companion object : ValueChooser.Factory {
        override val name: Name = "color".asName()

        override fun invoke(meta: Meta): ValueChooser =
            ColorValueChooser()
    }
}
