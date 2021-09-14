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
import react.functionComponent
import space.kscience.dataforge.meta.descriptors.allowedValues
import space.kscience.dataforge.values.asValue
import space.kscience.dataforge.values.string

@JsExport
public val MultiSelectChooser: FunctionComponent<ValueChooserProps> =
    functionComponent("MultiSelectChooser") { props ->
        val onChange: (Event) -> Unit = { event: Event ->
            val newSelected = (event.target as HTMLSelectElement).selectedOptions.asList()
                .map { (it as HTMLOptionElement).value.asValue() }
            props.meta.value = newSelected.asValue()
        }

        select {
            attrs {
                multiple = true
                values = (props.actual.value?.list ?: emptyList()).mapTo(HashSet()) { it.string }
                onChangeFunction = onChange
            }
            props.descriptor?.allowedValues?.forEach { optionValue ->
                option {
                    +optionValue.string
                }
            }

        }
    }