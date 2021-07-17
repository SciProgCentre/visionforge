/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package space.kscience.visionforge.editor

import javafx.collections.FXCollections
import javafx.scene.control.ComboBox
import javafx.util.StringConverter
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.value
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.dataforge.values.Value
import space.kscience.dataforge.values.parseValue
import space.kscience.dataforge.values.string
import java.util.*

public class ComboBoxValueChooser(public val values: Collection<Value>? = null) : ValueChooserBase<ComboBox<Value>>() {

    //    @Override
    //    protected void displayError(String error) {
    //        //TODO ControlsFX decorator here
    //    }

    private fun allowedValues(): Collection<Value> {
        return values ?: descriptor?.allowedValues ?: Collections.emptyList();
    }

    override fun buildNode(): ComboBox<Value> {
        val node = ComboBox(FXCollections.observableArrayList(allowedValues()))
        node.maxWidth = java.lang.Double.MAX_VALUE
        node.isEditable = false
        node.selectionModel.select(currentValue())
        node.converter = object : StringConverter<Value>() {
            override fun toString(value: Value?): String {
                return value?.string ?: ""
            }

            override fun fromString(string: String?): Value {
                return (string ?: "").parseValue()
            }

        }
        this.valueProperty.bind(node.valueProperty())
        return node
    }

    override fun setDisplayValue(value: Value) {
        node.selectionModel.select(value)
    }

    public companion object : ValueChooser.Factory {
        override val name: Name = "combo".asName()

        override fun invoke(meta: Meta): ValueChooser =
            ComboBoxValueChooser(meta["values"].value?.list)
    }

}
