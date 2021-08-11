/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package space.kscience.visionforge.editor

import javafx.beans.property.ObjectProperty
import javafx.beans.value.ObservableValue
import javafx.scene.Node
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.allowedValues
import space.kscience.dataforge.meta.descriptors.validate
import space.kscience.dataforge.misc.Named
import space.kscience.dataforge.misc.Type
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.values.Null
import space.kscience.dataforge.values.Value
import space.kscience.visionforge.widget
import space.kscience.visionforge.widgetType
import tornadofx.*

/**
 * A value chooser object. Must have an empty constructor to be invoked by
 * reflections.
 *
 * @author [Alexander Nozik](mailto:altavir@gmail.com)
 */
public interface ValueChooser {

    /**
     * Get or create a Node that could be later inserted into some parent
     * object.
     *
     * @return
     */
    public val node: Node

    /**
     * The descriptor property for this value. Could be null
     *
     * @return
     */
    public val descriptorProperty: ObjectProperty<MetaDescriptor?>
    public var descriptor: MetaDescriptor?

    public val valueProperty: ObjectProperty<Value?>
    public var value: Value?


    /**
     * Set display value but do not notify listeners
     *
     * @param value
     */
    public fun setDisplayValue(value: Value)


    public fun setDisabled(disabled: Boolean) {
        //TODO replace by property
    }

    public fun setCallback(callback: ValueCallback)

    @Type("space.kscience.dataforge.vis.fx.valueChooserFactory")
    public interface Factory : Named {
        public operator fun invoke(meta: Meta = Meta.EMPTY): ValueChooser
    }

    public companion object {

        private fun findWidgetByType(context: Context, type: String): Factory? {
            return when (Name.parse(type)) {
                TextValueChooser.name -> TextValueChooser
                ColorValueChooser.name -> ColorValueChooser
                ComboBoxValueChooser.name -> ComboBoxValueChooser
                else -> null//context.provideByType(type)//Search for additional factories in the plugin
            }
        }

        private fun build(context: Context, descriptor: MetaDescriptor?): ValueChooser {
            return if (descriptor == null) {
                TextValueChooser();
            } else {
                val widgetType = descriptor.widgetType
                val chooser: ValueChooser = when {
                    widgetType != null -> {
                        findWidgetByType(
                            context,
                            widgetType
                        )?.invoke(
                            descriptor.widget
                        ) ?: TextValueChooser()
                    }
                    !descriptor.allowedValues.isNullOrEmpty() -> ComboBoxValueChooser()
                    else -> TextValueChooser()
                }
                chooser.descriptor = descriptor
                chooser
            }
        }

        public fun build(
            context: Context,
            value: ObservableValue<Value?>,
            descriptor: MetaDescriptor? = null,
            setter: (Value) -> Unit,
        ): ValueChooser {
            val chooser = build(context, descriptor)
            chooser.setDisplayValue(value.value ?: Null)
            value.onChange {
                chooser.setDisplayValue(it ?: Null)
            }
            chooser.setCallback { result ->
                if (descriptor?.validate(result) != false) {
                    setter(result)
                    ValueCallbackResponse(true, result, "OK")
                } else {
                    ValueCallbackResponse(
                        false,
                        value.value ?: Null,
                        "Not allowed"
                    )
                }
            }
            return chooser
        }
    }
}