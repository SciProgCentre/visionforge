package space.kscience.visionforge.react

import kotlinx.html.js.onChangeFunction
import org.w3c.dom.HTMLOptionElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.asList
import org.w3c.dom.events.Event
import react.FunctionComponent
import react.dom.attrs
import react.dom.option
import react.dom.select
import react.functionalComponent
import react.useState
import space.kscience.dataforge.meta.descriptors.allowedValues
import space.kscience.dataforge.values.asValue
import space.kscience.dataforge.values.string

@JsExport
public val MultiSelectChooser: FunctionComponent<ValueChooserProps> =
    functionalComponent("MultiSelectChooser") { props ->
        var selectedItems by useState { props.item?.value?.list ?: emptyList() }

        val onChange: (Event) -> Unit = { event: Event ->
            val newSelected = (event.target as HTMLSelectElement).selectedOptions.asList()
                .map { (it as HTMLOptionElement).value.asValue() }
            props.valueChanged?.invoke(newSelected.asValue())
            selectedItems = newSelected
        }

        select {
            attrs {
                multiple = true
                values = selectedItems.mapTo(HashSet()) { it.string }
                onChangeFunction = onChange
            }
            props.descriptor?.allowedValues?.forEach { optionValue ->
                option {
                    +optionValue.string
                }
            }

        }
    }