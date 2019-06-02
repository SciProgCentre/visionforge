/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.vis.fx.values

import hep.dataforge.descriptors.ValueDescriptor
import hep.dataforge.values.Null
import hep.dataforge.values.Value
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Node
import org.slf4j.LoggerFactory
import tornadofx.*

/**
 * ValueChooser boilerplate
 *
 * @author Alexander Nozik
 */
abstract class ValueChooserBase<out T : Node> : ValueChooser {

    override val node by lazy { buildNode() }
    final override val valueProperty = SimpleObjectProperty<Value>(Null)
    final override val descriptorProperty = SimpleObjectProperty<ValueDescriptor>()

    override var descriptor: ValueDescriptor? by descriptorProperty
    override var value: Value? by valueProperty

    fun resetValue() {
        setDisplayValue(currentValue())
    }

    /**
     * Current value or default value
     * @return
     */
    protected fun currentValue(): Value {
        return value ?: descriptor?.default ?: Null
    }

    /**
     * True if builder node is successful
     *
     * @return
     */
    protected abstract fun buildNode(): T

    /**
     * Display validation error
     *
     * @param error
     */
    protected fun displayError(error: String) {
        LoggerFactory.getLogger(javaClass).error(error)
    }

    override fun setCallback(callback: ValueCallback) {
        valueProperty.onChange { newValue: Value? ->
            val response = callback(newValue ?: Null)
            if (response.value != valueProperty.get()) {
                setDisplayValue(response.value)
            }

            if (!response.success) {
                displayError(response.message)
            }
        }
    }
}
